/**
 * 
 */
package org.webreformatter.ebook.remote.presenter;

import org.webreformatter.ebook.remote.ISite;
import org.webreformatter.ebook.remote.IRemoteResourceLoader.RemoteResource;

/**
 * @author kotelnikov
 */
public class ImagePresenter extends BinaryResourcePresenter {

    public ImagePresenter(ISite site, RemoteResource resource) {
        super(site, resource);
    }

    @Override
    protected String getResourcePathFolder() {
        return "images";
    }

}
