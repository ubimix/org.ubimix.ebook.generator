/**
 * 
 */
package org.ubimix.ebook.remote.apps.github;

import java.io.IOException;

import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.ebook.remote.SiteExporter;
import org.ubimix.ebook.remote.Site;

/**
 * @author kotelnikov
 */
public class GithubApp extends SiteExporter {

    public static void main(String[] args) throws IOException {
        new GithubApp(args).export();
    }

    public GithubApp(String... args) throws IOException {
        super(args);
    }

    @Override
    protected Site newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new GithubSite(propertyProvider);
    }

}
