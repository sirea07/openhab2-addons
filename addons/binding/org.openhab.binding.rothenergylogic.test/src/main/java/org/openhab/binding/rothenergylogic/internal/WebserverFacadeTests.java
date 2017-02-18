package org.openhab.binding.rothenergylogic.internal;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.rothenergylogic.internal.model.Thermostat;

public class WebserverFacadeTests {

    private WebserverFacade webserver;
    private WebserverResponseParser responseParser;

    @Before
    public void Setup() {
        String ipAddress = "192.168.178.72";
        this.webserver = new WebserverFacade(ipAddress);
        this.responseParser = new WebserverResponseParser();
    }

    @Test
    public void getILRReadValues_ModuleAtPassedIpExists_ReturnsNotEmptyResponse() throws Exception {
        String response = this.webserver.getILRReadValues();
        Assert.assertFalse(StringUtils.isBlank(response));
        System.out.println(response);
    }

    @Test
    public void writeValue_UpdateTemperatureSetPoint() throws InterruptedException {
        String initialResponse = this.webserver.getILRReadValues();
        Collection<Thermostat> thermostats = this.responseParser.parseActualsFrom(initialResponse);

        Thermostat thermostat = thermostats.iterator().next();
        double currentSetPointTemperature = thermostat.getSetPointTemperature();
        double newSetPointTemperature = currentSetPointTemperature + 0.1;
        String webserverId = thermostat.getWebserverId();

        System.out.println(String.format("Updating thermostat %s (%s) set point temperature from %s to %s.",
                webserverId, thermostat.getName(), currentSetPointTemperature, newSetPointTemperature));

        this.webserver.writeValue(webserverId, WebserverFacade.KEY_TEMPERATURE_SETPOINT,
                this.webserver.convertTemperatureValue(newSetPointTemperature));

        String updatedResponse = this.webserver.getILRReadValues();

        Collection<Thermostat> updatedThermostats = this.responseParser.parseActualsFrom(updatedResponse);
        boolean assertedThermostat = false;
        for (Thermostat updatedThermostat : updatedThermostats) {
            if (updatedThermostat.getWebserverId().equals(webserverId)) {
                Assert.assertEquals(newSetPointTemperature, updatedThermostat.getSetPointTemperature(), 0);
                assertedThermostat = true;
            }
        }
        Assert.assertTrue(assertedThermostat);

        // revert udpate
        this.webserver.writeValue(webserverId, WebserverFacade.KEY_TEMPERATURE_SETPOINT,
                this.webserver.convertTemperatureValue(currentSetPointTemperature));
    }
}
