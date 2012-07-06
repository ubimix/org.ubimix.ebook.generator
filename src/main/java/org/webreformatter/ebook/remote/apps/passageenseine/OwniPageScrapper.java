package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;
import org.webreformatter.commons.json.ext.DateFormatter;
import org.webreformatter.commons.json.ext.FormattedDate;
import org.webreformatter.commons.xml.XHTMLUtils;
import org.webreformatter.commons.xml.XmlAcceptor;
import org.webreformatter.commons.xml.XmlAcceptor.XmlVisitor;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.commons.xml.XmlWrapper;
import org.webreformatter.commons.xml.XmlWrapper.XmlContext;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;
import org.webreformatter.ebook.remote.presenter.RemoteResourcePresenter;
import org.webreformatter.ebook.remote.scrappers.GenericPageScrapper;

public class OwniPageScrapper extends GenericPageScrapper {

    public static final String BASE_URL = "http://owni.fr/";

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
        "dd MMMMM yyyy",
        Locale.FRANCE);

    private String fAuthorRef;

    private String fAuthors;

    private FormattedDate fDate;

    public OwniPageScrapper(RemotePagePresenter presenter) {
        super(
            presenter,
            "//html:div[@class='entry_texte']",
            "//html:h1[@class='entry_title']");
    }

    @Override
    public Map<String, Object> getHtmlProperties()
        throws XmlException,
        IOException {
        Map<String, Object> properties = super.getHtmlProperties();
        properties.put("date", fDate);
        properties.put("pageAuthorUrl", fAuthorRef);
        properties.put("pageAuthor", fAuthors);
        properties.put("url", fPresenter.getResourceUrl());
        return properties;
    }

    @Override
    protected void onSplitContent() throws XmlException, IOException {
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