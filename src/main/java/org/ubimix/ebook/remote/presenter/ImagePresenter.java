/**
 * 
 */
package org.ubimix.ebook.remote.presenter;

import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.ubimix.ebook.remote.Site;

/**
 * @author kotelnikov
 */
public class ImagePresenter extends BinaryResourcePresenter {

    public ImagePresenter(Site site, RemoteResource resource, Uri resourceUri) {
        super(site, resource, resourceUri);
    }

    @Override
    public String getResourcePathFolder() {
        return "images/";
    }

}
