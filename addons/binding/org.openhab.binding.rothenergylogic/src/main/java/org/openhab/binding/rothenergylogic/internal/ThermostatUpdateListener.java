package org.openhab.binding.rothenergylogic.internal;

import org.openhab.binding.rothenergylogic.internal.model.Thermostat;

public interface ThermostatUpdateListener {
    void updateThermostat(Thermostat thermostat);
}
