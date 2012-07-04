package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;
import org.webreformatter.commons.json.ext.DateFormatter;
import org.webreformatter.commons.json.ext.FormattedDate;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XHTMLUtils;
import org.webreformatter.commons.xml.XmlAcceptor;
import org.webreformatter.commons.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.ebook.remote.AbstractSite;
import org.webreformatter.ebook.remote.formatters.IFormatterFactory;
import org.webreformatter.ebook.remote.formatters.MinisiteFormatterFactory;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter.IIndexPageScrapper;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageScrapper;
import org.webreformatter.ebook.remote.presenter.PresenterManager;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.RemoteResourcePresenter;
import org.webreformatter.ebook.remote.scrappers.CirclesUrlProvider;
import org.webreformatter.ebook.remote.scrappers.IScrapper;
import org.webreformatter.ebook.remote.scrappers.IScrapperFactory;
import org.webreformatter.ebook.remote.scrappers.PageScrapper;
import org.webreformatter.ebook.remote.scrappers.PageScrapper.IUrlProvider;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiIndexPageScrapper;
import org.webreformatter.ebook.remote.scrappers.xwiki.XWikiInternalPageScrapper;

/**
 * @author kotelnikov
 */
public class PasSageEnSeineSite extends AbstractSite {

    /**
     * @author kotelnikov
     */
    public static class GenericPageScrapper extends PageScrapper
        implements
        IInnerPageScrapper {

        protected XmlWrapper fContent;

        private String fContentXPath;

        protected String fTitle;

        private String fTitleXPath;

        public GenericPageScrapper(
            IUrlProvider urlProvider,
            RemotePagePresenter presenter,
            String contentXPath,
            String titleXPath) {
            super(urlProvider, presenter);
            fContentXPath = contentXPath;
            fTitleXPath = titleXPath;
        }

        @Override
        public XmlWrapper getContent() throws XmlException {
            splitContent();
            return fContent;
        }

        @Override
        public String getTitle() throws XmlException {
            splitContent();
            return fTitle;
        }

        protected void onSplitContent() throws XmlException {
        }

        private void splitContent() throws XmlException {
            if (fContent == null) {
                XmlWrapper page = getPage();
                fContent = page.eval(fContentXPath);
                XmlWrapper titleElement = page.eval(fTitleXPath);
                if (titleElement == null) {
                    titleElement = page.eval("//html:title");
                }
                if (titleElement != null) {
                    titleElement.remove();
                    fTitle = titleElement.toText();
                }
                onSplitContent();
            }
        }

    }

    public static class OwniPageScrapper extends GenericPageScrapper {

        private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "dd MMMMM yyyy",
            Locale.FRANCE);

        private String fAuthorRef;

        private String fAuthors;

        private FormattedDate fDate;

        public OwniPageScrapper(
            IUrlProvider urlProvider,
            RemotePagePresenter presenter) {
            super(
                urlProvider,
                presenter,
                "//html:div[@class='entry_texte']",
                "//html:h1[@class='entry_title']");
        }

        @Override
        public Map<String, Object> getProperties() throws XmlException {
            Map<String, Object> properties = super.getProperties();
            properties.put("date", fDate);
            properties.put("authorUrl", fAuthorRef);
            properties.put("author", fAuthors);
            return properties;
        }

        @Override
        protected void onSplitContent() throws XmlException {
            XmlWrapper page = getPage();
            XmlWrapper meta = page.eval("//html:div[@class='metaPost']");
            if (meta != null) {
                String str = meta.evalStr(".//html:span[@class='date']");
                if (str != null) {
                    str = str.trim().toLowerCase();
                    if (str.startsWith("le ")) {
                        str = str.substring("le ".length());
                        str = str.trim();
                    }
                    try {
                        Date date = DATE_FORMAT.parse(str);
                        fDate = DateFormatter.formatDate(date);
                    } catch (ParseException e) {
                    }
                }
                XmlWrapper authorRefTag = meta.eval(".//html:a[@rel='author']");
                if (authorRefTag != null) {
                    fAuthors = authorRefTag.toText();
                    fAuthorRef = authorRefTag.getAttribute("href");
                }
            }

            final XmlContext xmlContext = fContent.getXmlContext();
            XmlAcceptor.accept(fContent.getRootElement(), new XmlVisitor() {
                protected void buildIframeMediaBox(
                    final XmlContext xmlContext,
                    Element node) throws XmlException {
                    XmlWrapper w = xmlContext.wrap(node);
                    String src = w.getAttribute("src");
                    if (src == null || !src.contains("player.vimeo.com")) {
                        return;
                    }
                    wrapInMediaBox(w);
                }

                protected void buildMediaBox(
                    final XmlContext xmlContext,
                    Element node) throws XmlException {
                    String cls = node.getAttribute("class");
                    if (cls.contains("wp-caption")) {
                        XmlWrapper w = xmlContext.wrap(node);
                        w.removeAttribute("class");
                        w.removeAttribute("style");
                        w.removeAttribute("id");
                        w.setAttribute("class", "umx_media_box");
                        XmlWrapper img = w.eval(".//html:img");
                        XmlWrapper captionText = w
                            .eval(".//*[@class='wp-caption-text']");
                        w.removeChildren();
                        XmlWrapper mediaDiv = w.appendElement("html:div");
                        mediaDiv.setAttribute("class", "umx_media");
                        if (img != null) {
                            mediaDiv.append(img);
                        }
                        XmlWrapper captionDiv = w.appendElement("html:div");
                        captionDiv.setAttribute("class", "umx_description");
                        if (captionText != null) {
                            captionText.copyTo(captionDiv);
                        }
                    }
                }

                private void buildMediaBoxFromFullSizeImages(
                    XmlContext xmlContext,
                    Element node) throws XmlException {
                    String cls = node.getAttribute("class");
                    if (cls.contains("size-full")) {
                        XmlWrapper w = xmlContext.wrap(node);
                        w.removeAttribute("class");
                        wrapInMediaBox(w);
                    }
                }

                @Override
                public void visit(Element node) {
                    String name = XHTMLUtils.getHTMLName(node);
                    try {
                        if ("iframe".equals(name)) {
                            buildIframeMediaBox(xmlContext, node);
                        } else if ("div".equals(name)) {
                            buildMediaBox(xmlContext, node);
                        } else if ("img".equals(name)) {
                            buildMediaBoxFromFullSizeImages(xmlContext, node);
                        }
                    } catch (Throwable t) {
                        throw RemoteResourcePresenter.onError(
                            RuntimeException.class,
                            "Can not transform a media box.",
                            t);
                    }
                    super.visit(node);
                }

                private void wrapInMediaBox(XmlWrapper w) throws XmlException {
                    XmlWrapper parent = w.getParent();
                    XmlWrapper div = parent.appendElement("html:div");
                    parent.insertBefore(w, div);
                    div.setAttribute("class", "umx_media_box");
                    XmlWrapper mediaDiv = div.appendElement("html:div");
                    mediaDiv.setAttribute("class", "umx_media");
                    mediaDiv.append(w);
                }

            });
        }
    }

    /**
     * @author kotelnikov
     */
    public static class PasSageEnSeineScrapperFactory
        implements
        IScrapperFactory {

        private IUrlProvider fUrlProvider;

        public PasSageEnSeineScrapperFactory() {
            fUrlProvider = new CirclesUrlProvider(
                Arrays.<String> asList(XWIKI_URL_BASE),
                Arrays.<String> asList(OWNI_URL_BASE));
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
                    result = new XWikiIndexPageScrapper(fUrlProvider, p);
                } else if (scrapperType == IInnerPageScrapper.class) {
                    Uri pageUri = p.getResourceUrl();
                    String str = pageUri.toString();
                    if (str.startsWith(XWIKI_URL_BASE)) {
                        result = new XWikiInternalPageScrapper(fUrlProvider, p);
                    } else if (str.startsWith(OWNI_URL_BASE)) {
                        result = new OwniPageScrapper(fUrlProvider, p);
                    } else if (str.startsWith(STANDLOG_BASE)) {
                        result = new GenericPageScrapper(
                            fUrlProvider,
                            p,
                            "//html:div[@class='post-content']",
                            "//html:div[@class='post']/*[@class='post-title']");
                    } else {
                        // FIXME: !!!
                        result = new GenericPageScrapper(
                            fUrlProvider,
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

    private static final String OWNI_URL_BASE = "http://owni.fr/";

    // FIXME: remove it
    private static final String STANDLOG_BASE = "http://standblog.org/blog/";

    private static final String XWIKI_URL_BASE = "https://beebapp.ubimix.com/xwiki/bin/view/";

    private Uri fResourceBaseUri;

    public PasSageEnSeineSite(String indexPageName, String resourceBaseUri)
        throws IOException {
        super(new Uri(XWIKI_URL_BASE + indexPageName));
        fResourceBaseUri = new Uri(resourceBaseUri);
    }

    @Override
    protected IFormatterFactory newFormatterFactory() {
        return new MinisiteFormatterFactory(fResourceBaseUri);
    }

    @Override
    protected IPresenterManager newPresenterManager() throws IOException {
        return new PresenterManager(this, fResourceBaseUri);
    }

    @Override
    protected IScrapperFactory newScrapperFactory() {
        return new PasSageEnSeineScrapperFactory();
    }

}