/**
 * 
 */
package org.ubimix.ebook.remote.apps.ivry94;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.GenericPageScraper;
import org.ubimix.ebook.remote.scrapers.IScrapper;

/**
 * @author kotelnikov
 */
public class Ivry94FrScraper extends GenericPageScraper implements IScrapper {

    public static final String BASE_URL = "http://ivry94.fr/";

    /**
     * @param presenter
     */
    public Ivry94FrScraper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:div[@class='body']",
            "//html:div[@class='article']//html:h1");
    }

    @Override
    protected void doSplitContent() throws XmlException, IOException {
        super.doSplitContent();
    }
}
