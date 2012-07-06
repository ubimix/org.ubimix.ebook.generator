/**
 * 
 */
package org.webreformatter.ebook.remote.scrappers.xwiki;

import java.io.IOException;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.PageScrapper;

/**
 * @author kotelnikov
 */
public class XWikiInternalPageScrapper extends PageScrapper
    implements
    IInnerPageScrapper {

    private XmlWrapper fContent;

    private String fTitle;

    public XWikiInternalPageScrapper(RemotePagePresenter presenter) {
        super(presenter);
    }

    @Override
    public XmlWrapper getContent() throws XmlException, IOException {
        splitContent();
        return fContent;
    }

    @Override
    public String getTitle() throws XmlException, IOException {
        splitContent();
        return fTitle;
    }

    private void splitContent() throws XmlException, IOException {
        if (fContent == null) {
            XmlWrapper page = getPage();

            fContent = page.eval("//html:div[@id='xwikicontent']");

            XmlWrapper titleElement = page
                .eval("//html:div[@id='document-title']/html:h1");
            if (titleElement == null) {
                titleElement = page.eval("//html:title");
            }
            if (titleElement != null) {
                titleElement.remove();
                fTitle = titleElement.toText();
            }
        }
    }

}
