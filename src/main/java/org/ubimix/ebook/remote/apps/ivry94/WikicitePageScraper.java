/**
 * 
 */
package org.ubimix.ebook.remote.apps.ivry94;

import java.io.IOException;
import java.util.Map;

import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.xwiki.XWikiInternalPageScrapper;

/**
 * @author kotelnikov
 */
public class WikicitePageScraper extends XWikiInternalPageScrapper {

    public static final String BASE_URL = "http://wiki.wikicite.net/xwiki/bin/view/";

    public WikicitePageScraper(RemotePagePresenter presenter) {
        super(presenter);
    }

    @Override
    public Map<String, Object> getHtmlProperties()
        throws XmlException,
        IOException {
        Map<String, Object> properties = super.getHtmlProperties();
        properties.remove("date");
        properties.remove("pageAuthorUrl");
        properties.remove("pageAuthor");
        properties.remove("url");
        return properties;
    }

}
