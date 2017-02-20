/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.event;

/**
 * A listener which is notified when an AVR is disconnected.
 * 
 * @author Antoine Besnard
 *
 */
public interface AvrDisconnectionListener {

    /**
     * Called when an AVR is disconnected.
     * 
     * @param event
     */
    public void onDisconnection(AvrDisconnectionEvent event);

}
