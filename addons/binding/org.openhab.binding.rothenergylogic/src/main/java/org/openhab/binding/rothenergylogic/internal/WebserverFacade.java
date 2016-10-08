package org.openhab.binding.rothenergylogic.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebserverFacade {

    public static final String XML_ITEMS = "item_list";

    public static final String KEY_ID = "kurzID";
    public static final String KEY_OWNER_ID = "ownerKurzID";
    public static final String KEY_NAME = "name";
    public static final String KEY_TEMPERATURE_ACTUAL = "RaumTemp";
    public static final String KEY_TEMPERATURE_SETPOINT = "SollTemp";
    public static final String KEY_TEMPERATURE_SETPOINT_MIN = "SollTempMinVal";
    public static final String KEY_TEMPERATURE_SETPOINT_MAX = "SollTempMaxVal";
    public static final String KEY_TEMPERATURE_SETPOINT_STEP = "SollTempStepVal";
    public static final String KEY_TEMPERATURE_SI_UNIT = "TempSIUnit";
    public static final String KEY_WEEK_PROGRAM = "WeekProg";
    public static final String KEY_WEEK_PROGRAM_ENABLED = "WeekProgEna";
    public static final String KEY_OPMODE = "OPMode";
    public static final String KEY_OPMODE_ENABLED = "OPModeEna";

    private Logger logger = LoggerFactory.getLogger(WebserverFacade.class);
    private ArrayList<String> readItemsKeys = new ArrayList<String>();
    private String ipAddress;

    public WebserverFacade(String ipAddress) {
        this.ipAddress = ipAddress;
        this.fillReadItemsKeys();
    }

    private void fillReadItemsKeys() {
        this.readItemsKeys.add(KEY_ID);
        this.readItemsKeys.add(KEY_OWNER_ID);
        this.readItemsKeys.add(KEY_NAME);
        this.readItemsKeys.add(KEY_TEMPERATURE_ACTUAL);
        this.readItemsKeys.add(KEY_TEMPERATURE_SETPOINT);
        this.readItemsKeys.add(KEY_TEMPERATURE_SETPOINT_MAX);
        this.readItemsKeys.add(KEY_TEMPERATURE_SETPOINT_MIN);
        this.readItemsKeys.add(KEY_TEMPERATURE_SETPOINT_STEP);
        this.readItemsKeys.add(KEY_TEMPERATURE_SI_UNIT);
        this.readItemsKeys.add(KEY_WEEK_PROGRAM);
        this.readItemsKeys.add(KEY_WEEK_PROGRAM_ENABLED);
        this.readItemsKeys.add(KEY_OPMODE);
        this.readItemsKeys.add(KEY_OPMODE_ENABLED);
    }

    public String getILRReadValues() {
        String result = null;

        String xmlTemplate = this.getXmlTemplate();

        int numberOfDevices = this.getNumberOfDevices(xmlTemplate);

        String requestBody = this.getRequestBody(xmlTemplate, numberOfDevices);

        result = getILRReadValuesFromWebserver(requestBody);

        return result;
    }

    private String getILRReadValuesFromWebserver(String body) {
        String result = "";
        HttpClient client = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(String.format("http://%s/cgi-bin/ILRReadValues.cgi", this.ipAddress));
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

    private int getNumberOfDevices(String xmlTemplate) {

        String requestBody = this.getRequestBody(xmlTemplate, 1);

        String responseBody = this.getILRReadValuesFromWebserver(requestBody);

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
        InputStream httpBodyStream = this.getClass().getResourceAsStream("/resources/getActualsHttpBody.xml");
        java.util.Scanner s = new java.util.Scanner(httpBodyStream).useDelimiter("\\A");
        String body = s.hasNext() ? s.next() : "";

        return body;
    }
}
