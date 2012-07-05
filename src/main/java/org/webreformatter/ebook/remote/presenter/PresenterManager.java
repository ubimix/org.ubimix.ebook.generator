package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.IRemoteResourceLoader;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.apps.xwikiepub.XWikiSite;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.IUrlProvider;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class PresenterManager implements IPresenterManager {

    private final static Logger log = Logger.getLogger(XWikiSite.class
        .getName());

    private Map<Uri, IPresenter> fPresenters = new HashMap<Uri, IPresenter>();

    private List<Uri> fPresentersUrls = new ArrayList<Uri>();

    protected Uri fResourceBaseUrl;

    private ISite fSite;

    private IUrlProvider fUrlProvider;

    public PresenterManager(
        ISite site,
        IUrlProvider urlProvider,
        Uri localResources) throws IOException {
        fSite = site;
        fUrlProvider = urlProvider;
        fResourceBaseUrl = localResources;
    }

    private void addPresenter(Uri resourceUri, IPresenter presenter) {
        boolean add = !fPresenters.containsKey(resourceUri);
        fPresenters.put(resourceUri, presenter);
        if (add) {
            fPresentersUrls.add(resourceUri);
        }
    }

    @Override
    public IPresenter getPresenter(Uri resourceUri, boolean create)
        throws IOException,
        XmlException {
        resourceUri = RemoteResourcePresenter.removeFragment(resourceUri);
        IPresenter presenter = getResourcePresenter(resourceUri);
        if (presenter == null && create) {
            IRemoteResourceLoader resourceLoader = fSite.getResourceLoader();
            RemoteResource resource = resourceLoader.getResource(
                resourceUri,
                true);
            HttpStatusCode status = resource.download(shouldReload());
            if (status.isOkOrNotModified()) {
                presenter = newPresenter(resource);
                addPresenter(resourceUri, presenter);
            } else {
                log.log(Level.FINE, "Resource was not loaded. URL: '"
                    + resourceUri
                    + "'. Status: "
                    + status);
            }
        }
        return presenter;
    }

    @Override
    public Iterator<Uri> getPresenterUrls() {
        return new Iterator<Uri>() {

            private int fPosition;

            @Override
            public boolean hasNext() {
                return fPosition < fPresentersUrls.size();
            }

            @Override
            public Uri next() {
                if (!hasNext()) {
                    return null;
                }
                Uri uri = fPresentersUrls.get(fPosition);
                fPosition++;
                return uri;
            }

            @Override
            public void remove() {
            }
        };
    }

    private IPresenter getResourcePresenter(Uri resourceUri) {
        IPresenter presenter = fPresenters.get(resourceUri);
        return presenter;
    }

    private boolean isResourceUrl(Uri resourceUri) {
        String str = resourceUri.toString();
        if (str.startsWith(fResourceBaseUrl.toString())) {
            return true;
        }
        return false;
    }

    protected IPresenter newPresenter(RemoteResource resource)
        throws IOException,
        XmlException {
        Uri resourceUri = resource.getUri();

        // FIXME: remove this message
        System.out.println("Download '" + resourceUri + "'.");

        IPresenter presenter;
        if (isResourceUrl(resourceUri)) {
            presenter = new LocalResourcePresenter(
                fSite,
                resource,
                fResourceBaseUrl);
        } else if (resource.isHtmlPage()) {
            Uri siteUrl = fSite.getSiteUrl();
            if (siteUrl.equals(resourceUri)) {
                presenter = new IndexPagePresenter(
                    fSite,
                    resource,
                    fUrlProvider);
            } else {
                presenter = new InnerPagePresenter(
                    fSite,
                    resource,
                    fUrlProvider);
            }
        } else if (resource.isImage()) {
            presenter = new ImagePresenter(fSite, resource);
        } else {
            presenter = new BinaryResourcePresenter(fSite, resource);
        }
        return presenter;
    }

    protected boolean shouldReload() {
        return false;
    }

}