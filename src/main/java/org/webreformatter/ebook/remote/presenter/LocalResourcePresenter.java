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
