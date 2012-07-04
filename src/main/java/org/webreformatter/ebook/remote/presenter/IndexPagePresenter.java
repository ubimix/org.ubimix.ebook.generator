/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.bom.IBookToc;
import org.webreformatter.ebook.bom.IBookToc.IBookTocItem;
import org.webreformatter.ebook.bom.json.JsonBookMeta;
import org.webreformatter.ebook.bom.json.JsonBookToc;
import org.webreformatter.ebook.bom.json.JsonBookToc.JsonBookTocItem;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.scrappers.IScrapper;

/**
 * @author kotelnikov
 */
public class IndexPagePresenter extends RemotePagePresenter {

    /**
     * @author kotelnikov
     */
    public interface IIndexPageScrapper extends IScrapper {

        XmlWrapper getTitleElement() throws XmlException;

        XmlWrapper getTocList() throws XmlException;
    }

    private final static Logger log = Logger.getLogger(IndexPagePresenter.class
        .getName());

    private JsonBookMeta fBookMeta;

    private JsonBookToc fBookToc;

    private IIndexPageScrapper fFieldAccessor;

    private Set<Uri> fReferences;

    private XmlWrapper fTocElement;

    public IndexPagePresenter(
        ISite site,
        RemoteResource resource) throws IOException, XmlException {
        super(site, resource);
        fFieldAccessor = newScrapper(this, IIndexPageScrapper.class);
    }

    private void addTocItemFields(JsonBookTocItem tocItem, XmlWrapper item)
        throws XmlException,
        IOException {
        XmlWrapper ref = item.eval(".//html:a");
        String label;
        if (ref != null) {
            label = ref.toText();
            String hrefStr = ref.getAttribute("href");
            if (hrefStr != null) {
                Uri href = new Uri(hrefStr);
                setFullUrl(tocItem, href);
                IPresenter presenter = getPresenter(href, false);
                if (presenter instanceof IContentPresenter) {
                    Uri resourcePath = ((IContentPresenter) presenter)
                        .getResourcePath();
                    tocItem.setContentHref(resourcePath);
                }
            }
        } else {
            label = item.toString(false, false);
        }
        tocItem.setLabel(label);
    }

    /**
     * Checks that the root TOC element was loaded and all references were
     * resolved.
     * 
     * @return the top TOC element
     * @throws XmlException
     */
    private XmlWrapper checkTocElement() throws XmlException {
        if (fTocElement == null) {
            fTocElement = fFieldAccessor.getTocList();
            fReferences = new LinkedHashSet<Uri>();
            if (fTocElement != null) {
                List<XmlWrapper> ancors = fTocElement
                    .evalList(".//html:a[@href]");
                if (ancors != null) {
                    for (XmlWrapper ancor : ancors) {
                        String hrefStr = ancor.getAttribute("href");
                        if (hrefStr != null) {
                            Uri uri = new Uri(hrefStr);
                            uri = getResolved(uri, false);
                            ancor.setAttribute("href", uri.toString());
                            fReferences.add(uri);
                        }
                    }
                }
            }
        }
        return fTocElement;
    }

    public JsonBookMeta getBookMeta() throws XmlException {
        if (fBookMeta == null) {
            fBookMeta = readBookMetadata();
        }
        return fBookMeta;
    }

    public JsonBookToc getBookToc() throws XmlException, IOException {
        if (fBookToc == null) {
            JsonBookToc toc = new JsonBookToc();
            XmlWrapper ul = checkTocElement();
            if (ul != null) {
                ul = ul.newCopy();
                List<IBookTocItem> items = getItems(ul);
                if (items != null) {
                    toc.setTocItems(items);
                }
            }
            fBookToc = toc;
        }
        return fBookToc;
    }

    @Override
    public Set<Uri> getImageReferences() {
        return Collections.emptySet();
    }

    private List<IBookTocItem> getItems(XmlWrapper ul)
        throws XmlException,
        IOException {
        if (ul == null) {
            return null;
        }
        List<XmlWrapper> items = ul.evalList("html:li");
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<IBookTocItem> result = new ArrayList<IBookToc.IBookTocItem>();
        for (XmlWrapper item : items) {
            JsonBookTocItem tocItem = new JsonBookTocItem();
            result.add(tocItem);
            XmlWrapper childList = item.eval("html:ul");
            if (childList != null) {
                List<IBookTocItem> tocItemChildren = getItems(childList);
                childList.remove();
                addTocItemFields(tocItem, item);
                if (tocItemChildren != null) {
                    tocItem.setChildren(tocItemChildren);
                }
            } else {
                addTocItemFields(tocItem, item);
            }
        }
        return result;
    }

    @Override
    public Set<Uri> getPageReferences() throws XmlException, IOException {
        checkTocElement();
        return fReferences;
    }

    protected void handleError(String msg, Throwable t) {
        log.log(Level.FINE, msg, t);
        throw new RuntimeException(msg, t);
    }

    private JsonBookMeta readBookMetadata() throws XmlException {
        JsonBookMeta meta = new JsonBookMeta();
        XmlWrapper e = fFieldAccessor.getTitleElement();
        String title = e != null ? e.toText().trim() : null;
        if (title == null) {
            Uri pageUrl = getResourceUrl();
            title = pageUrl.getPath().getFileName();
        }
        meta.setBookTitle(title);
        meta.setBookIdentifier(getResourceId());
        // FIXME: extract page creator and other meta-information from the pag
        String creator = "";
        String language = "";
        meta.setBookCreator(creator);
        meta.setBookLanguage(language);
        return meta;
    }

}
