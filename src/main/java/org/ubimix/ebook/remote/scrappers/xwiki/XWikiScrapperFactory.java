package org.ubimix.ebook.remote.scrappers.xwiki;

import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.presenter.IPresenter;
import org.ubimix.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.presenter.RemoteResourcePresenter;
import org.ubimix.ebook.remote.scrappers.IScrapper;
import org.ubimix.ebook.remote.scrappers.IScrapperFactory;
import org.ubimix.ebook.remote.scrappers.mediawiki.MobileWikipediaPageScrapper;
import org.ubimix.ebook.remote.scrappers.mediawiki.WikipediaPageScrapper;

/**
 * @author kotelnikov
 */
public class XWikiScrapperFactory implements IScrapperFactory {

    public XWikiScrapperFactory() {
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
                result = new XWikiIndexPageScrapper(p);
            } else if (scrapperType == IInnerPageScrapper.class) {
                Uri pageUri = p.getResourceUrl();
                String str = pageUri.toString();
                if (str.indexOf(".m.wikipedia.org/") > 0) {
                    result = new MobileWikipediaPageScrapper(p);
                } else if (str.indexOf("wikipedia.org/") > 0) {
                    result = new WikipediaPageScrapper(p);
                } else {
                    result = new XWikiInternalPageScrapper(p);
                }
            }
        } else if (presenter instanceof RemoteResourcePresenter) {
            // Binary resources

        }
        return (S) result;
    }
}