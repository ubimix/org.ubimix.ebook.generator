/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.Iterator;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.scrappers.IScrapper;

/**
 * @author kotelnikov
 */
public abstract class Base {

    public Base() {
    }

    protected IPresenter getPresenter(Uri href, boolean create)
        throws IOException,
        XmlException {
        IPresenter result = getSite()
            .getPresenterManager()
            .getPresenter(href, create);
        return result;
    }

    protected Iterator<Uri> getPresenterUrls() throws IOException, XmlException {
        Iterator<Uri> result = getSite()
            .getPresenterManager()
            .getPresenterUrls();
        return result;
    }

    protected abstract ISite getSite();

    protected <F extends IFormatter> F newFormatter(
        IContentPresenter presenter,
        Class<F> type) throws IOException {
        F result = getSite().getFormatterFactory().getFormatter(
            presenter,
            type);
        return result;
    }

    protected <S extends IScrapper> S newScrapper(
        IPresenter presenter,
        Class<S> type) throws IOException, XmlException {
        S result = getSite().getScrapperFactory().getScrapper(
            presenter,
            type);
        return result;
    }
}