package org.openhab.binding.rothenergylogic.internal;

import java.util.ArrayList;
import java.util.List;

public class BindingConfiguration {
    private List<String> ipAddresses = new ArrayList<String>();
    private int refreshInterval;

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
