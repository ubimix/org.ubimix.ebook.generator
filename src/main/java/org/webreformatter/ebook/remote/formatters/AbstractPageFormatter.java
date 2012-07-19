/**
 * 
 */
package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;
import java.util.List;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.Site;
import org.webreformatter.ebook.remote.presenter.Base;
import org.webreformatter.ebook.remote.presenter.IContentPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;

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
