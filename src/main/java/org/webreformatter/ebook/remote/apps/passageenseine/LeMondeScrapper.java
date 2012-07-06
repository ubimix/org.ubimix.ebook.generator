/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.GenericPageScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapper;

/**
 * @author kotelnikov
 */
public class LeMondeScrapper extends GenericPageScrapper implements IScrapper {

    public static final String BASE_URL = "http://www.lemonde.fr/";

    /**
     * @param presenter
     */
    public LeMondeScrapper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:article[@class='article article_normal']/html:div[@class='txt15_140']",
            "//html:article[@class='article article_normal']/html:h1");
    }

}
