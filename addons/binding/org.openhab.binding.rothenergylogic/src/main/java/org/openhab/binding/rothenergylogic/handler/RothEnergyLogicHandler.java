/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rothenergylogic.handler;

import static org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.rothenergylogic.internal.ThermostatUpdateListener;
import org.openhab.binding.rothenergylogic.internal.WebserverFacade;
import org.openhab.binding.rothenergylogic.internal.model.Thermostat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RothEnergyLogicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author sirea07 - Initial contribution
 */
public class RothEnergyLogicHandler extends BaseThingHandler implements ThermostatUpdateListener {

    List<String> ipAddresses = new ArrayList<String>();

    private Logger logger = LoggerFactory.getLogger(RothEnergyLogicHandler.class);
    private WebserverFacade webserver;
    private Thermostat thermostat;

    public RothEnergyLogicHandler(Thing thing, WebserverFacade webserver) {
        super(thing);
        this.webserver = webserver;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing RothEnergyLogic handler.");
        super.initialize();
        this.updateActuals();
    }

    @Override
    public void handleConfigurationUpdate(java.util.Map<String, Object> configurationParameters) {
        this.ipAddresses = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(CHANNEL_ACTUAL_TEMP) || channelUID.getId().equals(CHANNEL_SET_TEMP)) {
                this.updateActuals();
            }
        } else if (command instanceof DecimalType) {
            if (channelUID.getId().equals(CHANNEL_SET_TEMP)) {
                this.thermostat.setSetPointTemperature(((DecimalType) command).doubleValue());
                this.webserver.writeSetPoint(this.thermostat);
            }
        }
    }

    @Override
    public void updateThermostat(Thermostat thermostat) {
        this.updateActuals(thermostat);
    }

    private void updateActuals() {
        Thermostat thermostat = this.webserver.getThermostat(this.getThing().getUID().getId());
        this.updateActuals(thermostat);
    }

    private void updateActuals(Thermostat thermostat) {
        this.thermostat = thermostat;

        updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTUAL_TEMP),
                new DecimalType(thermostat.getActualTemperature()));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SET_TEMP),
                new DecimalType(thermostat.getSetPointTemperature()));
    }

}
