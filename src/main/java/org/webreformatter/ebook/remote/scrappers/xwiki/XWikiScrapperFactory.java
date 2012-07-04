package org.webreformatter.ebook.remote.scrappers.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.RemoteResourcePresenter;
import org.webreformatter.ebook.remote.scrappers.IScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.PageScrapper.IUrlProvider;
import org.webreformatter.ebook.remote.scrappers.PageScrapper.UrlProvider;
import org.webreformatter.ebook.remote.scrappers.mediawiki.MobileWikipediaPageScrapper;
import org.webreformatter.ebook.remote.scrappers.mediawiki.WikipediaPageScrapper;

/**
 * @author kotelnikov
 */
public class XWikiScrapperFactory implements IScrapperFactory {

    private static Pattern WIKIPEDIA_LOCALIZED_URL = Pattern
        .compile("^(https?://\\w\\w\\.)(wikipedia.org/.*)$");

    private static Pattern WIKIPEDIA_URL = Pattern
        .compile("^(https?://.*?\\.wikipedia.org/.*)$");

    private String fPagePrefix;

    private Uri fPagePrefixUrl;

    private IUrlProvider fUrlProvider;

    public XWikiScrapperFactory(final Uri sitePrefix) {
        fPagePrefixUrl = sitePrefix;
        fPagePrefix = fPagePrefixUrl.toString();
        fUrlProvider = new UrlProvider() {

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
                return str.startsWith(fPagePrefix);
            }

        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends IScrapper, P extends IPresenter> S getScrapper(
        P presenter,
        Class<S> scrapperType) {
        IScrapper result = null;
        if (presenter instanceof RemotePagePresenter) {
            // HTML Pages
            RemotePagePresenter p = (RemotePagePresenter) presenter;
            if (scrapperType == IIndexPageScrapper.class) {
                result = new XWikiIndexPageScrapper(fUrlProvider, p);
            } else if (scrapperType == IInnerPageScrapper.class) {
                Uri pageUri = p.getResourceUrl();
                String str = pageUri.toString();
                if (str.indexOf(".m.wikipedia.org/") > 0) {
                    result = new MobileWikipediaPageScrapper(fUrlProvider, p);
                } else if (str.indexOf("wikipedia.org/") > 0) {
                    result = new WikipediaPageScrapper(fUrlProvider, p);
                } else {
                    result = new XWikiInternalPageScrapper(fUrlProvider, p);
                }
            }
        } else if (presenter instanceof RemoteResourcePresenter) {
            // Binary resources

        }
        return (S) result;
    }
}