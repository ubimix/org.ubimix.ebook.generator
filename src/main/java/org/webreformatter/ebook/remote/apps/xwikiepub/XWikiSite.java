package org.webreformatter.ebook.remote.apps.xwikiepub;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.templates.ITemplateProcessor;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.AbstractSite;
import org.webreformatter.ebook.remote.IRemoteResourceLoader;
import org.webreformatter.ebook.remote.RemoteResourceLoader;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.formatters.TemplateBasedFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.PresenterManager;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.IUrlProvider;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.UrlProvider;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public class XWikiSite extends AbstractSite {

    private static Pattern WIKIPEDIA_LOCALIZED_URL = Pattern
        .compile("^(https?://\\w\\w\\.)(wikipedia.org/.*)$");

    private static Pattern WIKIPEDIA_URL = Pattern
        .compile("^(https?://.*?\\.wikipedia.org/.*)$");

    private File fResources;

    private Uri fSitePrefix;

    private String fSitePrefixStr;

    private ITemplateProcessor fTemplateProcessor;

    private IUrlProvider fUrlProvider = new UrlProvider() {

        @Override
        public Uri getResourceUri(Uri parentUri, Uri resourceUri) {
            resourceUri = getNormalizedDownloadUri(parentUri, resourceUri);
            Uri result = null;
            if (isInternalUri(resourceUri)) {
                result = resourceUri;
            } else if (isInternalUri(parentUri)) {
                String str = resourceUri.toString();
                if (WIKIPEDIA_URL.matcher(str).matches()) {
                    Matcher matcher = WIKIPEDIA_LOCALIZED_URL.matcher(str);
                    if (matcher.matches()) {
                        String prefix = matcher.group(1);
                        String suffix = matcher.group(2);
                        String middle = "";
                        if (!suffix.startsWith("m.")) {
                            middle = "m.";
                        }
                        result = new Uri(prefix + middle + suffix);
                    } else {
                        result = new Uri(str);
                    }
                }
            }
            return result;
        }

        protected boolean isInternalUri(Uri resourceUri) {
            String str = resourceUri.toString();
            return str.startsWith(fSitePrefixStr);
        }

    };

    public XWikiSite(
        final Uri sitePrefix,
        Uri siteIndexUri,
        ITemplateProcessor templateProcessor,
        File resources) throws IOException {
        super(siteIndexUri);
        fTemplateProcessor = templateProcessor;
        fResources = resources;
        fSitePrefix = sitePrefix;
        fSitePrefixStr = fSitePrefix.toString();
    };

    @Override
    protected IFormatterFactory newFormatterFactory() {
        return new TemplateBasedFormatterFactory(fTemplateProcessor, fResources);
    }

    @Override
    protected IPresenterManager newPresenterManager() throws IOException {
        Uri localBaseUri = new Uri(fResources.toURI() + "");
        return new PresenterManager(this, fUrlProvider, localBaseUri);
    }

    @Override
    protected IRemoteResourceLoader newResourceLoader() throws IOException {
        return new RemoteResourceLoader();
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new XWikiScrapperFactory();
    }

}