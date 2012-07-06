package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;
import java.util.Arrays;

import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.AbstractConfiguredSite;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter.IUrlProvider;
import org.webreformatter.ebook.remote.scrappers.CirclesUrlProvider;
import org.webreformatter.ebook.remote.scrappers.GenericPageScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiIndexPageScrapper;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiInternalPageScrapper;

/**
 * @author kotelnikov
 */
public class PasSageEnSeineSite extends AbstractConfiguredSite {

    /**
     * @author kotelnikov
     */
    public static class PasSageEnSeineScrapperFactory
        implements
        IScrapperFactory {

        public PasSageEnSeineScrapperFactory() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S extends IScrapper, P extends IPresenter> S getScrapper(
            P presenter,
            Class<S> scrapperType) {
            IScrapper result = null;
            if (presenter instanceof RemotePagePresenter) {
                // HTML Pages
                RemotePagePresenter p = (RemotePagePresenter) presenter;
                if (scrapperType == IIndexPageScrapper.class) {
                    result = new XWikiIndexPageScrapper(p);
                } else if (scrapperType == IInnerPageScrapper.class) {
                    Uri pageUri = p.getResourceUrl();
                    String str = pageUri.toString();
                    if (str.startsWith(PasSageEnSeineSite.XWIKI_URL_BASE)) {
                        result = new XWikiInternalPageScrapper(p);
                    } else if (str.startsWith(PasSageEnSeineSite.OWNI_URL_BASE)) {
                        result = new OwniPageScrapper(p);
                    } else if (str.startsWith(PasSageEnSeineSite.STANDLOG_BASE)) {
                        result = new GenericPageScrapper(
                            p,
                            "//html:div[@class='post-content']",
                            "//html:div[@class='post']/*[@class='post-title']");
                    } else if (str.startsWith(PasSageEnSeineSite.LEMONDE_BASE)) {
                        result = new GenericPageScrapper(
                            p,
                            "//html:article[@class='article article_normal']/html:div[@class='txt15_140']",
                            "//html:article[@class='article article_normal']/html:h1");
                    } else {
                        // FIXME: !!!
                        result = new GenericPageScrapper(
                            p,
                            "//html:body",
                            "//html:title");
                    }
                }
            }
            if (result == null) {
                System.out.println("ERROR! Can not create a scrapper "
                    + "for this resource: '"
                    + presenter.getResourceUrl()
                    + "'.");
            }
            return (S) result;
        }
    }

    static final String LEMONDE_BASE = "http://www.lemonde.fr/";

    static final String OWNI_URL_BASE = "http://owni.fr/";

    // FIXME: remove it
    static final String STANDLOG_BASE = "http://standblog.org/blog/";

    static final String XWIKI_URL_BASE = "https://beebapp.ubimix.com/xwiki/bin/view/";

    private String fSitePrefixStr;

    public PasSageEnSeineSite(IVariableProvider propertyProvider)
        throws IOException {
        super(propertyProvider);
        fSitePrefixStr = propertyProvider.getValue("siteBaseUrl");
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new PasSageEnSeineScrapperFactory();
    }

    @Override
    protected IUrlProvider newUrlProvider() {
        return new CirclesUrlProvider(
            Arrays.<String> asList(XWIKI_URL_BASE),
            Arrays.<String> asList(OWNI_URL_BASE, LEMONDE_BASE, STANDLOG_BASE));
    }

}