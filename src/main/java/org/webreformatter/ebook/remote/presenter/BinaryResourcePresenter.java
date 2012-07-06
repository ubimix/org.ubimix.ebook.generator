/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.ISite;
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
    public Uri getResourcePath() throws IOException {
        String path = getResourcePathFolder();
        Uri uri = getResourceUrl();
        String hash = getHash(uri);
        String ext = fResource.getFileExtension();
        Uri result = new Uri(path + hash + "." + ext);
        return result;
    }

    public String getResourcePathFolder() {
        return "binaries/";
    }

}
