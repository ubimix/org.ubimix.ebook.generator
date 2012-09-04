package org.ubimix.ebook.remote.scrappers;

import org.ubimix.ebook.remote.presenter.IPresenter;

public interface IScrapperFactory {

    <S extends IScrapper, P extends IPresenter> S getScrapper(
        P presenter,
        Class<S> scrapperType);

}