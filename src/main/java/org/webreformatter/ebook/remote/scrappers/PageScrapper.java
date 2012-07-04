/**
 * 
 */
package org.webreformatter.ebook.remote.scrappers;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.BookId;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public abstract class PageScrapper implements IScrapper {

    /**
     * @author kotelnikov
     */
    public interface IUrlProvider {

        Uri getImageUri(Uri parentUri, Uri resourceUri);

        Uri getResourceUri(Uri parentUri, Uri resourceUri);

    }

    public static abstract class UrlProvider implements IUrlProvider {

        @Override
        public Uri getImageUri(Uri parentUri, Uri resourceUri) {
            resourceUri = getNormalizedDownloadUri(parentUri, resourceUri);
            return resourceUri;
        }

        protected Uri getNormalizedDownloadUri(Uri parentUri, Uri resourceUri) {
            Uri.Builder builder = resourceUri.getBuilder().setFragment(null);
            String scheme = builder.getScheme();
            if (parentUri != null && builder.hasAuthority() && scheme == null) {
                builder.setScheme(parentUri.getScheme());
            }
            resourceUri = builder.build();
            return resourceUri;
        }

    }

    private XmlWrapper fPage;

    protected RemotePagePresenter fPresenter;

    protected IUrlProvider fUrlProvider;

    public PageScrapper(IUrlProvider urlProvider, RemotePagePresenter presenter) {
        fUrlProvider = urlProvider;
        fPresenter = presenter;
    }

    public abstract XmlWrapper getContent() throws XmlException;

    public Set<Uri> getContentImages() throws XmlException {
        return getReferences(getContent(), ".//html:img", "src");
    }

    public Set<Uri> getContentReferences() throws XmlException {
        return getReferences(getContent(), ".//html:a[@href]", "href");
    }

    public XmlWrapper getPage() throws XmlException {
        if (fPage == null) {
            fPage = fPresenter.getHtmlPage();
            resolveReferences(fPage, ".//html:img", "src", true);
            resolveReferences(fPage, ".//html:a[@href]", "href", false);
            resolveReferences(fPage, ".//html:link[@href]", "href", false);
            resolveReferences(fPage, ".//html:script[@src]", "src", false);
        }
        return fPage;
    }

    public Map<String, Object> getProperties() throws XmlException {
        Map<String, Object> properties = new HashMap<String, Object>();
        Uri url = fPresenter.getResourceUrl();
        properties.put("url", url + "");
        XmlWrapper page = getPage();
        String title = page.evalStr("//html:title");
        if (title != null) {
            properties.put("title", title);
        }
        BookId id = fPresenter.getResourceId();
        if (id != null) {
            properties.put("id", id + "");
        }
        List<XmlWrapper> metaTags = page.evalList("//html:meta[@name]");
        for (XmlWrapper meta : metaTags) {
            String name = meta.getAttribute("name");
            String value = meta.getAttribute("content");
            if (name != null && value != null) {
                properties.put(name, value);
            }
        }
        return properties;
    }

    protected Set<Uri> getReferences(XmlWrapper xml, String xpath, String param)
        throws XmlException {
        Set<Uri> result = new LinkedHashSet<Uri>();
        if (xml != null) {
            List<XmlWrapper> references = xml.evalList(xpath);
            for (XmlWrapper reference : references) {
                String excluded = reference.getAttribute("_excluded");
                if (!"true".equals(excluded)) {
                    String ref = reference.getAttribute(param);
                    Uri uri = new Uri(ref);
                    result.add(uri);
                }
            }
        }
        return result;
    }

    private void resolveReferences(
        XmlWrapper xml,
        String xpath,
        String param,
        boolean image) throws XmlException {
        Uri parentUri = fPresenter.getResourceUrl();
        List<XmlWrapper> references = xml.evalList(xpath);
        for (XmlWrapper reference : references) {
            String ref = reference.getAttribute(param);
            Uri uri = fPresenter.getResolved(ref, true);
            if (uri != null) {
                Uri newUri = image
                    ? fUrlProvider.getImageUri(parentUri, uri)
                    : fUrlProvider.getResourceUri(parentUri, uri);
                boolean excluded = false;
                if (newUri != null) {
                    uri = newUri;
                } else {
                    excluded = true;
                }
                reference.setAttribute(param, uri.toString());
                if (excluded) {
                    reference.setAttribute("_excluded", "true");
                }
            }
        }
    }

}
