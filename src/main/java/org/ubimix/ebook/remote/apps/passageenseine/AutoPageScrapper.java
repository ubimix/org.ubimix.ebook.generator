/**
 * 
 */
package org.ubimix.ebook.remote.apps.passageenseine;

import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrappers.GenericPageScrapper;

/**
 * @author kotelnikov
 */
public class AutoPageScrapper extends GenericPageScrapper {

    public AutoPageScrapper(RemotePagePresenter presenter) {
        super(presenter, "//html:body", "//html:title");
    }

}
