/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Text;
import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XHTMLUtils;
import org.webreformatter.commons.xml.XmlAcceptor;
import org.webreformatter.commons.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.html.HtmlBurner;
import org.webreformatter.commons.xml.html.HtmlBurner.HtmlBurnerConfig;
import org.webreformatter.commons.xml.html.TagDictionary;
import org.webreformatter.ebook.bom.json.JsonBookSection;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.scrappers.IScrapper;

/**
 * @author kotelnikov
 */
public class InnerPagePresenter extends RemotePagePresenter
    implements
    IContentPresenter {

    public interface IInnerPageFormatter extends IFormatter {
    }

    /**
     * @author kotelnikov
     */
    public interface IInnerPageScrapper extends IScrapper {

        XmlWrapper getContent() throws XmlException;

        Set<Uri> getContentImages() throws XmlException;

        Set<Uri> getContentReferences() throws XmlException;

        Map<String, Object> getProperties() throws XmlException;

        String getTitle() throws XmlException;

    }

    private XmlWrapper fContentXml;

    private IInnerPageScrapper fFieldAccessor;

    public InnerPagePresenter(ISite site, RemoteResource resource)
        throws IOException,
        XmlException {
        super(site, resource);
        fFieldAccessor = newScrapper(this, IInnerPageScrapper.class);
    }

    public XmlWrapper getContent() throws XmlException, IOException {
        XmlWrapper xml = getExtractedContentElement();
        XmlWrapper div = xml.newCopy("html:div");
        localizeReferences(div, ".//html:a[@href]", "href");
        localizeReferences(div, ".//html:img", "src");
        return div;
    }

    public String getContentStr() throws XmlException, IOException {
        XmlWrapper xml = getExtractedContentElement();
        XmlWrapper div = xml.newCopy("html:div");
        Element tag = div.getRootElement();
        HtmlBurner burner = new HtmlBurner(new HtmlBurnerConfig() {

            @Override
            public boolean isExcludedAttribute(String name, Attr attr) {
                name = name.toLowerCase();
                if (TagDictionary.ATTR_STYLE.equals(name)) {
                    return true;
                }
                if (TagDictionary.ATTR_CLASS.equals(name)) {
                    String value = attr.getValue();
                    return !value.startsWith("umx_");
                }
                if ("_excluded".equals(name)) {
                    return true;
                }
                return super.isExcludedAttribute(name, attr);
            }

            @Override
            public boolean isExcludedTag(String name, Element element) {
                name = name.toLowerCase();
                if (TagDictionary.IFRAME.equals(name)) {
                    return false;
                }
                return super.isExcludedTag(name, element);
            }

            @Override
            public boolean keepIntact(String name, Element element) {
                String style = element.getAttribute(TagDictionary.ATTR_CLASS);
                if (style != null && style.startsWith("umx_")) {
                    return true;
                }
                return false;
            }
        });
        burner.burnHtml(tag);

        final Uri pagePath = getResourcePath();
        // localizeReferences(div, ".//html:a[@href]", "href");
        // localizeReferences(div, ".//html:img", "src");
        final StringBuilder buf = new StringBuilder();
        XmlAcceptor.accept(tag, new XmlVisitor() {

            private void print(String text) {
                buf.append(text);
            }

            @Override
            public void visit(Element node) {
                String name = XHTMLUtils.getHTMLName(node);
                print("<" + name);
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Attr attr = (Attr) attributes.item(i);
                    String attrName = XHTMLUtils.getHTMLName(attr);
                    if ("_excluded".equals(attrName)) {
                        continue;
                    }
                    String value = attr.getValue();
                    if (("a".equals(name) && "href".equals(attrName))
                        || ("img".equals(name) && "src".equals(attrName))) {
                        String localizedReference = localizeReference(
                            pagePath,
                            value);
                        if (localizedReference != null) {
                            value = localizedReference;
                        }
                    }
                    print(" " + attrName + "='" + value + "'");
                }
                print(">");
                super.visit(node);
                print("</" + name + ">");
            }

            @Override
            public void visit(Entity node) {
                String text = PageUtils.escapeXml(node.getTextContent());
                print(text);
            }

            @Override
            public void visit(Text node) {
                String text = PageUtils.escapeXml(node.getTextContent());
                print(text);
            }
        });
        return buf.toString();
    }

    private XmlWrapper getExtractedContentElement() throws XmlException {
        if (fContentXml == null) {
            fContentXml = fFieldAccessor.getContent();
        }
        return fContentXml;
    }

    @Override
    public IFormatter getFormatter() throws IOException {
        return newFormatter(this, IInnerPageFormatter.class);
    }

    @Override
    public Set<Uri> getImageReferences() throws XmlException, IOException {
        getExtractedContentElement();
        return fFieldAccessor.getContentImages();
    }

    @Override
    public Set<Uri> getPageReferences() throws XmlException, IOException {
        getExtractedContentElement();
        return fFieldAccessor.getContentReferences();
    }

    public String getPageTitle() throws XmlException {
        return fFieldAccessor.getTitle();
    }

    public JsonBookSection getSection() throws XmlException, IOException {
        JsonBookSection section = new JsonBookSection();
        String title = fFieldAccessor.getTitle();
        section.setTitle(title);
        String content = getContentStr();
        // XmlWrapper contentElement = getExtractedContentElement();
        section.setContent(content);
        Uri url = getResourceUrl();
        setFullUrl(section, url);

        JsonObject propObj = new JsonObject();
        section.setValue("properties", propObj);
        Map<String, Object> properties = fFieldAccessor.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            propObj.setValue(key, value);
        }

        return section;
    }

    protected String localizeReference(Uri pagePath, String ref) {
        try {
            Uri uri = new Uri(ref);
            IPresenter presenter = getPresenter(uri, false);
            if (presenter instanceof IContentPresenter) {
                Uri resourcePath = ((IContentPresenter) presenter)
                    .getResourcePath();
                resourcePath = pagePath.getRelative(resourcePath);
                ref = resourcePath.toString();
            } else {
                ref = null;
            }
            return ref;
        } catch (Throwable t) {
            throw onError(
                RuntimeException.class,
                "Can not localizer reference. Ref: '"
                    + ref
                    + "'. Page: '"
                    + pagePath
                    + "'.",
                t);
        }
    }

    private void localizeReferences(XmlWrapper xml, String xpath, String param)
        throws XmlException,
        IOException {
        Uri pagePath = getResourcePath();
        List<XmlWrapper> references = xml.evalList(xpath);
        for (XmlWrapper reference : references) {
            String ref = reference.getAttribute(param);
            ref = localizeReference(pagePath, ref);
            if (ref != null) {
                reference.setAttribute(param, ref);
            }
        }
    }

}
