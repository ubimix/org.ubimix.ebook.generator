/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.GenericPageScrapper;

/**
 * @author kotelnikov
 */
public class AutoPageScrapper extends GenericPageScrapper {

    public AutoPageScrapper(RemotePagePresenter presenter) {
        super(presenter, "//html:body", "//html:title");
    }

}
