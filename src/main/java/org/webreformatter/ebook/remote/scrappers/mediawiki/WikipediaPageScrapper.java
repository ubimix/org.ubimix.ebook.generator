/**
 * 
 */
package org.webreformatter.ebook.remote.scrappers.mediawiki;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.PageScrapper;

/**
 * @author kotelnikov
 */
public class WikipediaPageScrapper extends PageScrapper
    implements
    IInnerPageScrapper {

    public WikipediaPageScrapper(
        IUrlProvider urlProvider,
        RemotePagePresenter presenter) {
        super(urlProvider, presenter);
    }

    @Override
    public XmlWrapper getContent() throws XmlException {
        XmlWrapper page = getPage();
        return page.eval("//html:div[@id='bodyContent']");
    }

    @Override
    public String getTitle() throws XmlException {
        XmlWrapper page = getPage();
        return page
            .evalStr("//html:h1[@id='firstHeading']/html:span[@dir='auto']");
    }

}
