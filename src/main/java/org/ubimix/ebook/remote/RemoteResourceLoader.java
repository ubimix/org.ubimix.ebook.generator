package org.ubimix.ebook.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ubimix.commons.io.IOUtil;
import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.commons.uri.Path;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.uri.UriToPath;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.commons.xml.XmlWrapper.CompositeNamespaceContext;
import org.ubimix.commons.xml.XmlWrapper.SimpleNamespaceContext;
import org.ubimix.commons.xml.XmlWrapper.XmlContext;
import org.ubimix.resources.IContentAdapter;
import org.ubimix.resources.IWrfRepository;
import org.ubimix.resources.IWrfResource;
import org.ubimix.resources.IWrfResourceProvider;
import org.ubimix.resources.adapters.cache.CachedResourceAdapter;
import org.ubimix.resources.adapters.cache.DateUtil;
import org.ubimix.resources.adapters.html.HTMLAdapter;
import org.ubimix.resources.adapters.mime.MimeTypeAdapter;
import org.ubimix.resources.impl.WrfResourceRepository;
import org.ubimix.scraper.app.AbstractConfig;
import org.ubimix.scraper.core.AccessConfig;
import org.ubimix.scraper.core.IAccessConfig.ICredentials;
import org.ubimix.scraper.protocol.AccessManager;
import org.ubimix.scraper.protocol.AccessManager.CredentialInfo;
import org.ubimix.scraper.protocol.CompositeProtocolHandler;
import org.ubimix.scraper.protocol.HttpStatusCode;
import org.ubimix.scraper.protocol.ProtocolHandlerUtils;

/**
 * @author kotelnikov
 */
public class RemoteResourceLoader extends AbstractConfig {

    /**
     * @author kotelnikov
     */
    public static class RemoteResource {

        private HttpStatusCode fDownloadStatus;

        private XmlWrapper fHtmlPage;

        private String fMimeType;

        private IWrfResource fResource;

        private RemoteResourceLoader fResourceLoader;

        private Uri fUri;

        private XmlContext fXmlContext;

        public RemoteResource(
            RemoteResourceLoader loader,
            XmlContext xmlContext,
            Uri uri) {
            fUri = uri;
            fResourceLoader = loader;
            fXmlContext = xmlContext;
        }

        public RemoteResource(Uri uri) {
            fUri = uri;
        }

        protected void clean() {
            fMimeType = null;
            fDownloadStatus = null;
            fResource = null;
        }

        public HttpStatusCode download(boolean reload) throws IOException {
            if (fDownloadStatus == null || reload) {
                clean();
                IWrfResource resource = getWrfResource();
                Uri uri = getUri();
                fDownloadStatus = fResourceLoader.loadResource(
                    uri,
                    resource,
                    reload);
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
                download(false);
                fHtmlPage = loadContentAsHtml();
            }
            return fHtmlPage;
        }

        public String getMimeType() throws IOException {
            if (fMimeType == null) {
                download(false);
                fMimeType = loadMimeType();
            }
            return fMimeType;
        }

        public HttpStatusCode getStatus() throws IOException {
            download(false);
            return fDownloadStatus;
        }

        public Uri getUri() {
            return fUri;
        }

        private IWrfResource getWrfResource() {
            if (fResource == null) {
                fResource = fResourceLoader.getResource(
                    "download",
                    getUri(),
                    null);
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

        protected XmlWrapper loadContentAsHtml()
            throws IOException,
            XmlException {
            IWrfResource resource = getWrfResource();
            HTMLAdapter adapter = resource.getAdapter(HTMLAdapter.class);
            XmlWrapper xml = adapter.getWrapper();
            return fXmlContext.wrap(xml.getRoot()).createCopy();
        }

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

    private AccessManager fAccessManager = new AccessManager();

    private CompositeProtocolHandler fProtocolHandler = new CompositeProtocolHandler();

    private IWrfRepository fResourceRepository;

    private Map<Uri, RemoteResource> fResources = new HashMap<Uri, RemoteResource>();

    private XmlContext fXmlContext;

    public RemoteResourceLoader(IVariableProvider propertyProvider)
        throws IOException {
        this(propertyProvider, null, newXmlContext());
        ProtocolHandlerUtils.registerDefaultProtocols(fProtocolHandler);
    }

    public RemoteResourceLoader(
        IVariableProvider propertyProvider,
        IWrfRepository repository,
        XmlContext xmlContext) throws IOException {
        super(propertyProvider);
        fResourceRepository = repository != null
            ? repository
            : newResourceRepository();
        fXmlContext = xmlContext;
        initCredentials();
    }

    /**
     * This method is used to associate credentials with all resources starting
     * with the specified base URL.
     * 
     * @param baseUrl the basic URL associated with the specified credentials
     * @param login the login used to access resources
     * @param pwd the password associated with the specified login
     */
    public void addCredentials(Uri baseUrl, String login, String pwd) {
        fAccessManager.setCredentials(baseUrl, new CredentialInfo(login, pwd));
    }

    public RemoteResource download(Uri indexUri) throws IOException {
        RemoteResource resource = getResource(indexUri, true);
        resource.download(false);
        return resource;
    }

    private IWrfResource getResource(String storeName, Uri url, String suffix) {
        IWrfResourceProvider store = fResourceRepository.getResourceProvider(
            storeName,
            true);
        // Transform the full URL into a path
        Path targetResultPath = UriToPath.getPath(url);
        if (suffix != null) {
            Path.Builder pathBuilder = targetResultPath.getBuilder();
            pathBuilder.appendPath("$").appendPath(suffix);
            targetResultPath = pathBuilder.build();
        }
        IWrfResource targetResource = store.getResource(targetResultPath, true);
        return targetResource;
    }

    public RemoteResource getResource(Uri uri, boolean create) {
        RemoteResource resource = fResources.get(uri);
        if (resource == null && create) {
            resource = new RemoteResource(this, fXmlContext, uri);
            fResources.put(uri, resource);
        }
        return resource;
    }

    protected void initCredentials() throws IOException {
        String accessConfigFileName = getConfigString(
            "accessConfig",
            "./config/access.json");
        if (accessConfigFileName != null) {
            File configFile = new File(accessConfigFileName);
            String serializedConfig = IOUtil.readString(configFile);
            AccessConfig accessConfig = AccessConfig.FACTORY
                .newValue(serializedConfig);
            final Set<Uri> internalUrls = new HashSet<Uri>();
            List<ICredentials> credentials = accessConfig.getCredentials();
            for (ICredentials credential : credentials) {
                Uri url = credential.getBaseDomain();
                internalUrls.add(url);
                addCredentials(
                    url,
                    credential.getLogin(),
                    credential.getPassword());
            }
        }
    }

    private HttpStatusCode loadResource(
        Uri url,
        IWrfResource resource,
        boolean reload) throws IOException {
        HttpStatusCode statusCode = HttpStatusCode.STATUS_404;
        if (url != null) {
            boolean download = reload
                || !resource.getAdapter(IContentAdapter.class).exists();
            CachedResourceAdapter cacheAdapter = resource
                .getAdapter(CachedResourceAdapter.class);
            if (!download) {
                int code = cacheAdapter.getStatusCode();
                statusCode = HttpStatusCode.getStatusCode(code);
            } else if (!cacheAdapter.isExpired()) {
                statusCode = HttpStatusCode.STATUS_304; /* NOT_MODIFIED */
            } else {
                CredentialInfo credentials = fAccessManager != null
                    ? fAccessManager.getCredentials(url)
                    : null;
                String login = null;
                String password = null;
                if (credentials != null) {
                    login = credentials.getLogin();
                    password = credentials.getPassword();
                }
                statusCode = fProtocolHandler.handleRequest(
                    url,
                    login,
                    password,
                    resource);
                cacheAdapter.setStatusCode(statusCode.getStatusCode());
            }
        }
        return statusCode;
    }

    protected IWrfRepository newResourceRepository() {
        String repositoryPath = getConfigString(
            "repositoryPath",
            "${workdir}/.data");
        boolean reset = getConfigBoolean("resetRepository", false);
        IWrfRepository repository = WrfResourceRepository.newRepository(
            new File(repositoryPath),
            reset);
        long expirationTimeout = getConfigLong(
            "downloadExpirationTimeout",
            DateUtil.MIN * 1);
        CachedResourceAdapter.setRefreshTimeout(expirationTimeout);
        CachedResourceAdapter.setExpirationTimeout(expirationTimeout);
        return repository;

    }
}