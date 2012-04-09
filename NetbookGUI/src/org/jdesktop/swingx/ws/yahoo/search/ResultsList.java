/*
 * $Id: ResultsList.java 15 2006-07-14 19:04:35Z rbair $
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>A java.util.List containing the list of search results. This List is generally
 * constructed by the various YahooSearch subclasses during the readResults() phase
 * of the search. It is not thought generally useful to create instances of this
 * class yourself.</p>
 *
 * <p>ResultsList differs from java.util.List in that it adds two methods:
 * {@link #getTotalResultsAvailable} and
 * {@link #getFirstResultPosition}. The values returned from this method are
 * typically based on search results.</p>
 *
 * <p>For example, suppose a search is performed that includes 100 results but
 * only the results between 30(inclusive)-40(exclusive) are included. The results from these methods
 * would be:
 * <ul>
 *   <li>getTotalResultsAvailable() = 100</li>
 *   <li>getFirstResultPosition() = 30</li>
 * </ul></p>
 *
 * <p>None of these values are required for using this List. Rather, they are useful
 * when used to page through all of the totalResultsAvailable.</p>
 *
 * <p>This interface adds one more important method, {@link #getYahooSearch()} which
 * returns the {@link YahooSearch} that was used to produce this search ResultsList.
 * This allows some automated code to page through all the results, creating new
 * searches as necessary.</p>
 *
 * @author rbair
 */
public interface ResultsList<E> extends List<E> {
    /**
     * @return the total results available, not to be confused with the
     * total results in this ResultsList. This method returns the total number of
     * search results associated with the search that produced this ResultsList.
     */
    public int getTotalResultsAvailable();
    
    /**
     * @return the 1 based index into the totalResultsAvailable that this ResultsList
     * is populated with. For example, if there are 100 total results, and this list
     * contains results 30-40, then this method will return 30.
     */
    public int getFirstResultPosition();
    
    /**
     * @return the {@link YahooSearch} that was used to produce this ResultsList. May
     * be null.
     */
    public YahooSearch getYahooSearch();
}