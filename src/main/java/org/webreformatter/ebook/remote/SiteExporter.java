/**
 * 
 */
package org.webreformatter.ebook.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.webreformatter.commons.io.IOUtil;
import org.webreformatter.commons.json.JsonObject;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.ebook.bem.PrintBookListener;
import org.webreformatter.ebook.bom.epub.EPubGenerator;

/**
 * @author kotelnikov
 */
public class SiteExporter {

    private static Map<String, String> getConfigMap(String... args) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < args.length;) {
            String key = args[i++];
            String value = i < args.length ? args[i++] : null;
            map.put(key, value);
        }
        return map;
    }

    private IVariableProvider fPropertyProvider;

    public SiteExporter(String... args) throws IOException {
        final Map<String, String> configMap = getConfigMap(args);
        File configFile = new File("./config/app.json");
        String config = IOUtil.readString(configFile);
        final JsonObject jsonConfig = JsonObject.FACTORY.newValue(config);
        fPropertyProvider = new IVariableProvider() {
            @Override
            public String getValue(String name) {
                String value = System.getProperty(name);
                if (value == null) {
                    value = configMap.get(name);
                    if (value == null) {
                        value = jsonConfig.getString(name);
                    }
                }
                return value;
            }
        };
    }

    public void export() throws IOException {
        File epubOutputFile = new File(getConfigValue("epubOutputFile"));
        Site site = newSite(fPropertyProvider);
        RemoteBookVisitor visitor = new RemoteBookVisitor(site);
        EPubGenerator generator = new EPubGenerator(epubOutputFile);
        visitor.visitBook(new PrintBookListener(generator) {
            @Override
            protected void println(String msg) {
                SiteExporter.this.println(msg);
            }
        });
        if (epubOutputFile.exists()) {
            File siteOutputDir = new File(getConfigValue("siteOutputDir"));
            if (siteOutputDir.isDirectory()) {
                File[] children = siteOutputDir.listFiles();
                if (children != null) {
                    for (File child : children) {
                        IOUtil.delete(child);
                    }
                }
            }
            unzip(epubOutputFile, siteOutputDir);
        }
    }

    protected String getConfigValue(String key) {
        return StringUtil.resolvePropertyByKey(key, fPropertyProvider);
    }

    protected Site newSite(IVariableProvider propertyProvider)
        throws IOException {
        return new Site(propertyProvider);
    }

    protected void println(String msg) {
        System.out.println(msg);
    }

    protected void unzip(File zipFileName, File siteOutputDir)
        throws IOException {
        ZipFile zipFile = new ZipFile(zipFileName);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            println("Extracting file: " + entry.getName());
            File file = new File(siteOutputDir, entry.getName());
            IOUtil.delete(file);
            file.getParentFile().mkdirs();
            InputStream input = zipFile.getInputStream(entry);
            try {
                FileOutputStream output = new FileOutputStream(file);
                try {
                    IOUtil.copy(input, output);
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        }
    }
}
