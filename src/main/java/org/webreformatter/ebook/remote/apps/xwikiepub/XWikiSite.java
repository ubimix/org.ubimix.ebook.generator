package org.webreformatter.ebook.remote.apps.xwikiepub;

import java.io.File;
import java.io.IOException;

import org.webreformatter.commons.templates.ITemplateProcessor;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.AbstractSite;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.formatters.TemplateBasedFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.PresenterManager;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public class XWikiSite extends AbstractSite {

    private File fResources;

    private Uri fSitePrefix;

    private ITemplateProcessor fTemplateProcessor;

    public XWikiSite(
        final Uri sitePrefix,
        Uri siteIndexUri,
        ITemplateProcessor templateProcessor,
        File resources) throws IOException {
        super(siteIndexUri);
        fTemplateProcessor = templateProcessor;
        fResources = resources;
        fSitePrefix = sitePrefix;
    }

    @Override
    protected IFormatterFactory newFormatterFactory() {
        return new TemplateBasedFormatterFactory(fTemplateProcessor, fResources);
    }

    @Override
    protected IPresenterManager newPresenterManager() throws IOException {
        Uri localBaseUri = new Uri(fResources.toURI() + "");
        return new PresenterManager(this, localBaseUri);
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new XWikiScrapperFactory(fSitePrefix);
    }

}