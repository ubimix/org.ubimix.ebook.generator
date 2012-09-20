/**
 * 
 */
package org.ubimix.ebook.remote.scrapers.mediawiki;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.PageScraper;

/**
 * @author kotelnikov
 */
public class WikipediaPageScrapper extends PageScraper
    implements
    IInnerPageScrapper {

    public WikipediaPageScrapper(RemotePagePresenter presenter) {
        super(presenter);
    }

    @Override
    public XmlWrapper getContent() throws XmlException, IOException {
        XmlWrapper page = getPage();
        return page.eval("//html:div[@id='bodyContent']");
    }

    @Override
    public String getTitle() throws XmlException, IOException {
        XmlWrapper page = getPage();
        return page
            .evalStr("//html:h1[@id='firstHeading']/html:span[@dir='auto']");
    }

}
