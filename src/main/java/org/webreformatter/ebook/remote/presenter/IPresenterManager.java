package org.webreformatter.ebook.remote.presenter;

import java.io.IOException;
import java.util.Iterator;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;

public interface IPresenterManager {

    IPresenter getPresenter(Uri resourceUri, boolean create)
        throws IOException,
        XmlException;

    Iterator<Uri> getPresenterUrls();

}