/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;

import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.formatters.ResourceCopyFormatter;

/**
 * @author kotelnikov
 */
public class BinaryResourcePresenter extends RemoteResourcePresenter
    implements
    IContentPresenter {

    public BinaryResourcePresenter(ISite site, RemoteResource resource) {
        super(site, resource);
    }

    @Override
    public IFormatter getFormatter() throws IOException {
        return new ResourceCopyFormatter(fResource);
    }

    @Override
    protected String getResourcePathFolder() {
        return "binaries";
    }

}
