package org.ubimix.ebook.remote.scrapers;

import org.ubimix.ebook.remote.presenter.IPresenter;

public interface IScraperFactory {

    <S extends IScrapper, P extends IPresenter> S getScrapper(
        P presenter,
        Class<S> scrapperType);

}