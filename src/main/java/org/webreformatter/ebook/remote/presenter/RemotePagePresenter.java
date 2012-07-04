package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.Set;

import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.CompositeNamespaceContext;
import org.webreformatter.commons.xml.XmlWrapper.SimpleNamespaceContext;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;

/**
 * @author kotelnikov
 */
public abstract class RemotePagePresenter extends RemoteResourcePresenter {

    public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

    public static final String _PREFIX_XHTML = "html";

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

    public static void setFullUrl(JsonObject obj, Uri uri) {
        obj.setValue("_fullUrl", uri);
    }

    protected final XmlWrapper fPage;

    public RemotePagePresenter(
        ISite site,
        RemoteResource resource) throws IOException, XmlException {
        super(site, resource);
        fPage = fResource.getHtmlPage();
        XmlContext context = fPage.getXmlContext();
        checkAtomNamespaces(context);
    }

    public XmlWrapper getHtmlPage() {
        return fPage;
    }

    public abstract Set<Uri> getImageReferences()
        throws XmlException,
        IOException;

    public abstract Set<Uri> getPageReferences()
        throws XmlException,
        IOException;

    @Override
    protected String getResourcePathFolder() {
        return "html";
    }

}