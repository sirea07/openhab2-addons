package org.openhab.binding.rothenergylogic.discovery;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants;
import org.openhab.binding.rothenergylogic.internal.ConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryServiceFactory implements ConfigurationListener {

    private Logger logger = LoggerFactory.getLogger(DiscoveryServiceFactory.class);
    private BundleContext bundleContext;
    private ConfigurationManager configManager;

    protected void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
        this.configManager = new ConfigurationManager();
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        String id = event.getPid();
        if (id.equals(this.getPid()) && event.getType() == ConfigurationEvent.CM_UPDATED) {
            BundleContext bundleContext = this.bundleContext;
            ServiceReference<?> ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
            if (ref != null) {
                ConfigurationAdmin cm = ((ConfigurationAdmin) bundleContext.getService(ref));
                if (cm != null) {
                    List<String> ipAddresses = this.configManager.parseIpAddresses(cm);

                    if (ipAddresses != null && !ipAddresses.isEmpty()) {
                        initDiscoveryService(cm, ipAddresses);
                    }
                }
            }
        }
    }

    private void initDiscoveryService(ConfigurationAdmin cm, List<String> ipAddresses) {
        try {
            org.osgi.service.cm.Configuration config = cm
                    .createFactoryConfiguration("org.openhab.binding.rothenergylogictouchline.discovery", null);
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS, ipAddresses);
            properties.put(
                    org.eclipse.smarthome.config.discovery.DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY_ENABLED,
                    "true");
            config.update(properties);
        } catch (IOException e) {
            this.logger.error("An error occured on initialization of DiscoveryService.");
        }
    }

    private String getPid() {
        return String.format("binding.%s", RothEnergyLogicBindingConstants.BINDING_ID);
    }
}
