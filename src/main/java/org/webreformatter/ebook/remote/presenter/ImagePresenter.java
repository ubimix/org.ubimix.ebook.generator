/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.Site;

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
