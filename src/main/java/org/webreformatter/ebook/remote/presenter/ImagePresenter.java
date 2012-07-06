/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;
import org.webreformatter.ebook.remote.ISite;

/**
 * @author kotelnikov
 */
public class ImagePresenter extends BinaryResourcePresenter {

    public ImagePresenter(ISite site, RemoteResource resource) {
        super(site, resource);
    }

    @Override
    public String getResourcePathFolder() {
        return "images/";
    }

}
