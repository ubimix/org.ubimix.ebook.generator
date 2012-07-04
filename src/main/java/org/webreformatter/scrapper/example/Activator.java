package org.webreformatter.scrapper.example;

import java.io.IOException;
import java.util.Dictionary;

import javax.servlet.Servlet;

import org.webreformatter.commons.osgi.ConfigurableMultiserviceActivator;
import org.webreformatter.commons.osgi.OSGIObjectActivator;
import org.webreformatter.commons.osgi.OSGIObjectDeactivator;
import org.webreformatter.commons.osgi.OSGIService;
import org.webreformatter.commons.strings.StringUtil;
import org.webreformatter.commons.strings.StringUtil.IVariableProvider;
import org.webreformatter.commons.xml.XmlException;
import org.webreformatter.scrapper.core.AppContext;
import org.webreformatter.scrapper.core.AppContextConfigurator;

/**
 * @author kotelnikov
 */
public class Activator extends ConfigurableMultiserviceActivator {

    private AppContext fAppContext;

    private EPubGeneratorServlet fEPubServlet;

    private IVariableProvider fProperyProvider = new IVariableProvider() {
        @Override
        public String getValue(String name) {
            Object value = fProperties.get(name);
            if (value == null) {
                value = System.getProperty(name);
            }
            return value != null ? value.toString() : null;
        }
    };

    @OSGIObjectActivator
    public void activate() throws IOException, XmlException {
        fAppContext = AppContextConfigurator.createAppContext(fProperyProvider);
        fEPubServlet = new EPubGeneratorServlet(fAppContext);
    }

    @OSGIObjectDeactivator
    public void deactivate() {
    }

    @OSGIService
    public Servlet getEpubGenerationServlet(Dictionary<String, String> params) {
        params.put("alias", "/epub/builder");
        return fEPubServlet;
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        String result = StringUtil.resolvePropertyByKey(key, fProperyProvider);
        if (result == null && defaultValue != null) {
            result = StringUtil.resolveProperty(defaultValue, fProperyProvider);
        }
        return result;
    }

    @Override
    protected String getServiceID() {
        return "org.webreformatter.ebooks";
    }

}
