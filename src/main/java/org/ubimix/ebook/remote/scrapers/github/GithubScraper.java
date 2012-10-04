
package org.ubimix.ebook.remote.scrapers.github;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.GenericPageScraper;
import org.ubimix.ebook.remote.scrapers.IScrapper;

public class GithubScraper extends GenericPageScraper implements IScrapper {

    public static final String BASE_URL = "https://github.com/";

    /**
     * @param presenter
     */
    public GithubScraper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:div[@id='readme']",
            "//html:div[@id='readme']//html:h1");
    }

    @Override
    protected void doSplitContent() throws XmlException, IOException {
        super.doSplitContent();
    }
}
