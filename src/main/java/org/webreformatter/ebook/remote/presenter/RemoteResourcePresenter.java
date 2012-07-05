package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.digests.Sha1Digest;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.BookId;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.ISite;

/**
 * @author kotelnikov
 */
public abstract class RemoteResourcePresenter extends Base
    implements
    IPresenter {

    public static class RemoteBookExeption extends IOException {
        private static final long serialVersionUID = 2042430338643774688L;

        public RemoteBookExeption(String message, Throwable cause) {
            super(message, cause);
        }

    }

    private final static Logger log = Logger
        .getLogger(RemoteResourcePresenter.class.getName());

    @SuppressWarnings("unchecked")
    public static <T extends Exception> T onError(
        Class<T> type,
        String msg,
        Throwable t) {
        if (type.isInstance(t)) {
            return (T) t;
        }
        log.log(Level.FINE, msg, t);
        try {
            Constructor<T> constructor = type.getConstructor(
                String.class,
                Throwable.class);
            return constructor.newInstance(msg, t);
        } catch (Throwable x) {
            throw new RuntimeException(msg, t);
        }
    }

    public static RemoteBookExeption onError(String msg, Throwable t) {
        return onError(RemoteBookExeption.class, msg, t);
    }

    public static Uri removeFragment(Uri url) {
        if (url.getFragment() != null) {
            url = url.getBuilder().setFragment(null).build();
        }
        return url;
    }

    protected final RemoteResource fResource;

    private BookId fResourceId;

    private ISite fSite;

    public RemoteResourcePresenter(ISite site, RemoteResource resource) {
        super();
        fSite = site;
        fResource = resource;
    }

    protected String getHash(Uri url) {
        String hash = Sha1Digest
            .builder()
            .update(url.toString())
            .build()
            .toString();
        return hash;
    }

    protected BookId getIdByUrl(Uri url) {
        String hash = getHash(url);
        String ref = "uri:sha1:" + hash;
        BookId result = new BookId(ref);
        return result;
    }

    public String getMediaType() throws IOException {
        return fResource.getMimeType();
    }

    public Uri getPathToResource(IContentPresenter resourcePresenter)
        throws IOException {
        Uri resourcePath = resourcePresenter.getResourcePath();
        Uri thisPath = getResourcePath();
        Uri path = thisPath.getRelative(resourcePath);
        return path;
    }

    public Uri getPathToResource(Uri uri) throws IOException, XmlException {
        IPresenter resourcePresenter = getPresenter(uri, false);
        if (resourcePresenter instanceof IContentPresenter) {
            uri = getPathToResource((IContentPresenter) resourcePresenter);
        }
        return uri;
    }

    public Uri getResolved(String href, boolean removeFragment) {
        Uri resourceUrl = getResourceUrl();
        if (href.startsWith("//")) {
            String scheme = resourceUrl.getScheme();
            if (scheme != null && !"".equals(scheme)) {
                href = scheme + ":" + href;
            }
        }
        Uri url = resourceUrl.getResolved(href);
        if (removeFragment) {
            url = removeFragment(url);
        }
        return url;
    }

    protected Uri getResolved(Uri href, boolean removeFragment) {
        Uri resourceUrl = getResourceUrl();
        if (href.hasAuthority() && "".equals(href.getScheme())) {
            href = href.getBuilder().setScheme(resourceUrl.getScheme()).build();
        }
        Uri url = resourceUrl.getResolved(href);
        if (removeFragment && url.getFragment() != null) {
            url = removeFragment(url);
        }
        return url;
    }

    public BookId getResourceId() {
        if (fResourceId == null) {
            fResourceId = getIdByUrl(fResource.getUri());
        }
        return fResourceId;
    }

    public Uri getResourcePath() throws IOException {
        String type = getResourcePathFolder();
        Uri uri = getResourceUrl();
        String hash = getHash(uri);
        String ext = fResource.getFileExtension();
        Uri result = new Uri("OPS/" + type + "/" + hash + "." + ext);
        return result;
    }

    protected String getResourcePathFolder() {
        return "";
    }

    @Override
    public Uri getResourceUrl() {
        return fResource.getUri();
    }

    @Override
    public ISite getSite() {
        return fSite;
    }

    public boolean isValid() throws IOException {
        return fResource.getStatus().isOkOrNotModified();
    }

}