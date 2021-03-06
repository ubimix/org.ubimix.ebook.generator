/**
 * 
 */
package org.ubimix.scraper.example;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ubimix.commons.io.IOUtil;
import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.commons.uri.Uri;
import org.ubimix.commons.xml.XmlException;
import org.ubimix.commons.xml.XmlWrapper;
import org.ubimix.ebook.remote.RemoteResourceLoader;
import org.ubimix.ebook.remote.RemoteResourceLoader.RemoteResource;
import org.ubimix.scraper.protocol.HttpStatusCode;

/**
 * @author kotelnikov
 */
public class EPubGeneratorServlet extends HttpServlet {

    private static final long serialVersionUID = 6699363449276222571L;

    private IVariableProvider fPropertiesProvider;

    /**
     * 
     */
    public EPubGeneratorServlet(IVariableProvider propertiesProvider) {
        fPropertiesProvider = propertiesProvider;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException,
        IOException {
        String indexUrl = req.getParameter("index");
        if (indexUrl == null) {
            // FIXME: remove this line
            indexUrl = "https://beebapp.ubimix.com/xwiki/bin/view/a4/WebHome";
            // resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            // return;
        }

        RemoteResourceLoader reader = new RemoteResourceLoader(
            fPropertiesProvider);
        Uri uri = new Uri(indexUrl);
        RemoteResource r = reader.download(uri);
        HttpStatusCode status = r.getStatus();
        if (status.isOkOrNotModified()) {
            String mimeType = r.getMimeType();
            resp.setContentType(mimeType);
            if (r.isHtmlPage()) {
                try {
                    XmlWrapper page = r.getHtmlPage();
                    ServletOutputStream out = resp.getOutputStream();
                    out.print(page.toString());
                } catch (XmlException e) {
                    resp
                        .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                InputStream content = r.getContent();
                IOUtil.copy(content, resp.getOutputStream());
            }
        } else {
            resp.sendError(status.getStatusCode());
        }
    }
}
