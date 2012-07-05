/**
 * 
 */
package org.webreformatter.ebook.remote;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;

/**
 * @author kotelnikov
 */
public interface ISite {

    IFormatterFactory getFormatterFactory() throws IOException;

    IPresenterManager getPresenterManager() throws IOException;

    IRemoteResourceLoader getResourceLoader() throws IOException;

    IScrapperFactory getScrapperFactory() throws IOException;

    Uri getSiteUrl();

}
