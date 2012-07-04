/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.formatters.IFormatter;

/**
 * @author kotelnikov
 */
public interface IContentPresenter extends IPresenter {

    IFormatter getFormatter() throws IOException;

    Uri getResourcePath() throws IOException;

}
