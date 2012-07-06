/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;
import java.util.Map;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiInternalPageScrapper;

/**
 * @author kotelnikov
 */
public class BeebappPageScrapper extends XWikiInternalPageScrapper {

    public static final String BASE_URL = "https://beebapp.ubimix.com/xwiki/bin/view/";

    public BeebappPageScrapper(RemotePagePresenter presenter) {
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
