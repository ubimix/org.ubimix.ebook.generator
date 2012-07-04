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
import org.webreformatter.scrapper.core.DownloadAdapter;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class RemoteResourceLoader {

    /**
     * @author kotelnikov
     */
    public static class RemoteResource {

        private AppContext fContext;

        private HttpStatusCode fDownloadStatus;

        private XmlWrapper fHtmlPage;

        private String fMimeType;

        private IWrfResource fResource;

        private Uri fUri;

        private XmlContext fXmlContext;

        public RemoteResource(
            AppContext appContext,
            XmlContext xmlContext,
            Uri uri) {
            fContext = appContext;
            fXmlContext = xmlContext;
            fUri = uri;
        }

        private void clean() {
            fMimeType = null;
            fDownloadStatus = null;
            fResource = null;
        }

        public HttpStatusCode download(boolean reload) throws IOException {
            if (fDownloadStatus == null || reload) {
                clean();
                IWrfResource resource = getWrfResource();
                DownloadAdapter downloadAdapter = fContext
                    .getAdapter(DownloadAdapter.class);
                fDownloadStatus = downloadAdapter.loadResource(fUri, resource);
            }
            return fDownloadStatus;
        }

        public InputStream getContent() throws IOException {
            IWrfResource resource = getWrfResource();
            IContentAdapter content = resource
                .getAdapter(IContentAdapter.class);
            return content.getContentInput();
        }

        public String getFileExtension() throws IOException {
            String mimeType = getMimeType();
            String extension = getUri().getPath().getFileExtension();
            if (extension == null) {
                final String imagePrefix = "image/";
                if (mimeType.startsWith(imagePrefix)) {
                    extension = mimeType.substring(imagePrefix.length());
                } else if (isHtmlPage()) {
                    extension = "html";
                } else {
                    extension = "bin";
                }
            }
            return extension;
        }

        public XmlWrapper getHtmlPage() throws IOException, XmlException {
            if (fHtmlPage == null) {
                IWrfResource resource = getWrfResource();
                HTMLAdapter adapter = resource.getAdapter(HTMLAdapter.class);
                XmlWrapper xml = adapter.getWrapper();
                fHtmlPage = fXmlContext.wrap(xml.getRoot()).createCopy();
            }
            return fHtmlPage;
        }

        public String getMimeType() throws IOException {
            if (fMimeType == null) {
                IWrfResource resource = getWrfResource();
                MimeTypeAdapter mimeAdapter = resource
                    .getAdapter(MimeTypeAdapter.class);
                fMimeType = mimeAdapter.getMimeType();
            }
            return fMimeType;
        }

        public HttpStatusCode getStatus() {
            return fDownloadStatus;
        }

        public Uri getUri() {
            return fUri;
        }

        private IWrfResource getWrfResource() {
            if (fResource == null) {
                fResource = fContext.getResource("download", fUri);
            }
            return fResource;
        }

        public boolean isHtmlPage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("text/html");
        }

        public boolean isImage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("image/");
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

    private Map<Uri, RemoteResourceLoader.RemoteResource> fResources = new HashMap<Uri, RemoteResourceLoader.RemoteResource>();

    private XmlContext fXmlContext;

    public RemoteResourceLoader(AppContext appContext) {
        this(appContext, newXmlContext());
    }

    public RemoteResourceLoader(AppContext appContext, XmlContext xmlContext) {
        fContext = appContext;
        fXmlContext = xmlContext;
    }

    public RemoteResourceLoader.RemoteResource download(Uri indexUri)
        throws IOException {
        RemoteResourceLoader.RemoteResource resource = getResource(
            indexUri,
            true);
        resource.download(false);
        return resource;
    }

    public RemoteResourceLoader.RemoteResource getResource(
        Uri uri,
        boolean create) {
        RemoteResourceLoader.RemoteResource resource = fResources.get(uri);
        if (resource == null && create) {
            resource = new RemoteResource(fContext, fXmlContext, uri);
            fResources.put(uri, resource);
        }
        return resource;
    }
}