/**
 * 
 */
package org.ubimix.ebook.remote.apps.passageenseine;

import java.io.IOException;

import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.ebook.remote.SiteExporter;
import org.ubimix.ebook.remote.Site;

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
