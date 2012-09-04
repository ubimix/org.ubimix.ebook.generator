/**
 * 
 */
package org.ubimix.ebook.remote.presenter;

import java.io.IOException;

import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.ubimix.ebook.remote.Site;
import org.ubimix.ebook.remote.formatters.IFormatter;
import org.ubimix.ebook.remote.formatters.ResourceCopyFormatter;

/**
 * @author kotelnikov
 */
public class LocalResourcePresenter extends RemoteResourcePresenter
    implements
    IContentPresenter {

    public LocalResourcePresenter(
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
        Uri uri = getResourceUrl();
        Uri baseUrl = getSite().getLocalResourceBaseUrl();
        Uri path = baseUrl.getRelative(uri);
        Uri.Builder builder = path.getBuilder();
        builder.setQuery("");
        path = builder.build();
        return path;
    }

}
