/**
 * 
 */
package org.ubimix.ebook.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ubimix.commons.strings.StringUtil;
import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.commons.templates.ITemplateProcessor;
import org.ubimix.commons.templates.ITemplateProvider;
import org.ubimix.commons.templates.TemplateException;
import org.ubimix.commons.templates.providers.FileResourceProvider;
import org.ubimix.commons.templates.velocity.VelocityTemplateProcessor;
import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.formatters.IFormatterFactory;
import org.ubimix.ebook.remote.formatters.TemplateBasedFormatterFactory;
import org.ubimix.ebook.remote.presenter.IPresenterManager;
import org.ubimix.ebook.remote.presenter.PresenterManager;
import org.ubimix.ebook.remote.scrapers.IScraperFactory;
import org.ubimix.ebook.remote.scrapers.xwiki.XWikiScraperFactory;
import org.ubimix.scraper.app.AbstractConfig;

/**
 * @author kotelnikov
 */
public class Site extends AbstractConfig {

    private final static Logger log = Logger.getLogger(Site.class.getName());

    public static Set<Uri> getUrls(String... urls) {
        Set<Uri> result = new LinkedHashSet<Uri>();
        for (String url : urls) {
            Uri u = new Uri(url);
            result.add(u);
        }
        return result;
    }

    public static IOException handleError(String msg, Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        }
        log.log(Level.WARNING, msg, t);
        return new IOException(msg, t);
    }

    private List<String> fFirstCircleUrlPrefixes = new ArrayList<String>();

    private Boolean fForceDownload;

    private IFormatterFactory fFormatterFactory;

    private Uri fLocalResourceBaseUrl;

    private IPresenterManager fPresenterManager;

    private RemoteResourceLoader fResourceLoader;

    private File fResources;

    private IScraperFactory fScraperFactory;

    private List<String> fSecondCircleUrlPrefixes = new ArrayList<String>();

    private Uri fSiteUrl;

    private ITemplateProcessor fTemplateProcessor;

    public Site(IVariableProvider propertyProvider) throws IOException {
        super(propertyProvider);
        try {
            fPropertyProvider = propertyProvider;
            fResources = new File(getConfigValue("templatesRoot"));
            ITemplateProvider templateProvider = new FileResourceProvider(
                fResources);
            Properties properties = new Properties();
            fTemplateProcessor = new VelocityTemplateProcessor(
                templateProvider,
                properties);

            Collection<String> firstLevel = Arrays
                .asList(getConfigValue("siteBaseUrl"));
            addSiteUrlPrefixes(firstLevel, null);

            Uri siteIndexUrl = new Uri(getConfigValue("siteIndexUrl"));
            init(siteIndexUrl);
        } catch (TemplateException e) {
            throw handleError("Can not initialize template engine site", e);
        }
    }

    private void add(List<String> list, Collection<String> newPrefixes) {
        if (newPrefixes != null) {
            Set<String> set = new LinkedHashSet<String>(list);
            set.addAll(newPrefixes);
            list.clear();
            list.addAll(set);
        }
    }

    public void addSiteUrlPrefixes(
        Collection<String> firstLevel,
        Collection<String> secondLevel) {
        add(fFirstCircleUrlPrefixes, firstLevel);
        add(fSecondCircleUrlPrefixes, secondLevel);
    }

    public boolean forceResourceDownload(Uri downloadUri) {
        if (fForceDownload == null) {
            fForceDownload = getConfigBoolean("downloadExisting", false);
        }
        return fForceDownload;
    }

    protected String getConfigValue(String key) {
        return StringUtil.resolvePropertyByKey(key, fPropertyProvider);
    }

    public IFormatterFactory getFormatterFactory() throws IOException {
        if (fFormatterFactory == null) {
            fFormatterFactory = newFormatterFactory();
        }
        return fFormatterFactory;
    }

    public Uri getImageUri(Uri parentUri, Uri resourceUri) {
        resourceUri = getNormalizedDownloadUri(parentUri, resourceUri);
        return resourceUri;
    }

    public Uri getLocalResourceBaseUrl() {
        if (fLocalResourceBaseUrl == null) {
            fLocalResourceBaseUrl = new Uri(fResources.toURI() + "");
        }
        return fLocalResourceBaseUrl;
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

    public IPresenterManager getPresenterManager() throws IOException {
        if (fPresenterManager == null) {
            fPresenterManager = newPresenterManager();
        }
        return fPresenterManager;
    }

    public Uri getRemoteResourceUrl(Uri resourceUri) {
        return resourceUri;
    }

    public RemoteResourceLoader getResourceLoader() throws IOException {
        if (fResourceLoader == null) {
            fResourceLoader = newResourceLoader();
        }
        return fResourceLoader;
    }

    public Uri getResourceUri(Uri parentUri, Uri resourceUri) {
        resourceUri = getNormalizedDownloadUri(parentUri, resourceUri);
        boolean include = isInFirstCercle(resourceUri)
            || (isInFirstCercle(parentUri) && isInSecondCercle(resourceUri));
        Uri result = include ? resourceUri : null;
        return result;
    }

    public IScraperFactory getScraperFactory() throws IOException {
        if (fScraperFactory == null) {
            fScraperFactory = newScraperFactory();
        }
        return fScraperFactory;
    }

    public Uri getSiteUrl() {
        return fSiteUrl;
    }

    private boolean in(Uri resourceUri, List<String> prefixes) {
        String str = resourceUri.toString();
        boolean result = false;
        int len = prefixes != null ? prefixes.size() : 0;
        for (int i = 0; !result && i < len; i++) {
            String prefix = prefixes.get(i);
            result = str.startsWith(prefix);
        }
        return result;
    }

    protected void init(Uri siteUri) {
        fSiteUrl = siteUri;
    };

    protected boolean isInFirstCercle(Uri resourceUri) {
        return in(resourceUri, fFirstCircleUrlPrefixes);
    }

    protected boolean isInSecondCercle(Uri resourceUri) {
        return in(resourceUri, fSecondCircleUrlPrefixes);
    }

    protected IFormatterFactory newFormatterFactory() {
        return new TemplateBasedFormatterFactory(fTemplateProcessor, fResources);
    }

    protected IPresenterManager newPresenterManager() throws IOException {
        return new PresenterManager(this);
    }

    protected RemoteResourceLoader newResourceLoader() throws IOException {
        return new RemoteResourceLoader(fPropertyProvider);
    }

    protected IScraperFactory newScraperFactory() {
        return new XWikiScraperFactory();
    }

}
