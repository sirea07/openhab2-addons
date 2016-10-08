/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rothenergylogic;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RothEnergyLogicBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author sirea07 - Initial contribution
 */
public class RothEnergyLogicBindingConstants {

    public static final String BINDING_ID = "rothenergylogictouchline";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public final static String CHANNEL_ACTUAL_TEMP = "actual_temp";
    public final static String CHANNEL_SET_TEMP = "set_temp";

    // Configuration Parameters
    public final static String WEBSERVER_IP_ADDRESS = "webserver_ip";

}
