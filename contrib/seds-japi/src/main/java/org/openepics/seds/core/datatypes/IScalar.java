/* 
 * This software is Copyright by the Board of Trustees of Michigan
 *  State University (c) Copyright 2013, 2014.
 *  
 *  You may use this software under the terms of the GNU public license
 *  (GPL). The terms of this license are described at:
 *    http://www.gnu.org/licenses/gpl.txt
 *  
 *  Contact Information:
 *       Facility for Rare Isotope Beam
 *       Michigan State University
 *       East Lansing, MI 48824-1321
 *        http://frib.msu.edu
 */
package org.openepics.seds.core.datatypes;

import java.util.Arrays;
import java.util.Objects;
import org.openepics.seds.api.datatypes.SedsAlarm;
import org.openepics.seds.api.datatypes.SedsControl;
import org.openepics.seds.api.datatypes.SedsDisplay;
import org.openepics.seds.api.datatypes.SedsScalar;
import org.openepics.seds.api.datatypes.SedsTime;
import org.openepics.seds.util.ScalarType;
import org.openepics.seds.util.SedsException;

class IScalar<T> implements SedsScalar<T> {

    private final T value;
    private final SedsAlarm alarm;
    private final SedsDisplay display;
    private final SedsControl control;
    private final SedsTime time;
    private final ScalarType type;

    IScalar(Class<T> type, T value, SedsAlarm alarm, SedsDisplay display, SedsControl control, SedsTime time) {
        this.value = value;
        this.alarm = alarm;
        this.control = control;
        this.display = display;
        this.time = time;
        this.type = ScalarType.typeOf(type);

        if (this.type == ScalarType.UNKNOWN) {
            throw SedsException.buildIAE(
                    value,
                    "Element of a known scalar type, " + Arrays.toString(ScalarType.values()),
                    "Creating a scalar"
            );
        }
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public SedsAlarm getAlarm() {
        return alarm;
    }

    @Override
    public SedsControl getControl() {
        return control;
    }

    @Override
    public SedsDisplay getDisplay() {
        return display;
    }

    @Override
    public SedsTime getTime() {
        return time;
    }

    @Override
    public ScalarType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IScalar<?> other = (IScalar<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.alarm, other.alarm)) {
            return false;
        }
        if (!Objects.equals(this.control, other.control)) {
            return false;
        }
        if (!Objects.equals(this.display, other.display)) {
            return false;
        }
        if (!Objects.equals(this.time, other.time)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Scalar{" + "value=" + value + ", alarm=" + alarm + ", control=" + control + ", display=" + display + ", time=" + time + '}';
    }

}
