/**
 * 
 */
package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.ebook.remote.SiteExporter;
import org.webreformatter.ebook.remote.Site;

/**
 * @author kotelnikov
 */
public class PasSageEnSeineApp extends SiteExporter {

    public static void main(String[] args) throws IOException {
        new PasSageEnSeineApp(args).export();
    }

    public PasSageEnSeineApp(String... args) throws IOException {
        super(args);
    }

    @Override
    protected Site newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new PasSageEnSeineSite(propertyProvider);
    }

}
