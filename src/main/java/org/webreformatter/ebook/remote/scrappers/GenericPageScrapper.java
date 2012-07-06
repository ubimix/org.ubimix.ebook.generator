package org.webreformatter.ebook.remote.scrappers;

import java.io.IOException;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;

/**
 * @author kotelnikov
 */
public class GenericPageScrapper extends PageScrapper
    implements
    IInnerPageScrapper {

    protected XmlWrapper fContent;

    private String fContentXPath;

    protected String fTitle;

    private String fTitleXPath;

    public GenericPageScrapper(
        RemotePagePresenter presenter,
        String contentXPath,
        String titleXPath) {
        super(presenter);
        fContentXPath = contentXPath;
        fTitleXPath = titleXPath;
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

    protected void onSplitContent() throws XmlException, IOException {
    }

    private void splitContent() throws XmlException, IOException {
        if (fContent == null) {
            XmlWrapper page = getPage();
            fContent = page.eval(fContentXPath);
            XmlWrapper titleElement = page.eval(fTitleXPath);
            if (titleElement == null) {
                titleElement = page.eval("//html:title");
            }
            if (titleElement != null) {
                titleElement.remove();
                fTitle = titleElement.toText();
            }
            onSplitContent();
        }
    }

}