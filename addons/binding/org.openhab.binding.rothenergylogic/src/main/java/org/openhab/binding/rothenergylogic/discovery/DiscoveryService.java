package org.openhab.binding.rothenergylogic.discovery;

import static org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants.THING_TYPE_THERMOSTAT;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.events.ThingAddedEvent;
import org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants;
import org.openhab.binding.rothenergylogic.internal.WebserverFacade;
import org.openhab.binding.rothenergylogic.internal.WebserverResponseParser;
import org.openhab.binding.rothenergylogic.internal.model.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class DiscoveryService extends AbstractDiscoveryService implements EventSubscriber {
    private static HashMap<String, String> MappedWebserverKeys = null;
    private Logger logger = LoggerFactory.getLogger(DiscoveryService.class);
    private final static int DISCOVERY_TIME = 60;
    private Map<String, Object> configProperties;
    private WebserverResponseParser webserverResponseParser;
    private ThingRegistry thingRegistry;
    private ItemRegistry itemRegistry;

    public DiscoveryService() throws IllegalArgumentException {
        super(ImmutableSet.of(THING_TYPE_THERMOSTAT), DISCOVERY_TIME, false);
        this.webserverResponseParser = new WebserverResponseParser();
        this.fillMappedWebserverKeys();
    }

    private synchronized void fillMappedWebserverKeys() {
        if (MappedWebserverKeys == null) {
            MappedWebserverKeys = new HashMap<String, String>();
            MappedWebserverKeys.put(WebserverFacade.KEY_ID, "id");
            MappedWebserverKeys.put(WebserverFacade.KEY_OWNER_ID, "ownerId");
            MappedWebserverKeys.put(WebserverFacade.KEY_NAME, "name");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_ACTUAL, "actTemp");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_SETPOINT, "setPoint");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_SETPOINT_MIN, "setPointMin");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_SETPOINT_MAX, "setPointMax");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_SETPOINT_STEP, "setPointSteps");
            MappedWebserverKeys.put(WebserverFacade.KEY_TEMPERATURE_SI_UNIT, "Temperature SI Unit");
            MappedWebserverKeys.put(WebserverFacade.KEY_WEEK_PROGRAM, "Week Program");
            MappedWebserverKeys.put(WebserverFacade.KEY_WEEK_PROGRAM, "Week Program Enabled");
            MappedWebserverKeys.put(WebserverFacade.KEY_OPMODE, "OPMode");
            MappedWebserverKeys.put(WebserverFacade.KEY_OPMODE_ENABLED, "OPMode Enabled");
        }
    }

    @Override
    protected void activate(java.util.Map<String, Object> configProperties) {
        this.configProperties = configProperties;
        if (isIpAddressConfigured()) {
            this.modified(configProperties);
        }
    };

    private boolean isIpAddressConfigured() {
        List<String> ipAddresses = this.getIpAddresses();
        return ipAddresses != null && !ipAddresses.isEmpty();
    }

    private List<String> getIpAddresses() {
        return (List<String>) this.configProperties.get(RothEnergyLogicBindingConstants.WEBSERVER_IP_ADDRESS);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ImmutableSet.of(THING_TYPE_THERMOSTAT);
    }

    @Override
    protected void startScan() {
        System.out.println("Scan started!");

    }

    @Override
    protected void startBackgroundDiscovery() {
        if (!this.isIpAddressConfigured()) {
            return;
        }

        this.logger.info("Starting background discovery of RothEnergyLogicTouchline thermostats.");

        List<String> ipAddresses = this.getIpAddresses();

        for (String ipAddress : ipAddresses) {
            WebserverFacade webserver = new WebserverFacade(ipAddress);

            String ilrReadResponse = webserver.getILRReadValues();

            if (ilrReadResponse != null && !ilrReadResponse.isEmpty()) {
                Collection<Thermostat> thermostats = this.webserverResponseParser.parseActualsFrom(ilrReadResponse);
                for (Thermostat thermostat : thermostats) {
                    DiscoveryResult discoveryResult = this.createDiscoveryResult(thermostat);
                    thingDiscovered(discoveryResult);
                    this.setChannels(this.thingRegistry.get(discoveryResult.getThingUID()));
                }
            }
        }
    }

    private DiscoveryResult createDiscoveryResult(Thermostat thermostat) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_THERMOSTAT, thermostat.getId()))
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_ID), Integer.parseInt(thermostat.getId()))
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_OWNER_ID),
                        Integer.parseInt(thermostat.getOwnerId()))
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_NAME), thermostat.getName())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_ACTUAL),
                        thermostat.getActualTemperature())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_SETPOINT),
                        thermostat.getSetPointTemperature())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_SETPOINT_MIN),
                        thermostat.getSetPointMinTemperature())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_SETPOINT_MAX),
                        thermostat.getSetPointMaxTemperature())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_SETPOINT_STEP),
                        thermostat.getSetPointStepValue())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_TEMPERATURE_SI_UNIT),
                        thermostat.getTemperatureSIUnit())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_WEEK_PROGRAM), thermostat.getWeekProgram())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_WEEK_PROGRAM_ENABLED),
                        thermostat.isWeekProgramEnabled())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_OPMODE), thermostat.getOpMode())
                .withProperty(MappedWebserverKeys.get(WebserverFacade.KEY_OPMODE_ENABLED), thermostat.isOpModeEnabled())
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

    protected void addItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void removeItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
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
}
