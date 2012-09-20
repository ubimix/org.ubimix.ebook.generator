/**
 * 
 */
package org.ubimix.ebook.remote.apps.passageenseine;

import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.GenericPageScraper;

/**
 * @author kotelnikov
 */
public class AutoPageScrapper extends GenericPageScraper {

    public AutoPageScrapper(RemotePagePresenter presenter) {
        super(presenter, "//html:body", "//html:title");
    }

}
