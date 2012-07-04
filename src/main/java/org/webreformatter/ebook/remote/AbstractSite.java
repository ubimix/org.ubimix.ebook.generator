package org.webreformatter.ebook.remote;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;

/**
 * @author kotelnikov
 */
public abstract class AbstractSite implements ISite {

    private IFormatterFactory fFormatterFactory;

    private IPresenterManager fPresenterManager;

    private IScrapperFactory fScrapperFactory;

    private Uri fSiteUrl;

    public AbstractSite(Uri siteUri) {
        fSiteUrl = siteUri;
    }

    @Override
    public IFormatterFactory getFormatterFactory() throws IOException {
        if (fFormatterFactory == null) {
            fFormatterFactory = newFormatterFactory();
        }
        return fFormatterFactory;
    }

    @Override
    public IPresenterManager getPresenterManager() throws IOException {
        if (fPresenterManager == null) {
            fPresenterManager = newPresenterManager();
        }
        return fPresenterManager;
    }

    @Override
    public IScrapperFactory getScrapperFactory() throws IOException {
        if (fScrapperFactory == null) {
            fScrapperFactory = newScrapperFactory();
        }
        return fScrapperFactory;
    }

    @Override
    public Uri getSiteUrl() {
        return fSiteUrl;
    }

    protected abstract IFormatterFactory newFormatterFactory()
        throws IOException;

    protected abstract IPresenterManager newPresenterManager()
        throws IOException;

    protected abstract IScrapperFactory newScrapperFactory() throws IOException;

}