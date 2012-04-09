/*
 * $Id: ResultsArrayList.java 16 2006-07-14 19:06:47Z rbair $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.swingx.ws.yahoo.search;

import java.util.ArrayList;

/**
 * A basic implementation of ResultsList based on ArrayList. This object contains
 * a constructor in which the <code>totalResultsAvailable</code>, </code>firstResultPosition</code>,
 * and <code>YahooSearch</code> must be specified. These are immutable properties.
 *
 * @author rbair
 */
public class ResultsArrayList<E> extends ArrayList<E> implements ResultsList<E> {
    private int totalResultsAvailable;
    private int firstResultPosition;
    private YahooSearch search;
    
    /** Creates a new instance of ResultsArrayList */
    public ResultsArrayList(YahooSearch search, int totalResultsAvailable, int firstResultPosition) {
        this.totalResultsAvailable = totalResultsAvailable;
        this.firstResultPosition = firstResultPosition;
        this.search = search;
    }
    
    /**
     * @inheritDoc
     */
    public int getTotalResultsAvailable() {
        return totalResultsAvailable;
    }
    
    /**
     * @inheritDoc
     */
    public int getFirstResultPosition() {
        return firstResultPosition;
    }
    
    /**
     * @inheritDoc
     */
    public YahooSearch getYahooSearch() {
        return search;
    }
}
