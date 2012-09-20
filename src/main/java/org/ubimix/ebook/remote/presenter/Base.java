/**
 * 
 */
package org.ubimix.ebook.remote.presenter;

import java.io.IOException;
import java.util.Iterator;

import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.Site;
import org.ubimix.ebook.remote.formatters.IFormatter;
import org.ubimix.ebook.remote.scrapers.IScrapper;

/**
 * @author kotelnikov
 */
public abstract class Base {

    public Base() {
    }

    protected IPresenter getPresenter(Uri href, boolean create)
        throws IOException,
        XmlException {
        IPresenter result = getSite().getPresenterManager().getPresenter(
            href,
            create);
        return result;
    }

    protected Iterator<Uri> getPresenterUrls() throws IOException, XmlException {
        Iterator<Uri> result = getSite()
            .getPresenterManager()
            .getPresenterUrls();
        return result;
    }

    protected abstract Site getSite();

    protected <F extends IFormatter> F newFormatter(
        IContentPresenter presenter,
        Class<F> type) throws IOException {
        F result = getSite()
            .getFormatterFactory()
            .getFormatter(presenter, type);
        return result;
    }

    protected <S extends IScrapper> S newScrapper(
        IPresenter presenter,
        Class<S> type) throws IOException, XmlException {
        S result = getSite().getScraperFactory().getScrapper(presenter, type);
        return result;
    }
}