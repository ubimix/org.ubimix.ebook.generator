/**
 * 
 */
package org.webreformatter.ebook.remote.apps.xwikiepub;

import java.io.File;
import java.io.IOException;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.bem.PrintBookListener;
import org.webreformatter.ebook.bom.epub.EPubGenerator;
import org.webreformatter.ebook.remote.RemoteBookVisitor;

/**
 * @author kotelnikov
 */
public class XwikiEpubExporter {
    public static void main(String[] args) throws IOException, XmlException {
        String name = "portfolio5";
        Uri siteUrlPrefix = new Uri(
            "https://beebapp.ubimix.com/xwiki/bin/view/");
        String indexPageName = args.length > 0 ? args[0] : "a4/WebHome";

        String outputFileName = args.length > 1 ? args[1] : "./"
            + name
            + ".epub";
        // Used by templates to format pages. All CSS files, scripts etc are
        // loaded from this folder.
        final Uri resourceBaseUrl = new Uri("file://./template/");

        // See org.webreformatter.scrapper.core.AppContextConfigurator
        System.setProperty("app.config.file", "./config/app.json");
        System.setProperty("app.access.file", "./config/access.json");

        Uri indexUrl = new Uri(siteUrlPrefix + indexPageName);
        File outputFile = new File(outputFileName);

        XWikiSite xWikiSite = new XWikiSite(
            siteUrlPrefix,
            indexUrl,
            resourceBaseUrl);
        RemoteBookVisitor visitor = new RemoteBookVisitor(xWikiSite);
        EPubGenerator generator = new EPubGenerator(outputFile);
        visitor.visitBook(new PrintBookListener(generator) {
            @Override
            protected void println(String msg) {
                System.out.println(msg);
            }
        });

    }

}
