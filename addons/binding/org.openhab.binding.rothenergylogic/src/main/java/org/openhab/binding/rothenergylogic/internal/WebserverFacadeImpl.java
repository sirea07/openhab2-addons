package org.openhab.binding.rothenergylogic.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.openhab.binding.rothenergylogic.internal.model.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebserverFacadeImpl implements WebserverFacade, BindingConfigurationListener {

    private Hashtable<String, Thermostat> thermostats = new Hashtable<>();
    private Hashtable<String, ThermostatUpdateListener> updateListeners = new Hashtable<>();
    private WebserverResponseParser parser;
    private int blockTimeForNextExplicitRefresh = 5;
    private Date nextExplicitRefresh = new Date();
    private BindingConfiguration bindingConfig;
    private BindingConfigurationManager bindingConfigManager;

    public static final String XML_ITEMS = "item_list";

    public static final String KEY_WEBSERVER_ID = "kurzID";
    public static final String KEY_WEBSERVER_OWNER_ID = "ownerKurzID";
    public static final String KEY_WEBSERVER_NAME = "name";
    public static final String KEY_WEBSERVER_TEMPERATURE_ACTUAL = "RaumTemp";
    public static final String KEY_WEBSERVER_TEMPERATURE_SETPOINT = "SollTemp";
    public static final String KEY_WEBSERVER_TEMPERATURE_SETPOINT_MIN = "SollTempMinVal";
    public static final String KEY_WEBSERVER_TEMPERATURE_SETPOINT_MAX = "SollTempMaxVal";
    public static final String KEY_WEBSERVER_TEMPERATURE_SETPOINT_STEP = "SollTempStepVal";
    public static final String KEY_WEBSERVER_TEMPERATURE_SI_UNIT = "TempSIUnit";
    public static final String KEY_WEBSERVER_WEEK_PROGRAM = "WeekProg";
    public static final String KEY_WEBSERVER_WEEK_PROGRAM_ENABLED = "WeekProgEna";
    public static final String KEY_WEBSERVER_OPMODE = "OPMode";
    public static final String KEY_WEBSERVER_OPMODE_ENABLED = "OPModeEna";

    private Logger logger = LoggerFactory.getLogger(WebserverFacadeImpl.class);
    private ArrayList<String> readItemsKeys = new ArrayList<String>();

    private static final String writeThermostatPropertyTemplate = "http://%s/cgi-bin/writeVal.cgi?%s.%s=%s"; // e.g.
                                                                                                             // http://192.168.178.72/cgi-bin/writeVal.cgi?G0.SollTemp=2100
                                                                                                             // -> sets
                                                                                                             // the
                                                                                                             // thermostat
                                                                                                             // with
                                                                                                             // id=G0 to
                                                                                                             // 21
                                                                                                             // degrees

    public WebserverFacadeImpl() {
        this.fillReadItemsKeys();
        this.parser = new WebserverResponseParser();
    }

    private void fillReadItemsKeys() {
        this.readItemsKeys.add(KEY_WEBSERVER_ID);
        this.readItemsKeys.add(KEY_WEBSERVER_OWNER_ID);
        this.readItemsKeys.add(KEY_WEBSERVER_NAME);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_ACTUAL);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_SETPOINT);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_SETPOINT_MAX);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_SETPOINT_MIN);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_SETPOINT_STEP);
        this.readItemsKeys.add(KEY_WEBSERVER_TEMPERATURE_SI_UNIT);
        this.readItemsKeys.add(KEY_WEBSERVER_WEEK_PROGRAM);
        this.readItemsKeys.add(KEY_WEBSERVER_WEEK_PROGRAM_ENABLED);
        this.readItemsKeys.add(KEY_WEBSERVER_OPMODE);
        this.readItemsKeys.add(KEY_WEBSERVER_OPMODE_ENABLED);
    }

    @Override
    public Thermostat getThermostat(String thermostatId, boolean forceRefresh) {
        refreshThermostats(forceRefresh || this.isRefreshObligatoryFor(thermostatId), this.bindingConfig);

        return this.thermostats.get(thermostatId);
    }

    @Override
    public Collection<Thermostat> getThermostats() {
        refreshThermostats(true, this.bindingConfig);

        return this.thermostats.values();
    }

    @Override
    public void writeSetPoint(Thermostat thermostat) {
        this.writeValue(thermostat.getWebserverIpAddress(), thermostat.getWebserverId(),
                KEY_WEBSERVER_TEMPERATURE_SETPOINT, this.convertTemperatureValue(thermostat.getSetPointTemperature()));
    }

    private void refreshThermostats(boolean forceRefresh, BindingConfiguration bindingConfiguration) {
        if (new Date().after(nextExplicitRefresh) || forceRefresh) {
            Collection<Thermostat> thermostats = this.getThermostatsFromWebserver(bindingConfiguration);

            for (Thermostat thermostat : thermostats) {
                this.thermostats.put(thermostat.getUniqueId(), thermostat);
            }

            this.nextExplicitRefresh = getTimeToWaitUntilNextPossbileExplicitRefresh();
        }
    }

    private boolean isRefreshObligatoryFor(String thermostatId) {
        return !this.thermostats.containsKey(thermostatId);
    }

    private Date getTimeToWaitUntilNextPossbileExplicitRefresh() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, blockTimeForNextExplicitRefresh);
        return c.getTime();
    }

    private Collection<Thermostat> getThermostatsFromWebserver(BindingConfiguration bindingConfiguration) {
        Collection<Thermostat> thermostats = new ArrayList<>();

        if (bindingConfiguration != null) {
            for (String ipAddress : bindingConfiguration.getIpAddresses()) {
                thermostats.addAll(parser.parseActualsFrom(ipAddress, this.getILRReadValues(ipAddress)));
            }
        }

        return thermostats;
    }

    private String getILRReadValues(String ipAddress) {
        String result = null;

        String xmlTemplate = this.getXmlTemplate();

        int numberOfDevices = this.getNumberOfDevices(ipAddress, xmlTemplate);

        String requestBody = this.getRequestBody(xmlTemplate, numberOfDevices);

        result = getILRReadValuesFromWebserver(ipAddress, requestBody);

        return result;
    }

    private void writeValue(String ipAddress, String thermostatId, String property, String value) {
        HttpClient client = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(
                String.format(writeThermostatPropertyTemplate, ipAddress, thermostatId, property, value));

        try {
            client.execute(httpget);
        } catch (IOException e) {
            logger.error(String.format("IOException: %s %s", e.getMessage(), e));
        }
    }

    @Override
    public void addUpdateListenerFor(String thermostatId, ThermostatUpdateListener listener) {
        this.updateListeners.put(thermostatId, listener);
    }

    @Override
    public void removeUpdateListenerFor(String thermostatId) {
        this.updateListeners.remove(thermostatId);
    }

    private String convertTemperatureValue(double temperature) {
        return String.valueOf((int) (temperature * 100));
    }

    private String getILRReadValuesFromWebserver(String ipAddress, String body) {
        String result = "";
        HttpClient client = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(String.format("http://%s/cgi-bin/ILRReadValues.cgi", ipAddress));
        StringEntity entity;
        try {
            entity = new StringEntity(body);
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/xml"));
            httppost.setEntity(entity);
            HttpResponse response = client.execute(httppost);
            result = EntityUtils.toString(response.getEntity());
        } catch (UnsupportedEncodingException e) {
            logger.error(String.format("UnsupportedEncodingException: %s %s", e.getMessage(), e));
        } catch (ClientProtocolException e) {
            logger.error(String.format("ClientProtocolException: %s %s", e.getMessage(), e));
        } catch (IOException e) {
            logger.error(String.format("IOException: %s %s", e.getMessage(), e));
        }
        return result;
    }

    private int getNumberOfDevices(String ipAddress, String xmlTemplate) {

        String requestBody = this.getRequestBody(xmlTemplate, 1);

        String responseBody = this.getILRReadValuesFromWebserver(ipAddress, requestBody);

        String numberOfDevices = responseBody.substring(responseBody.indexOf("totalNumberOfDevices"));
        numberOfDevices = numberOfDevices.substring(numberOfDevices.indexOf("<v>") + 3,
                numberOfDevices.indexOf("</v>"));

        return Integer.parseInt(numberOfDevices);
    }

    private String getRequestBody(String xmlTemplate, int numberOfDevices) {
        String bodyItems = "";

        for (int i = 0; i < numberOfDevices; i++) {
            for (String itemKey : this.readItemsKeys) {
                bodyItems += String.format("<i><n>%s</n></i>", String.format("G%s.%s", i, itemKey));
            }
        }

        xmlTemplate = xmlTemplate.replace("{ITEMS}", bodyItems);
        xmlTemplate = xmlTemplate.replace("{ITEM_LIST_SIZE}", Integer.toString(5 + (13 * numberOfDevices)));

        return xmlTemplate;
    }

    private String getXmlTemplate() {
        InputStream httpBodyStream = this.getClass().getResourceAsStream("/getActualsHttpBody.xml");
        java.util.Scanner s = new java.util.Scanner(httpBodyStream).useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";

        return body;
    }

    protected void addBindingConfigurationManager(BindingConfigurationManager bindingConfigManager) {
        this.bindingConfigManager = bindingConfigManager;
        this.bindingConfigManager.addListener(this);
        this.bindingConfig = this.bindingConfigManager.getBindingConfiguration();
    }

    protected void removeBindingConfigurationManager(BindingConfigurationManager bindingConfigManager) {
        this.bindingConfigManager = null;
    }

    @Override
    public void ConfigurationUpdated(BindingConfiguration updatedConfiguration) {
        if (updatedConfiguration == null) {
            logger.warn("Omitting updated configuration due to null value.");
            return;
        }

        if (this.hasNewIpAddress(updatedConfiguration.getIpAddresses())) {
            // if the ip adresses have changed, we do an update in any case (even when auto refresh is not enabled)
            this.refreshThermostats(true, updatedConfiguration);
            notifyListeners();
        }

        if (this.bindingConfig != null
                && this.bindingConfig.getRefreshInterval() != updatedConfiguration.getRefreshInterval()) {

            if (this.pollingJob != null) {
                this.pollingJob.cancel(false);
            }

            this.initScheduledRefresh();
        }

        this.bindingConfig = updatedConfiguration;
    }

    private boolean hasNewIpAddress(List<String> newAddresses) {
        if (newAddresses == null) {
            return false;
        }

        if (this.bindingConfig == null) {
            return true;
        }

        if (newAddresses.size() != this.bindingConfig.getIpAddresses().size()) {
            return true;
        } else {
            for (String ipAddress : newAddresses) {
                if (!this.bindingConfig.getIpAddresses().contains(ipAddress)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void notifyListeners() {
        if (updateListeners != null && thermostats != null && !thermostats.isEmpty()) {
            for (String listenerKey : updateListeners.keySet()) {
                Thermostat thermostat = thermostats.get(listenerKey);
                if (thermostat != null) {
                    updateListeners.get(listenerKey).updateThermostat(thermostat);
                }
            }
        }
    }

    private ScheduledFuture<?> pollingJob;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private synchronized void initScheduledRefresh() {
        if ((pollingJob == null || pollingJob.isCancelled()) && this.bindingConfig != null
                && this.bindingConfig.getRefreshInterval() > 0) {
            pollingJob = scheduler.scheduleWithFixedDelay(refreshActualsRunnable,
                    this.bindingConfig.getRefreshInterval(), this.bindingConfig.getRefreshInterval(), TimeUnit.SECONDS);
        }
    }

    private Runnable refreshActualsRunnable = new Runnable() {
        @Override
        public void run() {
            refreshThermostats(false, bindingConfig);

            notifyListeners();
        }

    };
}
