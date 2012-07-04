package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.RemoteResourceLoader;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.apps.xwikiepub.XWikiSite;
import org.webreformatter.scrapper.core.AppContext;
import org.webreformatter.scrapper.core.AppContextConfigurator;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class PresenterManager implements IPresenterManager {

    private final static Logger log = Logger.getLogger(XWikiSite.class
        .getName());

    private Map<Uri, IPresenter> fPresenters = new HashMap<Uri, IPresenter>();

    private List<Uri> fPresentersUrls = new ArrayList<Uri>();

    private RemoteResourceLoader fReader;

    protected Uri fResourceBaseUrl;

    private ISite fSite;

    public PresenterManager(ISite site, Uri localResources) throws IOException {
        fSite = site;
        fResourceBaseUrl = localResources;
        AppContext appContext = AppContextConfigurator
            .createAppContext(new IVariableProvider() {
                @Override
                public String getValue(String name) {
                    return System.getProperty(name);
                }
            });
        fReader = new RemoteResourceLoader(appContext);
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
            RemoteResource resource = fReader.getResource(resourceUri, true);
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
                presenter = new IndexPagePresenter(fSite, resource);
            } else {
                presenter = new InnerPagePresenter(fSite, resource);
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