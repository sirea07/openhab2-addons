package org.openhab.binding.rothenergylogic.discovery;

import static org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants.THING_TYPE_THERMOSTAT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent;
import org.openhab.binding.rothenergylogic.internal.BindingConfiguration;
import org.openhab.binding.rothenergylogic.internal.BindingConfigurationListener;
import org.openhab.binding.rothenergylogic.internal.BindingConfigurationManager;
import org.openhab.binding.rothenergylogic.internal.WebserverFacade;
import org.openhab.binding.rothenergylogic.internal.model.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class DiscoveryService extends AbstractDiscoveryService
        implements BindingConfigurationListener, EventSubscriber {
    private Logger logger = LoggerFactory.getLogger(DiscoveryService.class);
    private final static int DISCOVERY_TIME = 60;
    private ThingRegistry thingRegistry;
    private BindingConfigurationManager bindingConfigManager;
    private List<String> ipAddresses = new ArrayList<>();
    private WebserverFacade webserver;

    public DiscoveryService() throws IllegalArgumentException {
        super(ImmutableSet.of(THING_TYPE_THERMOSTAT), DISCOVERY_TIME, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ImmutableSet.of(THING_TYPE_THERMOSTAT);
    }

    @Override
    protected void startScan() {
        this.startDiscovery();
    }

    @Override
    protected void startDiscovery() {
        if (this.ipAddresses == null || this.ipAddresses.isEmpty()) {
            return;
        }

        this.logger.info("Starting background discovery of RothEnergyLogic thermostats.");

        Collection<Thermostat> thermostats = this.webserver.getThermostats();

        for (Thermostat thermostat : thermostats) {
            DiscoveryResult discoveryResult = this.createDiscoveryResult(thermostat);
            thingDiscovered(discoveryResult);
            this.setChannels(this.thingRegistry.get(discoveryResult.getThingUID()));
        }
    }

    private DiscoveryResult createDiscoveryResult(Thermostat thermostat) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_THERMOSTAT, thermostat.getId()))
                .withProperty(Thermostat.KEY_INTERNAL_ID, Integer.parseInt(thermostat.getId()))
                .withProperty(Thermostat.KEY_INTERNAL_OWNER_ID, Integer.parseInt(thermostat.getOwnerId()))
                .withProperty(Thermostat.KEY_INTERNAL_NAME, thermostat.getName())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_ACTUAL, thermostat.getActualTemperature())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_SETPOINT, thermostat.getSetPointTemperature())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_SETPOINT_MIN, thermostat.getSetPointMinTemperature())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_SETPOINT_MAX, thermostat.getSetPointMaxTemperature())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_SETPOINT_STEP, thermostat.getSetPointStepValue())
                .withProperty(Thermostat.KEY_INTERNAL_TEMPERATURE_SI_UNIT, thermostat.getTemperatureSIUnit())
                .withProperty(Thermostat.KEY_INTERNAL_WEEK_PROGRAM, thermostat.getWeekProgram())
                .withProperty(Thermostat.KEY_INTERNAL_WEEK_PROGRAM_ENABLED, thermostat.isWeekProgramEnabled())
                .withProperty(Thermostat.KEY_INTERNAL_OPMODE, thermostat.getOpMode())
                .withProperty(Thermostat.KEY_INTERNAL_OPMODE_ENABLED, thermostat.isOpModeEnabled())
                .withLabel(thermostat.getName()).withRepresentationProperty("RepresentationProperty 123").build();

        return discoveryResult;
    }

    private void setChannels(Thing thing) {
        if (thing != null) {
            List<Channel> channels = thing.getChannels();

            for (Channel channel : channels) {
                System.out.println(channel.getUID());
            }
        } else {
            System.out.println("Debug");
        }
    }

    protected void addThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void removeThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    protected void addBindingConfigurationManager(BindingConfigurationManager bindingConfigManager) {
        this.bindingConfigManager = bindingConfigManager;
        this.bindingConfigManager.addListener(this);
    }

    protected void removeBindingConfigurationManager(BindingConfigurationManager bindingConfigManager) {
        this.bindingConfigManager = null;
    }

    protected void addWebserverFacade(WebserverFacade webserver) {
        this.webserver = webserver;
    }

    protected void removeWebserverFacade(WebserverFacade webserver) {
        this.webserver = null;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Collections.singleton(ThingAddedEvent.TYPE);
    }

    @Override
    public EventFilter getEventFilter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void receive(Event event) {
        System.out.println("Added thing!");
    }

    @Override
    public void ConfigurationUpdated(BindingConfiguration updatedConfiguration) {
        boolean ipAddressesUpdated = !(this.ipAddresses.containsAll(updatedConfiguration.getIpAddresses())
                && updatedConfiguration.getIpAddresses().containsAll(this.ipAddresses));

        this.ipAddresses = updatedConfiguration.getIpAddresses();

        if (ipAddressesUpdated && this.ipAddresses.size() > 0) {
            this.startDiscovery();
        }
    }
}
