package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;

import org.webreformatter.ebook.remote.presenter.IContentPresenter;

public interface IFormatterFactory {

    <F extends IFormatter, P extends IContentPresenter> F getFormatter(
        P presenter,
        Class<F> viewType) throws IOException;

}