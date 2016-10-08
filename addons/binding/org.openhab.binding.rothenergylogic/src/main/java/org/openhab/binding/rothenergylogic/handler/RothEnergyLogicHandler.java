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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rothenergylogic.internal.WebserverResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RothEnergyLogicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author sirea07 - Initial contribution
 */
public class RothEnergyLogicHandler extends BaseThingHandler {

    List<String> ipAddresses = new ArrayList<String>();

    private Logger logger = LoggerFactory.getLogger(RothEnergyLogicHandler.class);
    private Runnable refreshActualsRunnable = new Runnable() {
        @Override
        public void run() {
            refreshActuals();
        }
    };

    private ScheduledFuture<?> pollingJob;
    private WebserverResponseParser webserverResponseParser;

    public RothEnergyLogicHandler(Thing thing) {
        super(thing);
        this.webserverResponseParser = new WebserverResponseParser();
    }

    @Override
    public void handleConfigurationUpdate(java.util.Map<String, Object> configurationParameters) {
        this.ipAddresses = null;
    };

    @Override
    public void initialize() {
        super.initialize();
        initAutoRefresh();
    };

    private synchronized void initAutoRefresh() {
        int refreshInterval = 30;
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(refreshActualsRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_ACTUAL_TEMP)) {
            System.out.println(channelUID);
        } else if (channelUID.getId().equals(CHANNEL_SET_TEMP)) {
            System.out.println(channelUID);
        }
    }

    private static int count = 0;

    private void refreshActuals() {
        updateState(new ChannelUID(this.getThing().getUID(), CHANNEL_ACTUAL_TEMP), new DecimalType(count++));
    }
}
