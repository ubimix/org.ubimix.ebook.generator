package org.ubimix.ebook.remote.apps.github;

import java.io.IOException;
import java.util.Arrays;

import org.ubimix.commons.strings.StringUtil.IVariableProvider;
import org.ubimix.commons.uri.Uri;
import org.ubimix.ebook.remote.Site;
import org.ubimix.ebook.remote.presenter.IPresenter;
import org.ubimix.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.ubimix.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.ubimix.ebook.remote.presenter.RemotePagePresenter;
import org.ubimix.ebook.remote.scrapers.IScrapper;
import org.ubimix.ebook.remote.scrapers.IScraperFactory;
import org.ubimix.ebook.remote.scrapers.github.GithubScraper;
import org.ubimix.ebook.remote.scrapers.xwiki.XWikiIndexPageScrapper;

/**
 * @author kotelnikov
 */
public class GithubSite extends Site {

	/**
	 * @author kotelnikov
	 */
	public static class GithubScraperFactory implements IScraperFactory {

		public GithubScraperFactory() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S extends IScrapper, P extends IPresenter> S getScrapper(
				P presenter, Class<S> scrapperType) {
			IScrapper result = null;
			if (presenter instanceof RemotePagePresenter) {
				// HTML Pages
				RemotePagePresenter p = (RemotePagePresenter) presenter;
				if (scrapperType == IIndexPageScrapper.class) {
					result = new XWikiIndexPageScrapper(p);
				} else if (scrapperType == IInnerPageScrapper.class) {
					Uri pageUri = p.getResourceUrl();
					String str = pageUri.toString();
					if (str.startsWith(FactoryPageScraper.BASE_URL)) {
						result = new FactoryPageScraper(p);
					} else if (str.startsWith(GithubScraper.BASE_URL)) {
						result = new GithubScraper(p);
					} 
				}
			}
			if (result == null) {
				System.out.println("ERROR! Can not create a scrapper "
						+ "for this resource: '" + presenter.getResourceUrl()
						+ "'.");
			}
			return (S) result;
		}
	}

	public GithubSite(IVariableProvider propertyProvider)
			throws IOException {
		super(propertyProvider);

		addSiteUrlPrefixes(
				Arrays.<String> asList(FactoryPageScraper.BASE_URL),
				Arrays.<String> asList(GithubScraper.BASE_URL));

	}

	@Override
	public Uri getRemoteResourceUrl(Uri resourceUri) {
		if (isInFirstCercle(resourceUri)) {
			Uri.Builder builder = resourceUri.getBuilder();
			builder.addParam("basicauth", "1");
			resourceUri = builder.build();
		}
		return resourceUri;
	}

	@Override
	protected IScraperFactory newScraperFactory() {
		return new GithubScraperFactory();
	}

}