/**
 * 
 */
package org.webreformatter.ebook.remote.scrappers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.ebook.BookId;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public abstract class PageScrapper implements IScrapper {

    protected RemotePagePresenter fPresenter;

    public PageScrapper(RemotePagePresenter presenter) {
        fPresenter = presenter;
    }

    public abstract XmlWrapper getContent() throws XmlException, IOException;

    protected XmlWrapper getPage() throws XmlException, IOException {
        return fPresenter.getHtmlPage();
    }

    public Map<String, Object> getHtmlProperties() throws XmlException, IOException {
        Map<String, Object> properties = new HashMap<String, Object>();
        Uri url = fPresenter.getResourceUrl();
        properties.put("url", url + "");
        XmlWrapper page = getPage();
        String title = page.evalStr("//html:title");
        if (title != null) {
            properties.put("title", title);
        }
        BookId id = fPresenter.getResourceId();
        if (id != null) {
            properties.put("id", id + "");
        }
        List<XmlWrapper> metaTags = page.evalList("//html:meta[@name]");
        for (XmlWrapper meta : metaTags) {
            String name = meta.getAttribute("name");
            String value = meta.getAttribute("content");
            if (name != null && value != null) {
                properties.put(name, value);
            }
        }
        return properties;
    }

}
