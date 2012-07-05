package org.webreformatter.ebook.remote;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.CompositeNamespaceContext;
import org.webreformatter.commons.xml.XmlWrapper.SimpleNamespaceContext;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.resources.IContentAdapter;
import org.webreformatter.resources.IWrfResource;
import org.webreformatter.resources.adapters.html.HTMLAdapter;
import org.webreformatter.resources.adapters.mime.MimeTypeAdapter;
import org.webreformatter.scrapper.core.AppContext;
import org.webreformatter.scrapper.core.AppContextConfigurator;
import org.webreformatter.scrapper.core.DownloadAdapter;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class RemoteResourceLoader implements IRemoteResourceLoader {

    /**
     * @author kotelnikov
     */
    public static class PersistentResource extends RemoteResource {

        private AppContext fContext;

        private IWrfResource fResource;

        private XmlContext fXmlContext;

        public PersistentResource(
            AppContext appContext,
            XmlContext xmlContext,
            Uri uri) {
            super(uri);
            fXmlContext = xmlContext;
            fContext = appContext;
        }

        @Override
        protected void clean() {
            super.clean();
            fResource = null;
        }

        @Override
        protected HttpStatusCode doDownload() throws IOException {
            IWrfResource resource = getWrfResource();
            DownloadAdapter downloadAdapter = fContext
                .getAdapter(DownloadAdapter.class);
            return downloadAdapter.loadResource(getUri(), resource);
        }

        @Override
        public InputStream getContent() throws IOException {
            IWrfResource resource = getWrfResource();
            IContentAdapter content = resource
                .getAdapter(IContentAdapter.class);
            return content.getContentInput();
        }

        private IWrfResource getWrfResource() {
            if (fResource == null) {
                fResource = fContext.getResource("download", getUri());
            }
            return fResource;
        }

        @Override
        public boolean isHtmlPage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("text/html");
        }

        @Override
        public boolean isImage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("image/");
        }

        @Override
        protected XmlWrapper loadContentAsHtml()
            throws IOException,
            XmlException {
            IWrfResource resource = getWrfResource();
            HTMLAdapter adapter = resource.getAdapter(HTMLAdapter.class);
            XmlWrapper xml = adapter.getWrapper();
            return fXmlContext.wrap(xml.getRoot()).createCopy();
        }

        @Override
        protected String loadMimeType() throws IOException {
            IWrfResource resource = getWrfResource();
            MimeTypeAdapter mimeAdapter = resource
                .getAdapter(MimeTypeAdapter.class);
            return mimeAdapter.getMimeType();
        }
    }

    public static final String _NS_XHTML = "http://www.w3.org/1999/xhtml";

    public static final String _PREFIX_XHTML = "html";

    private static SimpleNamespaceContext HTML_NAMESPACE_CONTEXT = new SimpleNamespaceContext(
        _PREFIX_XHTML,
        _NS_XHTML);

    public static void checkHtmlNamespaces(XmlContext context) {
        CompositeNamespaceContext namespaceContext = context
            .getNamespaceContext();
        namespaceContext.addContext(HTML_NAMESPACE_CONTEXT);
    }

    public static XmlContext newXmlContext() {
        XmlContext xmlContext = XmlContext.builder().build();
        checkHtmlNamespaces(xmlContext);
        return xmlContext;
    }

    private AppContext fContext;

    private Map<Uri, RemoteResourceLoader.PersistentResource> fResources = new HashMap<Uri, RemoteResourceLoader.PersistentResource>();

    private XmlContext fXmlContext;

    public RemoteResourceLoader() throws IOException {
        this(AppContextConfigurator.createAppContext());
    }

    public RemoteResourceLoader(AppContext appContext) {
        this(appContext, newXmlContext());
    }

    public RemoteResourceLoader(AppContext appContext, XmlContext xmlContext) {
        fContext = appContext;
        fXmlContext = xmlContext;
    }

    public RemoteResourceLoader.PersistentResource download(Uri indexUri)
        throws IOException {
        RemoteResourceLoader.PersistentResource resource = getResource(
            indexUri,
            true);
        resource.download(false);
        return resource;
    }

    @Override
    public RemoteResourceLoader.PersistentResource getResource(
        Uri uri,
        boolean create) {
        RemoteResourceLoader.PersistentResource resource = fResources.get(uri);
        if (resource == null && create) {
            resource = new PersistentResource(fContext, fXmlContext, uri);
            fResources.put(uri, resource);
        }
        return resource;
    }
}