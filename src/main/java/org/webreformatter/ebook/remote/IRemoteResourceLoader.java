package org.webreformatter.ebook.remote;

import java.io.IOException;
import java.io.InputStream;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.scrapper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public interface IRemoteResourceLoader {

    /**
     * @author kotelnikov
     */
    public static abstract class RemoteResource {

        private HttpStatusCode fDownloadStatus;

        private XmlWrapper fHtmlPage;

        private String fMimeType;

        private Uri fUri;

        public RemoteResource(Uri uri) {
            fUri = uri;
        }

        protected void clean() {
            fMimeType = null;
            fDownloadStatus = null;
        }

        protected abstract HttpStatusCode doDownload() throws IOException;

        public HttpStatusCode download(boolean reload) throws IOException {
            if (fDownloadStatus == null || reload) {
                clean();
                fDownloadStatus = doDownload();
            }
            return fDownloadStatus;
        }

        public abstract InputStream getContent() throws IOException;

        public String getFileExtension() throws IOException {
            String mimeType = getMimeType();
            String extension = getUri().getPath().getFileExtension();
            if (extension == null) {
                final String imagePrefix = "image/";
                if (mimeType.startsWith(imagePrefix)) {
                    extension = mimeType.substring(imagePrefix.length());
                } else if (isHtmlPage()) {
                    extension = "html";
                } else {
                    extension = "bin";
                }
            }
            return extension;
        }

        public XmlWrapper getHtmlPage() throws IOException, XmlException {
            if (fHtmlPage == null) {
                download(false);
                fHtmlPage = loadContentAsHtml();
            }
            return fHtmlPage;
        }

        public String getMimeType() throws IOException {
            if (fMimeType == null) {
                download(false);
                fMimeType = loadMimeType();
            }
            return fMimeType;
        }

        public HttpStatusCode getStatus() throws IOException {
            download(false);
            return fDownloadStatus;
        }

        public Uri getUri() {
            return fUri;
        }

        public boolean isHtmlPage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("text/html");
        }

        public boolean isImage() throws IOException {
            String mimeType = getMimeType();
            return mimeType != null && mimeType.startsWith("image/");
        }

        protected abstract XmlWrapper loadContentAsHtml()
            throws IOException,
            XmlException;

        protected abstract String loadMimeType() throws IOException;
    }

    IRemoteResourceLoader.RemoteResource getResource(
        Uri resourceUri,
        boolean create);

}