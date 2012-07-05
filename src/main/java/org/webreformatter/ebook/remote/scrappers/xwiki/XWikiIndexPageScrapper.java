package org.webreformatter.ebook.remote.scrappers.xwiki;

import java.io.IOException;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.PageScrapper;

/**
 * @author kotelnikov
 */
public class XWikiIndexPageScrapper extends PageScrapper
    implements
    IIndexPageScrapper {

    public XWikiIndexPageScrapper(RemotePagePresenter presenter) {
        super(presenter);
    }

    @Override
    public XmlWrapper getContent() throws XmlException {
        return null;
    }

    @Override
    public XmlWrapper getTitleElement() throws XmlException, IOException {
        XmlWrapper page = getPage();
        return page.eval("//html:div[@id='document-title']/html:h1");
    }

    @Override
    public XmlWrapper getTocList() throws XmlException, IOException {
        XmlWrapper page = getPage();
        return page.eval("//html:div[@id='xwikicontent']/html:ul");
    }
}