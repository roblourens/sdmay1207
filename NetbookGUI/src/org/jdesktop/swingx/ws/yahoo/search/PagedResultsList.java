/*
 * $Id: PagedResultsList.java 18 2006-07-14 21:31:17Z rbair $
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

/**
 * <p>A ResultsList that pages through all of the results. This class contains a set
 * of methods that allow you to navigate through all of the pages in a set of
 * results. It only presents a single page of results at a time, which are available
 * through the methods in the java.util.List interface (such as the get(int) method).</p>
 *
 * <p>Here is some example usage:
 * <pre><code>
 *    PagedResultsList<YahooWebSearch.Result> results = new PagedResultsArrayList<YahooWebSearch.Result>();
 *    results.setYahooSearch(yahooWebSearch);
 *    results.refresh();
 *    System.out.println(results.getNumPages());
 *    ...
 *
 *    if (results.hasNextPage()) {
 *        results.nextPage();
 *        System.out.println(results.size()); // number of results in this page
 *        System.out.println(results.get(0)); // fetches and prints the first result on this page
 *    }
 * </code></pre></p>
 *
 * @author rbair
 */
public interface PagedResultsList<E> extends ResultsList<E> {
    /**
     * @return the number of total pages in this results list. This value can change
     *         whenever refresh() is called, or whenever the page index is changed
     *         via one of the page modification methods (firstPage, lastPage, etc).
     */
    public int getNumPages();

    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the next page of results. If hasNextPage returns false, this method does
     * nothing and returns false. If the YahooSearch component is not set, this
     * method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @return true upon a successful loading of the next page.
     */
    public boolean nextPage();

    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the previous page of results. If hasPrevPage returns false, this method does
     * nothing and returns false. If the YahooSearch component is not set, this
     * method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @return true upon a successful loading of the previous page.
     */
    public boolean previousPage();
    
    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the first page of results. If the YahooSearch component is not set, this
     * method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @return true upon a successful loading of the first page.
     */
    public boolean firstPage();

    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the last page of results. If the YahooSearch component is not set, this
     * method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @return true upon a successful loading of the last page.
     */
    public boolean lastPage();
    
    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the given page of results. The index is 0 based. If the YahooSearch component
     * is not set, this method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @param index a 0 based index of the page to load. If index > getNumPages(), then
     *        an IllegalArgumentException is thrown. Likewise if index < 0.
     *
     * @return true upon a successful loading of the given page.
     */
    public boolean gotoPage(int index);

    /**
     * @return true if, according to getNumPages(), there is a previous page of
     * data from that currently loaded in PagedResultsList. If the YahooSearch 
     * component is not set, this method returns false.
     */
    public boolean isHasPreviousPage();

    /**
     * @return true if, according to getNumPages(), there is another page of
     * data after that currently loaded in PagedResultsList. If the YahooSearch 
     * component is not set, this method returns false.
     */
    public boolean isHasNextPage();

    /**
     * <p>Reloads this PagedResultsList by searching the YahooSearch component for
     * the current page of results. If the YahooSearch component
     * is not set, this method returns false.</p>
     * 
     * <p>This method blocks, and should only be called
     * from {@link org.jdesktop.swingx.ws.BackgroundWorker} or SwingWorker or another
     * background threading library.</p>
     *
     * @return true upon a successful loading of the current page.
     */
    public void refresh();
    
    /**
     * Sets the YahooSearch component to use for populating this PagedResultsList.
     * Setting this property while this list is populated with results will not
     * clear those results.
     *
     * @param search the YahooSearch component to use for populating this PagedResultsList.
     *        This may be null.
     */
    public void setYahooSearch(YahooSearch search);
}
