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
import org.webreformatter.ebook.remote.AbstractConfiguredSite;
import org.webreformatter.ebook.remote.AbstractSiteExporter;
import org.webreformatter.ebook.remote.IRemoteResourceLoader;
import org.webreformatter.ebook.remote.RemoteResourceLoader;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.IUrlProvider;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.UrlProvider;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiScrapperFactory;

/**
 * @author kotelnikov
 */
public class XwikiEpubExporter extends AbstractSiteExporter {

    /**
     * @author kotelnikov
     */
    public static class XWikiSite extends AbstractConfiguredSite {

        private static Pattern WIKIPEDIA_LOCALIZED_URL = Pattern
            .compile("^(https?://\\w\\w\\.)(wikipedia.org/.*)$");

        private static Pattern WIKIPEDIA_URL = Pattern
            .compile("^(https?://.*?\\.wikipedia.org/.*)$");

        private String fSitePrefixStr;

        public XWikiSite(IVariableProvider propertyProvider) throws IOException {
            super(propertyProvider);
            fSitePrefixStr = getConfigValue("siteBaseUrl");
        };

        @Override
        protected IRemoteResourceLoader newResourceLoader() throws IOException {
            return new RemoteResourceLoader(fPropertyProvider);
        }

        @Override
        protected IScrapperFactory newScrapperFactory() {
            return new XWikiScrapperFactory();
        }

        @Override
        protected IUrlProvider newUrlProvider() {
            return new UrlProvider() {

                @Override
                public Uri getResourceUri(Uri parentUri, Uri resourceUri) {
                    resourceUri = getNormalizedDownloadUri(
                        parentUri,
                        resourceUri);
                    Uri result = null;
                    if (isInternalUri(resourceUri)) {
                        result = resourceUri;
                    } else if (isInternalUri(parentUri)) {
                        String str = resourceUri.toString();
                        if (WIKIPEDIA_URL.matcher(str).matches()) {
                            Matcher matcher = WIKIPEDIA_LOCALIZED_URL
                                .matcher(str);
                            if (matcher.matches()) {
                                String prefix = matcher.group(1);
                                String suffix = matcher.group(2);
                                String middle = "";
                                if (!suffix.startsWith("m.")) {
                                    middle = "m.";
                                }
                                result = new Uri(prefix + middle + suffix);
                            } else {
                                result = new Uri(str);
                            }
                        }
                    }
                    return result;
                }

                protected boolean isInternalUri(Uri resourceUri) {
                    String str = resourceUri.toString();
                    return str.startsWith(fSitePrefixStr);
                }

            };
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
    protected XWikiSite newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new XWikiSite(propertyProvider);
    }
}
