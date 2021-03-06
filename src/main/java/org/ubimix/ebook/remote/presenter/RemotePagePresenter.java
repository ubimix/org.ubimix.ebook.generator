package org.ubimix.ebook.remote.presenter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.ubimix.commons.json.JsonObject;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.commons.xml.XmlWrapper.CompositeNamespaceContext;
import org.ubimix.commons.xml.XmlWrapper.SimpleNamespaceContext;
import org.ubimix.commons.xml.XmlWrapper.XmlContext;
import org.ubimix.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.ubimix.ebook.remote.Site;

/**
 * @author kotelnikov
 */
public abstract class RemotePagePresenter extends RemoteResourcePresenter {

    public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

    public static final String _PREFIX_XHTML = "html";

    public static final String ATTR_EXCLUDED = "_excluded";

    private static SimpleNamespaceContext HTML_NAMESPACE_CONTEXT = new SimpleNamespaceContext(
        _PREFIX_XHTML,
        _NS_XHTML);

    public static void checkAtomNamespaces(XmlContext context) {
        CompositeNamespaceContext namespaceContext = context
            .getNamespaceContext();
        namespaceContext.addContext(HTML_NAMESPACE_CONTEXT);
    }

    public static Uri getFullUrl(JsonObject obj) {
        String str = obj.getString("_fullUrl");
        return str != null ? new Uri(str) : null;
    }

    public static boolean isExcludedAttribute(String attrName) {
        return ATTR_EXCLUDED.equals(attrName);
    }

    public static void setFullUrl(JsonObject obj, Uri uri) {
        obj.setValue("_fullUrl", uri);
    }

    private String fFileName;

    private XmlWrapper fPage;

    public RemotePagePresenter(
        Site site,
        RemoteResource resource,
        Uri resourceUri) throws IOException, XmlException {
        super(site, resource, resourceUri);
    }

    protected String getFileName() throws IOException {
        if (fFileName == null) {
            Uri uri = getResourceUrl();
            String hash = getHash(uri);
            String ext = fResource.getFileExtension();
            fFileName = hash + "." + ext;
        }
        return fFileName;
    }

    public XmlWrapper getHtmlPage() throws IOException, XmlException {
        if (fPage == null) {
            fPage = fResource.getHtmlPage();
            XmlContext context = fPage.getXmlContext();
            checkAtomNamespaces(context);
            resolveReferences(fPage, ".//html:img", "src", true);
            resolveReferences(fPage, ".//html:a[@href]", "href", false);
            resolveReferences(fPage, ".//html:link[@href]", "href", false);
            resolveReferences(fPage, ".//html:script[@src]", "src", false);
        }
        return fPage;
    }

    public abstract Set<Uri> getImageReferences()
        throws XmlException,
        IOException;

    public abstract Set<Uri> getPageReferences()
        throws XmlException,
        IOException;

    @Override
    public Uri getResourcePath() throws IOException {
        String path = getResourcePathFolder();
        String fileName = getFileName();
        Uri result = new Uri(path + fileName);
        return result;
    }

    public String getResourcePathFolder() {
        return "OPS/";
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

    protected void localizeReferences(XmlWrapper xml, String xpath, String param)
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

    private void resolveReferences(
        XmlWrapper xml,
        String xpath,
        String param,
        boolean image) throws XmlException {
        Uri parentUri = getResourceUrl();
        List<XmlWrapper> references = xml.evalList(xpath);
        for (XmlWrapper reference : references) {
            String ref = reference.getAttribute(param);
            Uri uri = getResolved(ref, true);
            if (uri != null) {
                Site site = getSite();
                Uri newUri = image ? site.getImageUri(parentUri, uri) : site
                    .getResourceUri(parentUri, uri);
                boolean excluded = false;
                if (newUri != null) {
                    uri = newUri;
                } else {
                    excluded = true;
                }
                reference.setAttribute(param, uri.toString());
                if (excluded) {
                    reference.setAttribute(ATTR_EXCLUDED, "true");
                }
            }
        }
    }

    public void setFileName(String fileName) {
        fFileName = fileName;
    }

}