package org.webreformatter.ebook.remote.formatters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.json.ext.FormattedDate;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.bom.IBookToc.IBookTocItem;
import org.webreformatter.ebook.bom.json.JsonBookSection;
import org.webreformatter.ebook.bom.json.JsonBookToc;
import org.webreformatter.ebook.io.IOutput;
import org.webreformatter.ebook.remote.Site;
import org.webreformatter.ebook.remote.presenter.IContentPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;
import org.webreformatter.ebook.remote.presenter.PageUtils;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public class MinisiteFormatterFactory implements IFormatterFactory {

    /**
     * @author kotelnikov
     */
    public static class SimplePageFormatter extends AbstractPageFormatter {

        public SimplePageFormatter(
            InnerPagePresenter presenter,
            Uri baseResourceUri) {
            super(presenter, baseResourceUri);
        }

        private String buildMenu(IndexPagePresenter indexPresenter)
            throws IOException,
            XmlException {
            String result = "";
            if (indexPresenter != null) {
                JsonBookToc toc = indexPresenter.getBookToc();
                List<IBookTocItem> items = toc.getTocItems();
                result = formatTocItems(items, 0);
            }
            return result;
        }

        protected String formatTocItems(List<IBookTocItem> items, int level)
            throws IOException {
            String result;
            StringBuilder buf = new StringBuilder();
            Uri thisPath = fPresenter.getResourcePath();
            for (IBookTocItem item : items) {
                Uri path = item.getContentHref();
                if (path != null) {
                    path = thisPath.getRelative(path);
                }
                String label = PageUtils.escapeXml(item.getLabel());
                List<IBookTocItem> children = Collections.emptyList();
                if (level == 0) {
                    children = item.getChildren();
                }
                if (children.isEmpty()) {
                    buf.append("<li>");
                    if (path != null) {
                        buf.append("<a href='" + path + "'>" + label + "</a>");
                    } else {
                        buf.append(label);
                    }
                    buf.append("</li>\n");
                } else {
                    buf.append("<li class=\"dropdown\">");
                    String str = path != null ? path.toString() : "#";
                    buf.append("<a href='"
                        + str
                        + "' class='dropdown-toggle' data-toggle='dropdown'>"
                        + label
                        + "</a>\n");
                    String submenu = formatTocItems(children, level + 1);
                    buf.append(submenu);
                    buf.append("</li>\n");
                }
            }
            if (buf.length() > 0) {
                String listClass = level == 0 ? "nav" : "dropdown-menu";
                buf.insert(0, "<ul class=\"" + listClass + "\">\n");
                buf.append("</ul>\n");
            }
            result = buf.toString();
            return result;
        }

        protected IndexPagePresenter getIndexPresenter()
            throws IOException,
            XmlException {
            IndexPagePresenter indexPresenter = null;
            Site provider = fPresenter.getSite();
            Uri indexUrl = provider.getSiteUrl();
            IPresenter presenter = getPresenter(indexUrl, true);
            if (presenter instanceof IndexPagePresenter) {
                indexPresenter = (IndexPagePresenter) presenter;
            }
            return indexPresenter;
        }

        private List<Uri> getResourceUrls() {
            return toUrls(
                "assets/img/glyphicons-halflings-white.png",
                "assets/img/glyphicons-halflings.png");
        }

        private List<Uri> getScriptsUrls() {
            return toUrls(
                "assets/js/jquery.js",
                "assets/js/bootstrap-transition.js",
                "assets/js/bootstrap-alert.js",
                "assets/js/bootstrap-modal.js",
                "assets/js/bootstrap-dropdown.js",
                "assets/js/bootstrap-scrollspy.js",
                "assets/js/bootstrap-tab.js",
                "assets/js/bootstrap-tooltip.js",
                "assets/js/bootstrap-popover.js",
                "assets/js/bootstrap-button.js",
                "assets/js/bootstrap-collapse.js",
                "assets/js/bootstrap-carousel.js",
                "assets/js/bootstrap-typeahead.js");
        }

        private List<Uri> getStyleUrls() {
            return toUrls(
                "assets/css/spacelab/bootstrap.css",
                "assets/css/bootstrap-responsive.css",
                "assets/css/docs.css");
        }

        private List<Uri> toUrls(String... urls) {
            List<Uri> result = new ArrayList<Uri>();
            for (String str : urls) {
                // FIXME: change it
                Uri uri = new Uri(fBaseResourceUri + str);
                result.add(uri);
            }
            return result;
        }

        @Override
        public void writeTo(IOutput output) throws IOException {
            if (output == null) {
                return;
            }
            try {
                IndexPagePresenter indexPresenter = getIndexPresenter();
                String siteTitle = PageUtils.escapeXml(indexPresenter
                    .getBookMeta()
                    .getBookTitle());

                JsonBookSection section = fPresenter.getSection();
                String pageTitle = section.getTitle();
                pageTitle = PageUtils.escapeXml(pageTitle);

                final StringBuilder buf = new StringBuilder();
                buf.append("<!DOCTYPE html>\n");
                buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
                buf.append("<head>\n");
                buf.append("<meta charset=\"utf-8\"></meta>\n");
                buf.append("<meta http-equiv=\"Content-Type\" "
                    + "content=\"text/html; charset=utf-8\"></meta>\n");
                buf
                    .append("<meta name=\"viewport\" "
                        + "content=\"width=device-width, initial-scale=1.0\"></meta>");
                buf.append("<title>" + pageTitle + "</title>\n");

                appendStyles(buf, getStyleUrls());
                appendScripts(buf, getScriptsUrls());
                loadResources(getResourceUrls());

                buf.append("</head>");
                buf
                    .append("<body data-spy=\"scroll\" data-target=\".subnav\" data-offset=\"50\" data-twttr-rendered=\"true\">");
                buf
                    .append("<div class=\"navbar navbar-fixed-top\">\n"
                        + "   <div class=\"navbar-inner\">\n"
                        + "     <div class=\"container\">\n"
                        + "       <a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">\n"
                        + "         <span class=\"icon-bar\"></span>\n"
                        + "         <span class=\"icon-bar\"></span>\n"
                        + "         <span class=\"icon-bar\"></span>\n"
                        + "       </a>\n"
                        + "       <span class=\"brand\" href=\"#\">"
                        + siteTitle
                        + "</span>"
                        + "       <div class=\"nav-collapse\">\n"
                        + buildMenu(indexPresenter)
                        + "     </div>\n"
                        + "   </div>\n"
                        + "  </div>\n"
                        + "</div>\n");

                buf.append("<div class='container'>");

                String content = section.getContent();
                JsonObject properties = section.getValue(
                    "properties",
                    JsonObject.FACTORY);
                String authorInfo = "";
                if (properties != null) {
                    String authors = properties.getString("author");
                    String authorsUrl = properties.getString("authorUrl");
                    if (authors != null) {
                        authorInfo += "<p>";
                        if (authorsUrl != null) {
                            authorInfo += "<a href='" + authorsUrl + "'>";
                        }
                        authorInfo += PageUtils.escapeXml(authors);
                        if (authorsUrl != null) {
                            authorInfo += "</a>";
                        }
                        authorInfo += "</p>";
                    }
                }

                String lead = "";
                String url = properties.getString("url");
                String formattedUrl = "";
                if (url != null && !"".equals(url)) {
                    formattedUrl = "<a href='"
                        + "' class='umx_url'>"
                        + url
                        + "</a>";
                }

                String formattedDate = "";
                FormattedDate date = properties.getValue(
                    "date",
                    FormattedDate.FACTORY);
                if (date != null) {
                    int m = date.getMonth();
                    int d = date.getDay() + 1;
                    formattedDate = "  (<span class='umx_date'>"
                        + date.getYear()
                        + "/"
                        + (m < 10 ? "0" + m : "" + m)
                        + "/"
                        + (d < 10 ? "0" + d : "" + d)
                        + "</span>)";
                }

                if (!"".equals(formattedUrl) || !"".equals(formattedDate)) {
                    lead += "<p class='lead'>";
                    if (formattedUrl != null) {
                        lead += formattedUrl;
                    }
                    if (formattedDate != null) {
                        lead += formattedDate;
                    }
                    lead += "</p>";
                }

                buf.append("<header class='jumbotron subhead'>\n"
                    + "  <h1>"
                    + pageTitle
                    + "</h1>\n"
                    + lead
                    + authorInfo
                    + "</header>");

                buf.append("<section>");
                buf.append(content);
                buf.append("</section>");

                buf
                    .append("<br /><br /><br /><br /><br />"
                        + "<footer class=\"footer\">\n"
                        + "        <p class=\"pull-right\"><a href=\"#\">Back to top</a></p>\n"
                        + "      </footer>");
                buf.append("</div>");

                buf.append("</body>");
                buf.append("</html>");
                byte[] array = buf.toString().getBytes("UTF-8");
                output.write(array, 0, array.length);
            } catch (XmlException e) {
                throw new IOException(e);
            } finally {
                output.close();
            }
        }
    }

    private Uri fResourceBaseUrl;

    public MinisiteFormatterFactory(Uri resourceBaseUri) {
        fResourceBaseUrl = resourceBaseUri;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F extends IFormatter, P extends IContentPresenter> F getFormatter(
        P presenter,
        Class<F> viewType) throws IOException {
        IFormatter formatter = null;
        if (viewType == IInnerPageFormatter.class
            && presenter instanceof RemotePagePresenter) {
            formatter = new SimplePageFormatter(
                (InnerPagePresenter) presenter,
                fResourceBaseUrl);
        }
        return (F) formatter;
    }

}