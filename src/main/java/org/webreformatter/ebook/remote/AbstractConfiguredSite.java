/**
 * 
 */
package org.webreformatter.ebook.remote;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.templates.ITemplateProcessor;
import org.webreformatter.commons.templates.ITemplateProvider;
import org.webreformatter.commons.templates.TemplateException;
import org.webreformatter.commons.templates.providers.FileResourceProvider;
import org.webreformatter.commons.templates.velocity.VelocityTemplateProcessor;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.formatters.TemplateBasedFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.PresenterManager;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.IUrlProvider;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public abstract class AbstractConfiguredSite extends AbstractSite {

    private final static Logger log = Logger
        .getLogger(AbstractConfiguredSite.class.getName());

    public static IOException handleError(String msg, Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        }
        log.log(Level.WARNING, msg, t);
        return new IOException(msg, t);
    }

    protected IVariableProvider fPropertyProvider;

    private File fResources;

    private ITemplateProcessor fTemplateProcessor;

    private IUrlProvider fUrlProvider;

    public AbstractConfiguredSite(IVariableProvider propertyProvider)
        throws IOException {
        try {
            fPropertyProvider = propertyProvider;
            fResources = new File(getConfigValue("templatesRoot"));
            ITemplateProvider templateProvider = new FileResourceProvider(
                fResources);
            Properties properties = new Properties();
            fTemplateProcessor = new VelocityTemplateProcessor(
                templateProvider,
                properties);
            Uri siteIndexUrl = new Uri(getConfigValue("siteIndexUrl"));
            init(siteIndexUrl);
        } catch (TemplateException e) {
            throw handleError("Can not initialize template engine site", e);
        }
    }

    protected String getConfigValue(String key) {
        return StringUtil.resolvePropertyByKey(key, fPropertyProvider);
    };

    protected IUrlProvider getUrlProvider() {
        if (fUrlProvider == null) {
            fUrlProvider = newUrlProvider();
        }
        return fUrlProvider;
    }

    @Override
    protected IFormatterFactory newFormatterFactory() {
        return new TemplateBasedFormatterFactory(fTemplateProcessor, fResources);
    }

    @Override
    protected IPresenterManager newPresenterManager() throws IOException {
        Uri localBaseUri = new Uri(fResources.toURI() + "");
        IUrlProvider urlProvider = getUrlProvider();
        return new PresenterManager(this, urlProvider, localBaseUri);
    }

    @Override
    protected IRemoteResourceLoader newResourceLoader() throws IOException {
        return new RemoteResourceLoader(fPropertyProvider);
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new XWikiScrapperFactory();
    }

    protected abstract IUrlProvider newUrlProvider();
}
