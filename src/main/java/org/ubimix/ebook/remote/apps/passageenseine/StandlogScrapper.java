package org.ubimix.ebook.remote.apps.passageenseine;

import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.GenericPageScraper;
import org.ubimix.ebook.remote.scrapers.IScrapper;

/**
 * @author kotelnikov
 */
public class StandlogScrapper extends GenericPageScraper implements IScrapper {
    public static final String BASE_URL = "http://standblog.org/blog/";

    public StandlogScrapper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:div[@class='post-content']",
            "//html:div[@class='post']/*[@class='post-title']");
    }

}
