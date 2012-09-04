package org.ubimix.ebook.remote.presenter;

import java.io.IOException;
import java.util.Iterator;

import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;

public interface IPresenterManager {

    IPresenter getPresenter(Uri resourceUri, boolean create)
        throws IOException,
        XmlException;

    Iterator<Uri> getPresenterUrls();

}