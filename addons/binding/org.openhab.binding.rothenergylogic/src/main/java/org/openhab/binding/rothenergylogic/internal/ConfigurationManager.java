package org.openhab.binding.rothenergylogic.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationManager {

    private static final Pattern IP_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    public List<String> parseIpAddresses(ConfigurationAdmin cm) {
        List<String> validatedAddresses = new ArrayList<String>();

        try {
            org.osgi.service.cm.Configuration conf = cm.getConfiguration(this.getPid());
            Dictionary<String, Object> dict = conf.getProperties();
            for (Enumeration<String> e = dict.keys(); e.hasMoreElements();) {
                String key = e.nextElement();
                if (key.equals(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS)) {
                    String[] ipAddresses = ((String) dict.get(key)).split(";");

                    for (String ipAddress : ipAddresses) {
                        if (this.isValidIpAddress(ipAddress)) {
                            validatedAddresses.add(ipAddress);
                        }
                    }
                }
            }
        } catch (IOException e) {
            this.logger.error("An error occured while retrieving the configuration parameters.", e);
        }

        return validatedAddresses;
    }

    private boolean isValidIpAddress(String ipAddress) {
        return IP_PATTERN.matcher(ipAddress).matches();
    }

    private String getPid() {
        return String.format("binding.%s", RothEnergyLogicBindingConstants.BINDING_ID);
    }
}
