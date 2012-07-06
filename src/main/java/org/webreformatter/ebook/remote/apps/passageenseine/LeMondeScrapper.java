/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;

import org.webreformatter.commons.xml.XmlException;
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
            "//html:div[@class='grid_10 alpha prefix_1 suffix_1']//html:div[@class='txt15_140']",
            "//html:div[@class='grid_10 alpha prefix_1 suffix_1']//html:h1");
    }

    @Override
    protected void doSplitContent() throws XmlException, IOException {
        super.doSplitContent();
    }
}
