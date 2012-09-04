package org.ubimix.ebook.remote.scrappers;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public class GenericPageScrapper extends PageScrapper
    implements
    IInnerPageScrapper {

    protected XmlWrapper fContent;

    protected String fContentXPath;

    protected String fTitle;

    protected String fTitleXPath;

    public GenericPageScrapper(
        RemotePagePresenter presenter,
        String contentXPath,
        String titleXPath) {
        super(presenter);
        fContentXPath = contentXPath;
        fTitleXPath = titleXPath;
    }

    protected void doSplitContent() throws XmlException, IOException {
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

    protected void splitContent() throws XmlException, IOException {
        if (fContent == null) {
            doSplitContent();
        }
    }

}