package org.webreformatter.ebook.remote.apps.passageenseine;

import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.GenericPageScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapper;

/**
 * @author kotelnikov
 */
public class StandlogScrapper extends GenericPageScrapper implements IScrapper {
    public static final String BASE_URL = "http://standblog.org/blog/";

    public StandlogScrapper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:div[@class='post-content']",
            "//html:div[@class='post']/*[@class='post-title']");
    }

}
