package org.openhab.binding.rothenergylogic.internal;

public interface BindingConfigurationManager {
    void addListener(BindingConfigurationListener listener);

    BindingConfiguration getBindingConfiguration();
}
