package org.webreformatter.ebook.remote.scrappers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.webreformatter.commons.uri.Uri;
import org.webreformatter.ebook.remote.scrappers.PageScrapper.UrlProvider;

/**
 * @author kotelnikov
 */
public class CirclesUrlProvider extends UrlProvider {

    public static Set<Uri> getUrls(String... urls) {
        Set<Uri> result = new LinkedHashSet<Uri>();
        for (String url : urls) {
            Uri u = new Uri(url);
            result.add(u);
        }
        return result;
    }

    private List<String> fFirstLevel;

    private List<String> fSecondLevel;

    public CirclesUrlProvider(List<String> firstLevel, List<String> secondLevel) {
        fFirstLevel = firstLevel;
        fSecondLevel = secondLevel;
    }

    @Override
    public Uri getResourceUri(Uri parentUri, Uri resourceUri) {
        resourceUri = getNormalizedDownloadUri(parentUri, resourceUri);
        boolean include = isInFirstCercle(resourceUri)
            || (isInFirstCercle(parentUri) && isInSecondCercle(resourceUri));
        Uri result = include ? resourceUri : null;
        return result;
    }

    private boolean in(Uri resourceUri, List<String> prefixes) {
        String str = resourceUri.toString();
        boolean result = false;
        int len = prefixes != null ? prefixes.size() : 0;
        for (int i = 0; !result && i < len; i++) {
            String prefix = prefixes.get(i);
            result = str.startsWith(prefix);
        }
        return result;
    }

    protected boolean isInFirstCercle(Uri resourceUri) {
        return in(resourceUri, fFirstLevel);
    }

    protected boolean isInSecondCercle(Uri resourceUri) {
        return in(resourceUri, fSecondLevel);
    }

}