/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rothenergylogic.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rothenergylogic.RothEnergyLogicBindingConstants;
import org.openhab.binding.rothenergylogic.handler.RothEnergyLogicHandler;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link RothEnergyLogicHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author sirea07 - Initial contribution
 */
public class RothEnergyLogicHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(RothEnergyLogicBindingConstants.THING_TYPE_THERMOSTAT);
    private WebserverFacade webserver;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    };

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(RothEnergyLogicBindingConstants.THING_TYPE_THERMOSTAT)) {
            return new RothEnergyLogicHandler(thing, this.webserver);
        }

        return null;
    }

    protected void addWebserverFacade(WebserverFacade webserver) {
        this.webserver = webserver;
    }

    protected void removeWebserverFacade(WebserverFacade webserver) {
        this.webserver = null;
    }
}
