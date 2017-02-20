package org.openhab.binding.rothenergylogic.internal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openhab.binding.rothenergylogic.internal.model.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class WebserverResponseParser {
    private Logger logger = LoggerFactory.getLogger(WebserverResponseParser.class);

    public Collection<Thermostat> parseActualsFrom(String webserverIpAddress, String ilrReadValuesResponse) {
        Collection<Thermostat> result = new ArrayList<Thermostat>();
        HashMap<Integer, Thermostat> thermostatsByIds = new HashMap<Integer, Thermostat>();

        try {
            Document doc = this.loadXMLFromString(ilrReadValuesResponse);
            Node itemsRootNode = doc.getElementsByTagName(WebserverFacadeImpl.XML_ITEMS).item(0);
            NodeList items = itemsRootNode.getChildNodes();

            if (items != null && items.getLength() > 0) {
                for (int i = 0; i < items.getLength(); i++) {
                    if (items.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) items.item(i);
                        if (el.getNodeName().equals("i")) {
                            String key = el.getElementsByTagName("n").item(0).getTextContent();
                            String value = el.getElementsByTagName("v").item(0).getTextContent();

                            if (key != null && key.startsWith("G") && value != null && !value.isEmpty()) {
                                int id = getItemId(key);
                                if (id > -1) {
                                    Thermostat thermostat = thermostatsByIds.get(id);
                                    if (thermostat == null) {
                                        thermostat = new Thermostat();
                                        thermostat.setWebserverId(key.split("\\.")[0]);
                                        thermostat.setWebserverIpAddress(webserverIpAddress);
                                        thermostatsByIds.put(id, thermostat);
                                    }
                                    this.setParsedValueOnThermostat(thermostat, key, value);
                                }
                            }
                        }
                    }
                }
            }

            result = thermostatsByIds.values();

        } catch (Exception e) {
            this.logger.error("An Exception occurred while parsing response of webserver.", e);
        }

        return result;
    }

    private void setParsedValueOnThermostat(Thermostat thermostat, String key, String value) {
        String keyName = key.substring(key.indexOf(".") + 1);
        switch (keyName) {
            case WebserverFacadeImpl.KEY_WEBSERVER_ID:
                thermostat.setId(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_OWNER_ID:
                thermostat.setOwnerId(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_NAME:
                thermostat.setName(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_ACTUAL:
                thermostat.setActualTemperature(this.parseTemperature(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_SETPOINT:
                thermostat.setSetPointTemperature(this.parseTemperature(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_SETPOINT_MIN:
                thermostat.setSetPointMinTemperature(this.parseTemperature(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_SETPOINT_MAX:
                thermostat.setSetPointMaxTemperature(this.parseTemperature(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_SETPOINT_STEP:
                thermostat.setSetPointStepValue(this.parseTemperature(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_TEMPERATURE_SI_UNIT:
                thermostat.setTemperatureSIUnit(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_WEEK_PROGRAM:
                thermostat.setWeekProgram(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_WEEK_PROGRAM_ENABLED:
                thermostat.setWeekProgramEnabled(Boolean.parseBoolean(value));
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_OPMODE:
                thermostat.setOpMode(value);
                break;
            case WebserverFacadeImpl.KEY_WEBSERVER_OPMODE_ENABLED:
                thermostat.setOpModeEnabled(Boolean.parseBoolean(value));
                break;
        }
    }

    private double parseTemperature(String tempStr) {
        double result = -1;

        try {
            int tempInt = Integer.parseInt(tempStr);
            result = ((double) tempInt) / 100;
        } catch (NumberFormatException e) {
            this.logger.error(String.format("Invalid temperature value: '%s'. %s", tempStr, e));
        }

        return result;
    }

    private int getItemId(String key) {
        int result = -1;

        try {
            result = Integer.parseInt(key.substring(1, key.indexOf(".")));
        } catch (NumberFormatException e) {
            this.logger.error(String.format("Invalid thermostat id: '%s'. %s", key, e));
        }

        return result;
    }

    private Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
