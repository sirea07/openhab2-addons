package org.openhab.binding.rothenergylogic.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingConfigurationManagerImpl implements BindingConfigurationManager, ConfigurationListener {

    private static final Pattern IP_PATTERN = Pattern
            .compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private Logger logger = LoggerFactory.getLogger(BindingConfigurationManagerImpl.class);
    private List<BindingConfigurationListener> listeners = new ArrayList<BindingConfigurationListener>();
    private BindingConfiguration bindingConfiguration;
    private ConfigurationAdmin configurationAdmin;
    private BundleContext bundleContext;

    private ComponentContext componentContext;

    protected void activate(ComponentContext componentContext) {
        this.componentContext = componentContext;
        this.bundleContext = componentContext.getBundleContext();
        this.setConfigAdmin();
    }

    private void setConfigAdmin() {
        ServiceReference<?> ref = this.bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (ref != null) {
            this.configurationAdmin = ((ConfigurationAdmin) bundleContext.getService(ref));
        }
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        String id = event.getPid();
        if (id.equals(this.getPid()) && event.getType() == ConfigurationEvent.CM_UPDATED) {
            if (this.configurationAdmin != null) {
                this.updateConfiguration();
            }
        }
    }

    @Override
    public BindingConfiguration getBindingConfiguration() {
        if (bindingConfiguration == null) {
            this.updateConfiguration();
        }

        return bindingConfiguration;
    }

    private void updateConfiguration() {
        Configuration configuration;

        try {
            configuration = this.configurationAdmin.getConfiguration(this.getPid());
        } catch (IOException e) {
            this.logger.error("An error occured while retrieving the configuration parameters.", e);
            return;
        }

        boolean configUpdated;
        Dictionary<String, Object> configDictionary = configuration.getProperties();

        if (configDictionary != null) {
            if (this.bindingConfiguration == null) {
                this.bindingConfiguration = new BindingConfiguration();
            }

            configUpdated = updateIpAddresses(configDictionary);
            configUpdated = updateRefreshInterval(configDictionary) || configUpdated;

            if (configUpdated) {
                this.notifyListeners();
            }
        }

        try {
            if (configDictionary != null && getBindingConfiguration().getRefreshInterval() != 30) {
                configDictionary.put(RothEnergyLogicBindingConstants.REFRESH_INTERVAL_IN_SECONDS, 30);

                configuration.update();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void notifyListeners() {
        if (this.listeners == null || this.listeners.isEmpty()) {
            return;
        }

        for (BindingConfigurationListener listener : this.listeners) {
            listener.ConfigurationUpdated(getBindingConfiguration());
        }
    }

    @Override
    public void addListener(BindingConfigurationListener listener) {
        listeners.add(listener);
    }

    private boolean updateRefreshInterval(Dictionary<String, Object> configDictionary) {
        String refreshStr = (String) configDictionary.get(RothEnergyLogicBindingConstants.REFRESH_INTERVAL_IN_SECONDS);
        refreshStr = refreshStr != null ? refreshStr : "0";

        int newRefreshInterval = Integer.valueOf(refreshStr);

        boolean propertyChanged = newRefreshInterval != getBindingConfiguration().getRefreshInterval();

        if (propertyChanged) {
            getBindingConfiguration().setRefreshInterval(newRefreshInterval);
        }

        return propertyChanged;
    }

    private boolean updateIpAddresses(Dictionary<String, Object> configDictionary) {
        List<String> validatedAddresses = new ArrayList<String>();

        String newIpAddresses = String
                .valueOf(configDictionary.get(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS));

        if (newIpAddresses != null) {
            String[] ipAddresses = splitIpAddresses(newIpAddresses);

            for (String ipAddress : ipAddresses) {
                if (this.isValidIpAddress(ipAddress) && !validatedAddresses.contains(ipAddress)) {
                    validatedAddresses.add(ipAddress);
                }
            }
        }

        boolean propertyChanged = hasIpAddressChanged(validatedAddresses);

        if (propertyChanged) {
            getBindingConfiguration().setIpAddresses(validatedAddresses);
        }

        return propertyChanged;
    }

    private String[] splitIpAddresses(String newIpAddresses) {
        return newIpAddresses.split(";");
    }

    private boolean hasIpAddressChanged(List<String> validatedAddresses) {
        if (validatedAddresses.size() != getBindingConfiguration().getIpAddresses().size()) {
            return true;
        } else {
            for (String ipAddress : validatedAddresses) {
                if (!getBindingConfiguration().getIpAddresses().contains(ipAddress)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidIpAddress(String ipAddress) {
        return IP_PATTERN.matcher(ipAddress).matches();
    }

    private String getPid() {
        return String.format("binding.%s", RothEnergyLogicBindingConstants.BINDING_ID);
    }

    @Override
    public synchronized void addIpAddress(String ipAddress) {
        if (this.isValidIpAddress(ipAddress)) {

            try {
                Configuration configuration = this.configurationAdmin.getConfiguration(this.getPid());
                Dictionary<String, Object> properties = configuration.getProperties();

                if (properties == null) {
                    properties = new Hashtable<>();
                }

                String ipAddressesValue = (String) properties.get(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS);

                boolean isNew = true;

                if (ipAddressesValue == null || ipAddressesValue.length() == 0) {
                    ipAddressesValue = ipAddress;
                } else {
                    String[] splitIpAddresses = splitIpAddresses(ipAddressesValue);

                    for (String existingIpAddress : splitIpAddresses) {
                        if (existingIpAddress.equals(ipAddress)) {
                            isNew = false;
                            break;
                        }
                    }

                    ipAddressesValue = String.format("%s;%s", ipAddressesValue, ipAddress);
                }

                if (isNew) {
                    properties.put(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS, ipAddressesValue);
                    configuration.update(properties);

                    this.updateConfiguration();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
