/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.Site;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.formatters.ResourceCopyFormatter;

/**
 * @author kotelnikov
 */
public class BinaryResourcePresenter extends RemoteResourcePresenter
    implements
    IContentPresenter {

    public BinaryResourcePresenter(
        Site site,
        RemoteResource resource,
        Uri resourceUri) {
        super(site, resource, resourceUri);
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
