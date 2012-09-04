/**
 * 
 */
package org.ubimix.ebook.remote.presenter;

import java.io.IOException;

import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.formatters.IFormatter;

/**
 * @author kotelnikov
 */
public interface IContentPresenter extends IPresenter {

    IFormatter getFormatter() throws IOException;

    Uri getResourcePath() throws IOException;

}
