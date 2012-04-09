/*
 * $Id: PagedResultsArrayList.java 76 2006-09-18 20:20:47Z rbair $
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
import org.jdesktop.beans.AbstractBean;

/**
 * <p>A non visual JavaBean that represents the results of a Yahoo! search. Unlike
 * {@link ResultsList} which only contains the results of a single "page" of
 * search results, PagedResultsArrayList allows you to step through all of the
 * results returned from a search.</p>
 *
 * <p>Sample usage:
 * <pre><code>
 *      YahooWebSearch yahoo = new YahooWebSearch();
 *      yahoo.appId = "myappidxxx(get one from yahoo)";
 *      yahoo.query = "world cup 2006 Italy";
 *      PagedResultsArrayList<YahooWebSearch.Result> results = new
 *              PagedResultsArrayList<YahooWebSearch.Result>();
 *      results.setYahooSearch(yahoo);
 *      results.refresh();
 *      
 *      System.out.println("NumPages: " + results.getNumPages());
 *      System.out.println("NumResults: " + results.getTotalResultsAvailable());
 *      
 *      int count = 1;
 *      while (count < 300 && results.hasNextPage()) {
 *          for (int i=0; i<results.size(); i++) {
 *              Result r = results.get(i);
 *              System.out.println(count++ + ".\t" + r.title);
 *              System.out.println("\t" + r.summary);
 *              System.out.println("\t" + r.modDate);
 *          }
 *          results.nextPage();
 *      }
 * </code></pre></p>
 *
 * @author rbair
 */
public class PagedResultsArrayList<E> extends AbstractBean implements List<E>, PagedResultsList<E> {
    /**
     * The number of logical pages in this List
     */
    private int numPages = 0;
    /**
     * The index of the current page loaded in this list
     */
    private int currentPageIndex = 0;
    /**
     * The number of results per page. When this changes, the numPages value
     * also changes since numPages is calculated based on the number of search
     * results / resultsPerPage. The currentPageIndex may also change in that
     * scenario.
     */
    private int resultsPerPage = 10;
    /**
     * The YahooSearch component that will perform the searches
     */
    private YahooSearch yahoo;
    private int totalResultsAvailable = 0;
    private int firstResultPosition = 1;
    /**
     * Implementation detail: stores the results of the search
     */
    private List<E> results = new ArrayList<E>();
    
    /** Creates a new instance of PagedResultsArrayList */
    public PagedResultsArrayList() {
    }

    /**
     * @inheritDoc
     */
    public int getNumPages() {
        return numPages;
    }

    /**
     * @inheritDoc
     */
    public boolean nextPage() {
        if (isHasNextPage()) {
            return gotoPage(currentPageIndex + 1);
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    public boolean previousPage() {
        if (isHasPreviousPage()) {
            return gotoPage(currentPageIndex - 1);
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    public boolean firstPage() {
        if (currentPageIndex > 0) {
            return gotoPage(0);
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    public boolean lastPage() {
        if (currentPageIndex < numPages - 1) {
            return gotoPage(numPages - 1);
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    public boolean gotoPage(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must not be < 0");
        }
        
        if (index >= numPages) {
            throw new IllegalArgumentException("Index must be < the total number of pages");
        }
        
        if (yahoo == null) {
            return false;
        }
        
        int old = currentPageIndex;
        currentPageIndex = index;
        refresh();
        firePropertyChange("currentPageIndex", old, currentPageIndex);
        return true;
    }

    /**
     * @inheritDoc
     */
    public boolean isHasPreviousPage() {
        return yahoo != null && currentPageIndex > 0;
    }

    /**
     * @inheritDoc
     */
    public boolean isHasNextPage() {
        return yahoo != null && currentPageIndex < numPages - 1;
    }

    public void refresh() {
        int oldTotalResultsAvailable = totalResultsAvailable;
        int oldFirstResultPosition = firstResultPosition;
        int oldNumPages = numPages;
        
        if (yahoo != null) {
            ResultsList r = yahoo.search(currentPageIndex * resultsPerPage, resultsPerPage);
            results.clear();
            results.addAll(r);
            totalResultsAvailable = r.getTotalResultsAvailable();
            firstResultPosition = r.getFirstResultPosition();
            numPages = totalResultsAvailable / resultsPerPage;
        } else {
            totalResultsAvailable = 0;
            firstResultPosition = 1;
            numPages = 0;
        }
        
        firePropertyChange("totalResultsAvailable", oldTotalResultsAvailable, totalResultsAvailable);
        firePropertyChange("firstResultPosition", oldFirstResultPosition, firstResultPosition);
        firePropertyChange("numPages", oldNumPages, numPages);
    }

    public final void setYahooSearch(YahooSearch search) {
        YahooSearch old = getYahooSearch();
        this.yahoo = search;
        firePropertyChange("yahooSearch", old, getYahooSearch());
    }

    public final YahooSearch getYahooSearch() {
        return yahoo;
    }
    
    public final int getTotalResultsAvailable() {
        return totalResultsAvailable;
    }

    public final int getFirstResultPosition() {
        return firstResultPosition;
    }

    public final int getCurrentPageIndex() {
        return currentPageIndex;
    }
    
    public final void setResultsPerPage(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("resultsPerPage cannot be <= 0");
        }
        
        int oldNumPages = numPages;
        int oldCurrentPageIndex = currentPageIndex;
        boolean oldHasNext = isHasNextPage();
        boolean oldHasPrev = isHasPreviousPage();
        
        int old = getResultsPerPage();
        this.resultsPerPage = value;
        firePropertyChange("resultsPerPage", old, getResultsPerPage());
        numPages = getTotalResultsAvailable() / resultsPerPage;
        currentPageIndex = getFirstResultPosition() / resultsPerPage;
        
        firePropertyChange("numPages", oldNumPages, numPages);
        firePropertyChange("currentPageIndex", oldCurrentPageIndex, currentPageIndex);
        firePropertyChange("hasNextPage", oldHasNext, isHasNextPage());
        firePropertyChange("hasPreviousPage", oldHasPrev, isHasPreviousPage());
    }
    
    public final int getResultsPerPage() {
        return resultsPerPage;
    }
    
    // List methods
    
    /**
     */
    public int size() {
        return results.size();
    }

    /**
     */
    public boolean isEmpty() {
        return results.isEmpty();
    }

    /**
     */
    public boolean contains(Object o) {
        return results.contains(o);
    }

    /**
     */
    public Iterator<E> iterator() {
        return results.iterator();
    }

    /**
     */
    public Object[] toArray() {
        return results.toArray();
    }

    /**
     */
    public <T> T[] toArray(T[] a) {
        return results.toArray(a);
    }


    // Modification Operations

    /**
     */
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    // Bulk Modification Operations

    /**
     */
    public boolean containsAll(Collection<?> c) {
        return results.containsAll(c);
    }

    /**
     */
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();

    }

    /**
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }


    // Comparison and hashing

    /**
     */
    public boolean equals(Object o) {
        //TODO .equals() should be more strict
        return results.equals(o);
    }

    /**
     */
    public int hashCode() {
        return results.hashCode();
    }


    // Positional Access Operations

    /**
     */
    public E get(int index) {
        return results.get(index);
    }

    /**
     */
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }


    // Search Operations

    /**
     */
    public int indexOf(Object o) {
        return results.indexOf(o);
    }

    /**
     */
    public int lastIndexOf(Object o) {
        return results.lastIndexOf(o);
    }


    // List Iterators

    /**
     */
    public ListIterator<E> listIterator() {
        return results.listIterator();
    }

    /**
     */
    public ListIterator<E> listIterator(int index) {
        return results.listIterator(index);
    }

    // View

    /**
     */
    public List<E> subList(int fromIndex, int toIndex) {
        return results.subList(fromIndex, toIndex);
    }
}
