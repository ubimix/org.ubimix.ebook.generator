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
public class MobileWikipediaPageScrapper extends PageScraper
    implements
    IInnerPageScrapper {

    private XmlWrapper fContent;

    private String fTitle;

    /**
     * @param page
     * @param pageUri
     */
    public MobileWikipediaPageScrapper(RemotePagePresenter presenter) {
        super(presenter);
    }

    private void checkContent() throws XmlException, IOException {
        if (fContent == null) {
            XmlWrapper page = getPage();
            fContent = page.eval("//html:div[@id='content']");
            if (fContent == null) {
                return;
            }
            XmlWrapper titleElement = fContent
                .eval(".//html:h1[@id='firstHeading']");
            if (titleElement != null) {
                titleElement.remove();
                fTitle = titleElement.toText();
            }
            removeElements(
                fContent,
                ".//html:div[@class='navbox_group']",
                ".//ul[@id='bandeau-portail']");
        }
    }

    @Override
    public XmlWrapper getContent() throws XmlException, IOException {
        checkContent();
        return fContent;
    }

    @Override
    public String getTitle() throws XmlException, IOException {
        checkContent();
        return fTitle;
    }

    private void removeElements(XmlWrapper xml, String... paths)
        throws XmlException {
        for (String path : paths) {
            XmlWrapper e = xml.eval(path);
            if (e != null) {
                e.remove();
            }
        }
    }

}
