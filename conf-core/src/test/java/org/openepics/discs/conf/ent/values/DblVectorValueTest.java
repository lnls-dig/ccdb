/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or any newer version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openepics.discs.conf.ent.values;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
public class DblVectorValueTest {

    private List<Double> testValues;

    @Before
    public void testValuesInitialization() {
        testValues = new ArrayList<>();
        testValues.add(0.0);
        testValues.add(1.1);
        testValues.add(2.2);
        testValues.add(3.3);
        testValues.add(4.4);
        testValues.add(5.5);
        testValues.add(6.6);
    }

    @Test(expected = NullPointerException.class)
    public void dblVectorValue() {
        DblVectorValue dblVectorValue = new DblVectorValue(null);
    }

    /*
     * All display tests also test the Value.auditLogString(int... dimensions)
     */
    @Test
    public void dblVectorDisplay1() {
        DblVectorValue dblVectorValue = new DblVectorValue(testValues.subList(0, 1));
        assertEquals("[0.0]", dblVectorValue.toString());
    }

    @Test
    public void dblVectorDisplay2() {
        DblVectorValue dblVectorValue = new DblVectorValue(testValues.subList(0, 2));
        assertEquals("[0.0, 1.1]", dblVectorValue.toString());
    }

    @Test
    public void dblVectorDisplay5() {
        DblVectorValue dblVectorValue = new DblVectorValue(testValues.subList(0, 5));
        assertEquals("[0.0, 1.1, 2.2, 3.3, 4.4]", dblVectorValue.toString());
    }

    @Test
    public void dblVectorDisplay7() {
        DblVectorValue dblVectorValue = new DblVectorValue(testValues);
        assertEquals("[0.0, 1.1, 2.2, 3.3, ..., 6.6]", dblVectorValue.toString());
    }
}