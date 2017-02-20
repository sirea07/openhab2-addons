package org.openhab.binding.rothenergylogic.internal;

import java.util.Collection;

import org.openhab.binding.rothenergylogic.internal.model.Thermostat;

public interface WebserverFacade {
    Thermostat getThermostat(String thermostatId);

    Collection<Thermostat> getThermostats();

    void writeSetPoint(Thermostat thermostat);

    void addUpdateListenerFor(String thermostatId, ThermostatUpdateListener listener);

    void removeUpdateListenerFor(String thermostatId);
}
