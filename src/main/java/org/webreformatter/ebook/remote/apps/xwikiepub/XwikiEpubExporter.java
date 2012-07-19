/**
 * 
 */
package org.webreformatter.ebook.remote.apps.xwikiepub;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.templates.TemplateException;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.remote.Site;
import org.webreformatter.ebook.remote.SiteExporter;
import org.webreformatter.ebook.remote.RemoteResourceLoader;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public class XwikiEpubExporter extends SiteExporter {

    /**
     * @author kotelnikov
     */
    public static class XWikiSite extends Site {

        private static Pattern WIKIPEDIA_LOCALIZED_URL = Pattern
            .compile("^(https?://\\w\\w\\.)(wikipedia.org/.*)$");

        private static Pattern WIKIPEDIA_URL = Pattern
            .compile("^(https?://.*?\\.wikipedia.org/.*)$");

        public XWikiSite(IVariableProvider propertyProvider) throws IOException {
            super(propertyProvider);
        };

        @Override
        public Uri getRemoteResourceUrl(Uri resourceUri) {
            if (isInFirstCercle(resourceUri)) {
                Uri.Builder builder = resourceUri.getBuilder();
                builder.addParam("basicauth", "1");
                resourceUri = builder.build();
            } else {
                String str = resourceUri.toString();
                if (WIKIPEDIA_URL.matcher(str).matches()) {
                    Matcher matcher = WIKIPEDIA_LOCALIZED_URL.matcher(str);
                    if (matcher.matches()) {
                        String prefix = matcher.group(1);
                        String suffix = matcher.group(2);
                        String middle = "";
                        if (!suffix.startsWith("m.")) {
                            middle = "m.";
                        }
                        resourceUri = new Uri(prefix + middle + suffix);
                    } else {
                        resourceUri = new Uri(str);
                    }
                }
            }
            return resourceUri;
        }

        @Override
        protected RemoteResourceLoader newResourceLoader() throws IOException {
            return new RemoteResourceLoader(fPropertyProvider);
        }

        @Override
        protected IScrapperFactory newScrapperFactory() {
            return new XWikiScrapperFactory();
        }

    }

    public static void main(String[] args)
        throws IOException,
        XmlException,
        TemplateException {
        new XwikiEpubExporter(args).export();
    }

    public XwikiEpubExporter(String... args)
        throws IOException,
        TemplateException {
        super(args);
    }

    @Override
    protected Site newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new XWikiSite(propertyProvider);
    }
}
