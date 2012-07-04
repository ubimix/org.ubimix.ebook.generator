package org.webreformatter.ebook.remote.scrappers;

import org.webreformatter.ebook.remote.presenter.IPresenter;

public interface IScrapperFactory {

    <S extends IScrapper, P extends IPresenter> S getScrapper(
        P presenter,
        Class<S> scrapperType);

}