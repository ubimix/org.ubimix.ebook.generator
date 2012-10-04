/**
 * 
 */
package org.ubimix.ebook.remote.apps.ivry94;

import java.io.IOException;

import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.ebook.remote.SiteExporter;
import org.ubimix.ebook.remote.Site;

/**
 * @author kotelnikov
 */
public class IvrySurSeineApp extends SiteExporter {

    public static void main(String[] args) throws IOException {
        new IvrySurSeineApp(args).export();
    }

    public IvrySurSeineApp(String... args) throws IOException {
        super(args);
    }

    @Override
    protected Site newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new IvrySurSeineSite(propertyProvider);
    }

}
