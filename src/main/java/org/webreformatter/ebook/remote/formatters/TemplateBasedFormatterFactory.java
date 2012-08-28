/**
 * 
 */
package org.webreformatter.ebook.remote.formatters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.json.ext.DateFormatter;
import org.webreformatter.commons.json.ext.FormattedDate;
import org.webreformatter.commons.templates.ITemplateProcessor;
import org.webreformatter.commons.templates.ITemplateProvider;
import org.webreformatter.commons.uri.Uri;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.ebook.bom.IBookToc.IBookTocItem;
import org.webreformatter.ebook.bom.json.JsonBookSection;
import org.webreformatter.ebook.bom.json.JsonBookToc;
import org.webreformatter.ebook.bom.json.JsonBookToc.JsonBookTocItem;
import org.webreformatter.ebook.io.IOutput;
import org.webreformatter.ebook.io.server.OutputToStream;
import org.webreformatter.ebook.remote.Site;
import org.webreformatter.ebook.remote.presenter.IContentPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenter;
import org.webreformatter.ebook.remote.presenter.IPresenterManager;
import org.webreformatter.ebook.remote.presenter.IndexPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter;
import org.webreformatter.ebook.remote.presenter.InnerPagePresenter.IInnerPageFormatter;
import org.webreformatter.ebook.remote.presenter.PageUtils;
import org.webreformatter.ebook.remote.presenter.RemotePagePresenter;

/**
 * @author kotelnikov
 */
public class TemplateBasedFormatterFactory implements IFormatterFactory {

    public static class DocumentContext {

        private IndexPagePresenter fIndexPresenter;

        private InnerPagePresenter fPresenter;

        public DocumentContext(InnerPagePresenter presenter) {
            fPresenter = presenter;
        }

        public String esc(String str) {
            return PageUtils.escapeXml(str);
        }

        private void findTocAbsolutePosition(
            List<IBookTocItem> tocItems,
            int[] pos,
            List<IBookTocItem> result) {
            if (tocItems != null) {
                Uri url = fPresenter.getResourceUrl();
                if (url != null) {
                    for (int i = 0; i < tocItems.size(); i++) {
                        JsonBookTocItem item = (JsonBookTocItem) tocItems
                            .get(i);
                        Uri itemUri = RemotePagePresenter.getFullUrl(item);
                        if (itemUri != null) {
                            result.add(item);
                            if (pos[0] < 0) {
                                if (url.equals(itemUri)) {
                                    pos[0] = -(pos[0] + 1);
                                } else {
                                    pos[0]--;
                                }
                            }
                        }
                        findTocAbsolutePosition(item.getChildren(), pos, result);
                    }
                }
            }
        }

        private void findTocPosition(
            List<IBookTocItem> tocItems,
            int[] pos,
            List<IBookTocItem> result) {
            if (tocItems != null) {
                Uri url = fPresenter.getResourceUrl();
                if (url != null) {
                    boolean found = false;
                    for (int i = 0; pos[0] < 0 && i < tocItems.size(); i++) {
                        JsonBookTocItem item = (JsonBookTocItem) tocItems
                            .get(i);
                        Uri itemUri = RemotePagePresenter.getFullUrl(item);
                        if (url.equals(itemUri)) {
                            pos[0] = i;
                            found = true;
                        } else {
                            findTocPosition(item.getChildren(), pos, result);
                        }
                    }
                    if (found) {
                        for (IBookTocItem item : tocItems) {
                            Uri itemUri = RemotePagePresenter
                                .getFullUrl((JsonBookTocItem) item);
                            if (itemUri != null) {
                                result.add(item);
                            }
                        }
                    }
                }
            }
        }

        public String formatDate(
            FormattedDate date,
            String template,
            String locale) {
            if (date == null) {
                return null;
            }
            // DateFormatSymbols symbols = new DateFormatSymbols(new
            // Locale(locale));
            // String[] months = symbols.getMonths();
            //
            SimpleDateFormat format = new SimpleDateFormat(
                template,
                new Locale(locale));
            format.setTimeZone(TimeZone.getTimeZone("PST"));
            return format.format(DateFormatter.getDateTime(date));
        }

        public String formatDate(String date, String template, String locale) {
            if (date == null) {
                return null;
            }
            String result = formatDate(
                new FormattedDate(date),
                template,
                locale);
            return result;
        }

        public List<IBookTocItem> getChildren(IBookTocItem item) {
            return item.getChildren();
        }

        public String getContent() throws XmlException, IOException {
            JsonBookSection section = fPresenter.getSection();
            String content = section.getContent();
            return content;
        }

        protected IndexPagePresenter getIndexPresenter()
            throws IOException,
            XmlException {
            if (fIndexPresenter == null) {
                IndexPagePresenter indexPresenter = null;
                Site provider = fPresenter.getSite();
                Uri indexUrl = provider.getSiteUrl();
                IPresenter presenter = getPresenter(indexUrl, true);
                if (presenter instanceof IndexPagePresenter) {
                    indexPresenter = (IndexPagePresenter) presenter;
                }
                fIndexPresenter = indexPresenter;
            }
            return fIndexPresenter;
        }

        public IBookTocItem getNextTocItem() throws XmlException, IOException {
            return getNextTocItem(true);
        }

        /**
         * @param flat if this flag is <code>true</code> then this method
         *        considers that all pages are on the same level (without
         *        hierarchy); otherwise it returns the next page on the same
         *        level of the TOC
         * @return the next TOC item at the same level
         * @throws IOException
         * @throws XmlException
         */
        public IBookTocItem getNextTocItem(boolean flat)
            throws XmlException,
            IOException {
            IndexPagePresenter indexPresenter = getIndexPresenter();
            JsonBookToc toc = indexPresenter.getBookToc();
            int[] pos = { -1 };
            List<IBookTocItem> items = new ArrayList<IBookTocItem>();
            if (flat) {
                findTocAbsolutePosition(toc.getTocItems(), pos, items);
            } else {
                findTocPosition(toc.getTocItems(), pos, items);
            }
            IBookTocItem result = (pos[0] >= 0 && pos[0] < items.size() - 1)
                ? items.get(pos[0] + 1)
                : null;
            return result;
        }

        protected IPresenter getPresenter(Uri uri, boolean create)
            throws IOException,
            XmlException {
            IPresenter presenter = fPresenter
                .getSite()
                .getPresenterManager()
                .getPresenter(uri, create);
            return presenter;
        }

        public IBookTocItem getPreviousTocItem()
            throws XmlException,
            IOException {
            return getPreviousTocItem(true);
        }

        /**
         * @param flat if this flag is <code>true</code> then this method
         *        considers that all pages are on the same level (without
         *        hierarchy); otherwise it returns the previous page on the same
         *        level of the TOC
         * @return the next TOC item at the same level
         * @throws IOException
         * @throws XmlException
         */
        public IBookTocItem getPreviousTocItem(boolean flat)
            throws XmlException,
            IOException {
            IndexPagePresenter indexPresenter = getIndexPresenter();
            JsonBookToc toc = indexPresenter.getBookToc();
            int[] pos = { -1 };
            List<IBookTocItem> items = new ArrayList<IBookTocItem>();
            if (flat) {
                findTocAbsolutePosition(toc.getTocItems(), pos, items);
            } else {
                findTocPosition(toc.getTocItems(), pos, items);
            }
            IBookTocItem result = (pos[0] > 0 && pos[0] < items.size()) ? items
                .get(pos[0] - 1) : null;
            return result;
        }

        public String getProperty(String key) throws XmlException, IOException {
            return getProperty("properties", key);
        }

        protected String getProperty(String type, String key)
            throws XmlException,
            IOException {
            JsonBookSection section = fPresenter.getSection();
            JsonObject properties = section.getValue(type, JsonObject.FACTORY);
            String result = null;
            if (properties != null) {
                result = properties.getString(key);
            }
            return result;
        }

        public String getSiteTitle() throws IOException, XmlException {
            IndexPagePresenter indexPresenter = getIndexPresenter();
            String siteTitle = esc(indexPresenter.getBookMeta().getBookTitle());
            return siteTitle;
        }

        public String getSystemProperty(String key)
            throws XmlException,
            IOException {
            return getProperty("systemProperties", key);
        }

        public String getTitle() throws XmlException, IOException {
            JsonBookSection section = fPresenter.getSection();
            String title = section.getTitle();
            return esc(title);
        }

        public List<IBookTocItem> getToc() throws XmlException, IOException {
            IndexPagePresenter indexPresenter = getIndexPresenter();
            JsonBookToc toc = indexPresenter.getBookToc();
            List<IBookTocItem> items = toc.getTocItems();
            return items;
        }

        public boolean hasChildren(IBookTocItem item) {
            return !getChildren(item).isEmpty();
        }

        public boolean isActive(String path) throws IOException {
            return isActive(new Uri(path));
        }

        public boolean isActive(Uri path) throws IOException {
            Uri resourcePath = fPresenter.getResourcePath();
            return resourcePath.equals(path);
        }

        public String pathTo(String path) throws IOException {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return pathTo(new Uri(path));
        }

        public String pathTo(Uri path) throws IOException {
            Uri resourcePath = fPresenter.getResourcePath();
            Uri result = resourcePath.getRelative(path);
            return result.toString();
        }

        public String urlToPath(String url) throws IOException, XmlException {
            return urlToPath(new Uri(url));
        }

        public String urlToPath(Uri uri) throws IOException, XmlException {
            String result = null;
            IPresenter presenter = getPresenter(uri, true);
            if (presenter instanceof IContentPresenter) {
                Uri resourcePath = ((IContentPresenter) presenter)
                    .getResourcePath();
                result = pathTo(resourcePath);
            }
            return result;
        }

    }

    /**
     * @author kotelnikov
     */
    public static class TemplateBasedFormatter implements IFormatter {

        private final static Logger log = Logger
            .getLogger(TemplateBasedFormatter.class.getName());

        private static IOException handleError(String msg, Throwable e) {
            if (e instanceof IOException) {
                return (IOException) e;
            }
            log.log(Level.WARNING, msg, e);
            return new IOException(msg, e);
        }

        private InnerPagePresenter fPresenter;

        private File fResourceFolder;

        private ITemplateProcessor fTemplateProcessor;

        /**
         * @param resourceFolder
         * @param templateProcessor
         * @param presenter
         */
        public TemplateBasedFormatter(
            InnerPagePresenter presenter,
            ITemplateProcessor templateProcessor,
            File resourceFolder) {
            fPresenter = presenter;
            fTemplateProcessor = templateProcessor;
            fResourceFolder = resourceFolder;
        }

        private void copyResources(IPresenterManager presenterManager, File file)
            throws IOException,
            XmlException {
            if (file.isDirectory()) {
                File[] list = file.listFiles();
                if (list != null) {
                    for (File child : list) {
                        copyResources(presenterManager, child);
                    }
                }
            } else if (file.isFile()) {
                String strUrl = file.toURI() + "";
                if (!strUrl.endsWith(".vm")) {
                    Uri url = new Uri(strUrl);
                    presenterManager.getPresenter(url, true);
                }
            }
        }

        protected String getTemplateName(JsonBookSection section)
            throws IOException {
            String type = section.getString("type");
            String templateName = null;
            if (type != null) {
                templateName = type + ".vm";
                ITemplateProvider resourceProvider = fTemplateProcessor
                    .getResourceProvider();
                if (!resourceProvider.templateExists(templateName)) {
                    templateName = null;
                }
            }
            if (templateName == null) {
                templateName = "main.vm";
            }
            String folder = fPresenter.getResourcePathFolder();
            templateName = folder + templateName;
            return templateName;
        }

        /**
         * @see org.webreformatter.ebook.remote.formatters.IFormatter#writeTo(org.webreformatter.ebook.io.IOutput)
         */
        @Override
        public void writeTo(IOutput output) throws IOException {
            try {
                OutputToStream stream = new OutputToStream(output);
                OutputStreamWriter writer = new OutputStreamWriter(stream);

                JsonBookSection section = fPresenter.getSection();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("context", new DocumentContext(fPresenter));
                params.put("presenter", fPresenter);
                params.put("doc", section);

                String templateName = getTemplateName(section);
                fTemplateProcessor.render(templateName, params, writer);
                writer.flush();
                writer.close();
                stream.close();

                Site site = fPresenter.getSite();
                IPresenterManager presenterManager = site.getPresenterManager();
                copyResources(presenterManager, fResourceFolder);
            } catch (Exception e) {
                throw handleError("Can not render an XML document.", e);
            } finally {
                output.close();
            }
        }
    }

    private File fResourceFolder;

    private ITemplateProcessor fTemplateProcessor;

    /**
     * 
     */
    public TemplateBasedFormatterFactory(
        ITemplateProcessor templateProcessor,
        File resources) {
        fTemplateProcessor = templateProcessor;
        fResourceFolder = resources;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends IFormatter, P extends IContentPresenter> F getFormatter(
        P presenter,
        Class<F> viewType) throws IOException {
        IFormatter formatter = null;
        if (viewType == IInnerPageFormatter.class
            && presenter instanceof RemotePagePresenter) {
            formatter = new TemplateBasedFormatter(
                (InnerPagePresenter) presenter,
                fTemplateProcessor,
                fResourceFolder);
        }
        return (F) formatter;
    }

}
