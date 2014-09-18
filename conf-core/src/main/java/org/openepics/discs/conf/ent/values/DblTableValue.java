/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any
 * newer version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.openepics.discs.conf.ent.values;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * A table is a collection columns containing double precision values. All columns must contain the same number of elements.
 * This restriction is enforced implicitly by the SEDS serialization and is checked in the UI layer.
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 *
 */
public class DblTableValue implements Value {
    private static final int ROWS = 5;
    private static final int COLS = 3;

    private final List<List<Double>> dblTableValue;

    public DblTableValue(List<List<Double>> dblTableValue) {
        this.dblTableValue = Preconditions.checkNotNull(dblTableValue);
    }

    /**
     * @return the table
     */
    public List<List<Double>> getDblTableValue() { return dblTableValue; }

    @Override
    public String toString() {
        final StringBuilder retStr = new StringBuilder();
        final int columnsSize = dblTableValue.size(); // number of columns in the table
        int colIndex = 0;
        retStr.append('[');

        for (List<Double> column : dblTableValue) {
            appendSingleColumn(column, retStr);
            colIndex++;
            if (colIndex < columnsSize) {
                retStr.append(", ");
            }
            if ((columnsSize > COLS) && (colIndex >= COLS - 1)) {
                retStr.append("..., ");
                appendSingleColumn(dblTableValue.get(columnsSize - 1), retStr); // append last column
                break;
            }
        }
        retStr.append(']');

        return retStr.toString();
    }

    private void appendSingleColumn(List<Double> column, StringBuilder retStr) {
        final int rowsSize = column.size(); // number of rows in a column
        int rowIndex = 0;
        retStr.append('[');

        for (Double item : column) {
            retStr.append(item);
            rowIndex++;
            if (rowIndex < rowsSize) {
                retStr.append(", ");
            }
            if ((rowsSize > ROWS) && (rowIndex >= ROWS - 1)) {
                retStr.append("..., ").append(column.get(rowsSize - 1)); // append last row
                break;
            }
        }
        retStr.append(']');
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dblTableValue == null) ? 0 : dblTableValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DblTableValue)) {
            return false;
        }
        DblTableValue other = (DblTableValue) obj;
        if (dblTableValue == null) {
            return other.dblTableValue == null;
        }

        return dblTableValue.equals(other.dblTableValue);
    }
}
