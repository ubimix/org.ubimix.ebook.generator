/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.formatters.IFormatter;
import org.webreformatter.ebook.remote.formatters.ResourceCopyFormatter;

/**
 * @author kotelnikov
 */
public class LocalResourcePresenter extends RemoteResourcePresenter
    implements
    IContentPresenter {

    private Uri fBaseUrl;

    public LocalResourcePresenter(
        ISite site,
        RemoteResource resource,
        Uri baseUrl) {
        super(site, resource);
        fBaseUrl = baseUrl;
    }

    @Override
    public IFormatter getFormatter() throws IOException {
        return new ResourceCopyFormatter(fResource);
    }

    @Override
    public Uri getResourcePath() throws IOException {
        Uri uri = getResourceUrl();
        Uri path = fBaseUrl.getRelative(uri);
        Uri.Builder builder = path.getBuilder();
        builder.setQuery("");
        path = builder.build();
        return path;
    }

}
