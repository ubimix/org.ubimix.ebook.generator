/**
 * 
 */
package org.webreformatter.ebook.remote.scrappers.xwiki;

import java.util.Map;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.PageScrapper;
import org.webreformatter.scrapper.utils.HtmlPropertiesExtractor;
import org.webreformatter.scrapper.utils.HtmlPropertiesExtractor.PropertyListener;
import org.webreformatter.scrapper.utils.HtmlTablePropertiesExtractor;

/**
 * @author kotelnikov
 */
public class XWikiInternalPageScrapper extends PageScrapper
    implements
    IInnerPageScrapper {

    private XmlWrapper fContent;

    private String fTitle;

    public XWikiInternalPageScrapper(
        IUrlProvider urlProvider,
        RemotePagePresenter presenter) {
        super(urlProvider, presenter);
    }

    @Override
    public XmlWrapper getContent() throws XmlException {
        splitContent();
        return fContent;
    }

    @Override
    public Map<String, Object> getProperties() throws XmlException {
        XmlWrapper content = getContent();
        HtmlPropertiesExtractor extractor = new HtmlTablePropertiesExtractor();
        PropertyListener listener = new PropertyListener();
        extractor.extractProperties(content, listener);
        return listener.getProperties();
    }

    @Override
    public String getTitle() throws XmlException {
        splitContent();
        return fTitle;
    }

    private void splitContent() throws XmlException {
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
