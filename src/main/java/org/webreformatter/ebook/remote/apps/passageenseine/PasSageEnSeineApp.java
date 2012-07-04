/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.File;
import java.io.IOException;

import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.bem.PrintBookListener;
import org.webreformatter.ebook.bom.epub.EPubGenerator;
import org.webreformatter.ebook.remote.RemoteBookVisitor;

/**
 * @author kotelnikov
 */
public class PasSageEnSeineApp {

    public static void main(String[] args) throws IOException, XmlException {
        File outputFile = new File("./tmp/PasSageEnSeineApp.epub");
        PasSageEnSeineSite site = new PasSageEnSeineSite(
            "a5/WebHome",
            "file://./template/");
        RemoteBookVisitor visitor = new RemoteBookVisitor(site);
        EPubGenerator generator = new EPubGenerator(outputFile);
        visitor.visitBook(new PrintBookListener(generator) {
            @Override
            protected void println(String msg) {
                System.out.println(msg);
            }
        });

    }

}
