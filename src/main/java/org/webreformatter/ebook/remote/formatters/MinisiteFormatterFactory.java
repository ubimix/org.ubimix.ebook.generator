package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.presenter.IContentPresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public class MinisiteFormatterFactory implements IFormatterFactory {

    private Uri fResourceBaseUrl;

    public MinisiteFormatterFactory(Uri resourceBaseUri) {
        fResourceBaseUrl = resourceBaseUri;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F extends IFormatter, P extends IContentPresenter> F getFormatter(
        P presenter,
        Class<F> viewType) throws IOException {
        IFormatter formatter = null;
        if (viewType == IInnerPageFormatter.class
            && presenter instanceof RemotePagePresenter) {
            formatter = new SimplePageFormatter(
                (InnerPagePresenter) presenter,
                fResourceBaseUrl);
        }
        return (F) formatter;
    }

}