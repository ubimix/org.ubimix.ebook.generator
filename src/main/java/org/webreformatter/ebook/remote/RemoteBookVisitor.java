package org.webreformatter.ebook.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.BookId;
import org.webreformatter.ebook.bem.IBookManifestListener;
import org.webreformatter.ebook.bem.IBookVisitor;
import org.webreformatter.ebook.bom.json.JsonBookMeta;
import org.webreformatter.ebook.bom.json.JsonBookToc;
import org.webreformatter.ebook.bom2bem.BookVisitor;
import org.webreformatter.ebook.io.IOutput;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.presenter.Base;
import org.webreformatter.ebook.remote.presenter.IContentPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.RemoteResourcePresenter;

/**
 * @author kotelnikov
 */
public class RemoteBookVisitor extends Base implements IBookVisitor {

    private int fOrderCounter = 0;

    private Site fSite;

    private Map<Uri, Integer> fVisitOrder = new HashMap<Uri, Integer>();

    public RemoteBookVisitor(Site site) throws IOException {
        super();
        fSite = site;
    }

    protected boolean addToList(RemotePagePresenter presenter) {
        boolean result = false;
        Uri uri = presenter.getResourceUrl();
        Integer order = fVisitOrder.get(uri);
        if (order == null) {
            order = fOrderCounter++;
            fVisitOrder.put(uri, order);
            String pageName = newPageName(order);
            presenter.setFileName(pageName);
            result = true;
        }
        return result;
    }

    private IndexPagePresenter downloadAll() throws IOException, XmlException {
        Uri indexUrl = fSite.getSiteUrl();
        IndexPagePresenter indexPresenter = (IndexPagePresenter) getPresenter(
            indexUrl,
            true);
        downloadReferencedResources(indexPresenter);
        return indexPresenter;
    }

    private void downloadReferencedResources(RemotePagePresenter pagePresenter)
        throws IOException,
        XmlException {
        Set<Uri> references = pagePresenter.getPageReferences();
        Set<RemotePagePresenter> children = new HashSet<RemotePagePresenter>();
        if (references != null && !references.isEmpty()) {
            for (Uri uri : references) {
                IPresenter presenter = getPresenter(uri, true);
                if (presenter instanceof RemotePagePresenter) {
                    RemotePagePresenter childPresenter = (RemotePagePresenter) presenter;
                    if (addToList(childPresenter)) {
                        children.add(childPresenter);
                    }
                }
            }
        }

        // Download all referenced pages
        for (RemotePagePresenter child : children) {
            downloadReferencedResources(child);
        }

        // Get all referenced images and other binaries
        Set<Uri> imageReferences = pagePresenter.getImageReferences();
        for (Uri imageUrl : imageReferences) {
            getPresenter(imageUrl, true);
        }
    }

    @Override
    protected Site getSite() {
        return fSite;
    }

    protected String newPageName(int order) {
        String result;
        if (order == 0) {
            result = "index.html";
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(Integer.toString(order));
            while (buf.length() < 4) {
                buf.insert(0, "0");
            }
            buf.insert(0, "page-");
            buf.append(".html");
            result = buf.toString();
        }
        return result;
    }

    @Override
    public void visitBook(IBookListener listener) throws IOException {
        try {
            listener.begin();
            try {
                IndexPagePresenter indexPresenter = downloadAll();
                if (indexPresenter.isValid()) {

                    JsonBookMeta meta = indexPresenter.getBookMeta();
                    BookVisitor.visitMetadata(
                        meta,
                        listener.getMetadataListener());

                    JsonBookToc toc = indexPresenter.getBookToc();
                    BookVisitor.visitToc(toc, listener.getTocListener());

                    IBookManifestListener manifestListener = listener
                        .getManifestListener();
                    manifestListener.beginBookManifest();

                    for (Iterator<Uri> urls = getPresenterUrls(); urls
                        .hasNext();) {
                        Uri uri = urls.next();
                        IPresenter presenter = getPresenter(uri, false);
                        if (!(presenter instanceof RemoteResourcePresenter)) {
                            continue;
                        }
                        RemoteResourcePresenter remoteResourcePresenter = (RemoteResourcePresenter) presenter;
                        BookId itemId = remoteResourcePresenter.getResourceId();
                        Uri itemPath = remoteResourcePresenter
                            .getResourcePath(); // indexPresenter.getPathTo(presenter);
                        if (presenter instanceof IContentPresenter) {
                            IOutput output = null;
                            if (presenter instanceof RemotePagePresenter) {
                                Uri resourceUri = remoteResourcePresenter
                                    .getResourceUrl();
                                Integer orderNumber = fVisitOrder
                                    .get(resourceUri);
                                output = manifestListener.onBookSection(
                                    itemPath,
                                    itemId,
                                    orderNumber);
                            } else {
                                String itemMediaType = remoteResourcePresenter
                                    .getMediaType();
                                output = manifestListener.onBookEntry(
                                    itemPath,
                                    itemId,
                                    itemMediaType);
                            }
                            if (output != null) {
                                try {
                                    IContentPresenter contentPresenter = (IContentPresenter) presenter;
                                    IFormatter formatter = contentPresenter
                                        .getFormatter();
                                    if (formatter != null) {
                                        formatter.writeTo(output);
                                    }
                                } finally {
                                    output.close();
                                }
                            }
                        }
                    }
                    manifestListener.endBookManifest();
                } else {
                    // FIXME: add notification
                }
            } finally {
                listener.end();
            }
        } catch (Throwable t) {
            Uri indexUrl = fSite.getSiteUrl();
            throw RemoteResourcePresenter.onError(
                "Can not build a book from this site. "
                    + "Index URL: '"
                    + indexUrl
                    + "'.",
                t);
        }
    }

}