/**
 * 
 */
package org.ubimix.ebook.remote.apps.passageenseine;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrappers.GenericPageScrapper;
import org.ubimix.ebook.remote.scrappers.IScrapper;

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
            "//html:div[@class='grid_10 alpha prefix_1 suffix_1']//html:div[@class='txt15_140']",
            "//html:div[@class='grid_10 alpha prefix_1 suffix_1']//html:h1");
    }

    @Override
    protected void doSplitContent() throws XmlException, IOException {
        super.doSplitContent();
    }
}
