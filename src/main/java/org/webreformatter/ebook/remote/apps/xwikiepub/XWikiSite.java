package org.webreformatter.ebook.remote.apps.xwikiepub;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.AbstractSite;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.formatters.MinisiteFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.PresenterManager;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public class XWikiSite extends AbstractSite {

    private Uri fResourceBaseUri;

    private Uri fSitePrefix;

    public XWikiSite(final Uri sitePrefix, Uri siteIndexUri, Uri resourceBaseUri)
        throws IOException {
        super(siteIndexUri);
        fResourceBaseUri = resourceBaseUri;
        fSitePrefix = sitePrefix;
    }

    @Override
    protected IFormatterFactory newFormatterFactory() {
        return new MinisiteFormatterFactory(fResourceBaseUri);
    }

    @Override
    protected IPresenterManager newPresenterManager() throws IOException {
        return new PresenterManager(this, fResourceBaseUri);
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new XWikiScrapperFactory(fSitePrefix);
    }

}