/**
 * 
 */
package org.ubimix.ebook.remote.apps.ivry94;

import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.GenericPageScraper;

/**
 * @author kotelnikov
 */
public class AutoPageScraper extends GenericPageScraper {

    public AutoPageScraper(RemotePagePresenter presenter) {
        super(presenter, "//html:body", "//html:title");
    }

}
