package org.webreformatter.ebook.remote.apps.passageenseine;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

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
                    fDate.setHour(12);
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
            private boolean buildContextBoxes(
                XmlContext context,
                Element node,
                String name) throws XmlException {
                if (!"div".equals(name)) {
                    return false;
                }
                String cls = node.getAttribute("class");
                if (!cls.equals("post insertPost cat-activisme")) {
                    return false;
                }
                boolean visit = false;
                XmlWrapper w = context.wrap(node);
                if (!visit) {
                    w.remove();
                } else {// Never visited
                    String imgSrc = w.evalStr(".//html:img/@src");
                    XmlWrapper titleTag = w
                        .eval(".//html:h3[@class='entry_title']");
                    String title = "";
                    String url = w.evalStr(".//html:a/@href");
                    if (titleTag != null) {
                        title = titleTag.toText();
                        url = titleTag.evalStr(".//html:a/@href");
                    }
                    XmlWrapper description = w
                        .eval(".//html:div[@class='entry_texte']");
                    if (description != null) {
                        description = description.newCopy("html:div");
                    }
                    XmlWrapper mediaBox = null;
                    if (imgSrc != null) {
                        mediaBox = context.newXML("html:div");
                        XmlWrapper a = mediaBox.appendElement("html:a");
                        a.setAttribute("href", url);
                        XmlWrapper img = a.appendElement("html:img");
                        img.setAttribute("src", imgSrc);
                    }
                    XmlWrapper descriptionBox = null;
                    if (description != null || title != null) {
                        descriptionBox = context.newXML("html:div");
                        if (title != null) {
                            XmlWrapper h3 = descriptionBox
                                .appendElement("html:h3");
                            h3.appendText(title);
                        }
                        if (description != null) {
                            descriptionBox.append(description);
                        }
                    }
                    XmlWrapper box = wrapInMediaBox(w, mediaBox, descriptionBox);
                    box.setAttribute(
                        "class",
                        "umx_media_box umx_sidebox umx_right");
                }
                return true;
            }

            private boolean buildIframeMediaBox(
                final XmlContext xmlContext,
                Element node,
                String name) throws XmlException {
                if (!"iframe".equals(name)) {
                    return false;
                }
                XmlWrapper w = xmlContext.wrap(node);
                String src = w.getAttribute("src");
                if (src == null || !src.contains("player.vimeo.com")) {
                    return false;
                }
                wrapInMediaBox(w);
                w.setAttribute("width", "100%");
                return true;
            }

            protected boolean buildMediaBox(
                final XmlContext xmlContext,
                Element node,
                String name) throws XmlException {
                if (!"div".equals(name)) {
                    return false;
                }
                String cls = node.getAttribute("class");
                if (!cls.contains("wp-caption")) {
                    return false;
                }
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
                    clearImageAttributes(img);
                    mediaDiv.append(img);
                }
                XmlWrapper captionDiv = w.appendElement("html:div");
                captionDiv.setAttribute("class", "umx_description");
                if (captionText != null) {
                    captionText.copyTo(captionDiv);
                }
                return true;
            }

            private boolean buildMediaBoxFromFullSizeImages(
                XmlContext xmlContext,
                Element node,
                String name) throws XmlException {
                if (!"img".equals(name)) {
                    return false;
                }
                String cls = node.getAttribute("class");
                if (!cls.contains("size-full")) {
                    return false;
                }
                XmlWrapper w = xmlContext.wrap(node);
                clearImageAttributes(w);
                wrapInMediaBox(w);
                return true;
            }

            private void clearImageAttributes(XmlWrapper img) {
                img.removeAttribute("width");
                img.removeAttribute("height");
                img.removeAttribute("class");
            }

            @Override
            public void visit(Element node) {
                boolean handled = false;
                try {
                    String name = XHTMLUtils.getHTMLName(node);
                    handled = buildIframeMediaBox(xmlContext, node, name)
                        || buildContextBoxes(xmlContext, node, name)
                        || buildMediaBoxFromFullSizeImages(
                            xmlContext,
                            node,
                            name) || buildMediaBox(xmlContext, node, name);
                    if (!handled) {
                        super.visit(node);
                    }
                } catch (Throwable t) {
                    throw RemoteResourcePresenter.onError(
                        RuntimeException.class,
                        "Can not transform a media box.",
                        t);
                }
            }

            private XmlWrapper wrapInMediaBox(XmlWrapper w) throws XmlException {
                return wrapInMediaBox(w, w, null);
            }

            private XmlWrapper wrapInMediaBox(
                XmlWrapper w,
                XmlWrapper media,
                XmlWrapper description) throws XmlException {
                XmlWrapper parent = w.getParent();
                XmlWrapper div = parent.appendElement("html:div");
                div.setAttribute("class", "umx_media_box");
                parent.insertBefore(w, div);
                w.remove();
                if (media != null) {
                    XmlWrapper mediaDiv = div.appendElement("html:div");
                    mediaDiv.setAttribute("class", "umx_media");
                    mediaDiv.append(media);
                }
                if (description != null) {
                    XmlWrapper mediaDiv = div.appendElement("html:div");
                    mediaDiv.setAttribute("class", "umx_description");
                    mediaDiv.append(description);
                }
                return div;
            }

        });
    }
}