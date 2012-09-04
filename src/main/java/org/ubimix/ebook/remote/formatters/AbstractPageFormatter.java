/**
 * 
 */
package org.ubimix.ebook.remote.formatters;

import java.io.IOException;
import java.util.List;

import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.ebook.remote.Site;
import org.ubimix.ebook.remote.presenter.Base;
import org.ubimix.ebook.remote.presenter.IContentPresenter;
import org.ubimix.ebook.remote.presenter.IPresenter;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;

/**
 * @author kotelnikov
 */
public abstract class AbstractPageFormatter extends Base
    implements
    IInnerPageFormatter {

    protected Uri fBaseResourceUri;

    protected InnerPagePresenter fPresenter;

    public AbstractPageFormatter(
        InnerPagePresenter presenter,
        Uri baseResourceUri) {
        fPresenter = presenter;
        fBaseResourceUri = baseResourceUri;
    }

    public void appendScripts(final StringBuilder buf, List<Uri> urls)
        throws IOException,
        XmlException {
        Site site = fPresenter.getSite();
        for (Uri url : urls) {
            IPresenter presenter = site.getPresenterManager().getPresenter(
                url,
                true);
            if (presenter instanceof IContentPresenter) {
                IContentPresenter p = (IContentPresenter) presenter;
                Uri path = fPresenter.getPathToResource(p);
                buf
                    .append("<script src=\"")
                    .append(path)
                    .append("\" type=\"text/javascript\"></script>");
            }
        }
    }

    public void appendStyles(StringBuilder buf, List<Uri> urls)
        throws IOException,
        XmlException {
        for (Uri url : urls) {
            IPresenter presenter = getPresenter(url, true);
            if (presenter instanceof IContentPresenter) {
                IContentPresenter p = (IContentPresenter) presenter;
                Uri path = fPresenter.getPathToResource(p);
                buf
                    .append("<link href=\"")
                    .append(path)
                    .append("\" rel=\"stylesheet\"></link>");
            }
        }
    }

    @Override
    protected Site getSite() {
        return fPresenter.getSite();
    }

    public void loadResources(List<Uri> urls) throws IOException, XmlException {
        for (Uri url : urls) {
            getPresenter(url, true);
        }
    }
}
