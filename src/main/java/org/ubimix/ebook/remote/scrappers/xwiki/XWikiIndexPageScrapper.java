package org.ubimix.ebook.remote.scrappers.xwiki;

import java.io.IOException;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrappers.PageScrapper;

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