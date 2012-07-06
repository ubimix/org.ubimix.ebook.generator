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
import org.webreformatter.ebook.remote.scrappers.IScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiIndexPageScrapper;

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
                    if (str.startsWith(BeebappPageScrapper.BASE_URL)) {
                        result = new BeebappPageScrapper(p);
                    } else if (str.startsWith(OwniPageScrapper.BASE_URL)) {
                        result = new OwniPageScrapper(p);
                    } else if (str.startsWith(StandlogScrapper.BASE_URL)) {
                        result = new StandlogScrapper(p);
                    } else if (str.startsWith(LeMondeScrapper.BASE_URL)) {
                        result = new LeMondeScrapper(p);
                    } else {
                        result = new AutoPageScrapper(p);
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

    public PasSageEnSeineSite(IVariableProvider propertyProvider)
        throws IOException {
        super(propertyProvider);
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new PasSageEnSeineScrapperFactory();
    }

    @Override
    protected IUrlProvider newUrlProvider() {
        return new CirclesUrlProvider(
            Arrays.<String> asList(BeebappPageScrapper.BASE_URL),
            Arrays.<String> asList(
                OwniPageScrapper.BASE_URL,
                LeMondeScrapper.BASE_URL,
                StandlogScrapper.BASE_URL));
    }

}