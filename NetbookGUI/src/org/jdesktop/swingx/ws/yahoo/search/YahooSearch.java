/*
 * $Id: YahooSearch.java 76 2006-09-18 20:20:47Z rbair $
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import org.jdesktop.beans.AbstractBean;

/**
 * Base class from which other Yahoo! search beans extend. It provides support for
 * constructing the URL in a common manner, as well as a consistent set of "search"
 * methods that may be called by client code.
 *
 * @author rbair
 */
public abstract class YahooSearch<T> extends AbstractBean {
    /**
     * The URL prefix at the beginning of every Yahoo! search request
     */
    private static final String URL_PREFIX = "http://api.search.yahoo.com";
    
    /**
     * @return a ResultsList containing the results read from the given InputStream.
     */
    protected abstract ResultsList<T> readResults(InputStream in) throws Exception;

    /**
     * @return the service name for the web service. See the 
     *         <a href="http://developer.yahoo.com/search/index.html">Yahoo documentation</a>
     *         for more information about what the service name is for your
     *         particular service.
     */
    protected abstract String getServiceName();

    /**
     * @return the version number of the web service. See the 
     *         <a href="http://developer.yahoo.com/search/index.html">Yahoo documentation</a>
     *         for more information about what the version number is for your
     *         particular service.
     */
    protected abstract String getVersionNumber();

    /**
     * @return the default number of results returned from the web service. See the 
     *         <a href="http://developer.yahoo.com/search/index.html">Yahoo documentation</a>
     *         for more information about your specific web service. Defaults to 10.
     */
    protected int getDefaultResultCount() {
        return 10;
    }
    
    /**
     * @return the method name for the web service. See the 
     *         <a href="http://developer.yahoo.com/search/index.html">Yahoo documentation</a>
     *         for more information about what the method name is for your
     *         particular service.
     */
    protected abstract String getMethod();
    
    /**
     * @return a map of parameters for your web service. See the 
     *         <a href="http://developer.yahoo.com/search/index.html">Yahoo documentation</a>
     *         for more information about what parameters are available for your
     *         particular service.
     *
     *         <p>Note that the parameters "results" and "start" should be ommitted
     *         from this map since they are always included automatically by the
     *         {@link #constructUrl(int,int)} method. Also note that you do not
     *         have to worry about url-encoding the values for these params since
     *         the {@link #constructUrl(int,int)} method does this for you.</p>
     */
    protected abstract Map getParameters();
    
    /**
     * <p>Creates and returns a java.net.URL representing the search. It is constructed
     * by calling the {@link #getServiceName}, {@link #getVersionNumber},
     * {@link #getMethod()}, and {@link #getParameters()} methods. In addition
     * to combining these returned values into the URL, this method also performs
     * URL escaping of the parameters as necessary. Thus, it is not necessary for
     * the parameters in the map to be URL encoded.</p>
     *
     * <p>If the <code>firstSearchPosition</code> and/or <code>resultCount</code>
     * params are >= 1, then they are included in the search URL as appropriate to
     * set the number of search results to return (<code>resultCount</code>) and
     * where in the possibly infinite number of search results to start collecting
     * results (<code>firstSearchPosition</code>). For instance:
     * <pre><code>
     *   constructURL(35, 5);
     * </code></pre>
     * This method call would return 5 search results, starting at the 35th result
     * (1 based number).</p>
     *
     * @param firstSearchPosition 1 based position in the stream of all results from
     *        which to collect <code>resultCount</code> number of results. If < 1, then
     *        1 is used.
     *
     * @param resultCount number of results to return. If < 1, then the default result
     *        count will be used for the search (differs based on Yahoo search).
     *
     * @return URL representing the search.
     */
    protected final URL constructUrl(int firstSearchPosition, int resultCount) throws MalformedURLException {
        firstSearchPosition = firstSearchPosition < 1 ? 1 : firstSearchPosition;
        resultCount = resultCount < 1 ? getDefaultResultCount() : resultCount;
        
        StringBuilder buffer = new StringBuilder(URL_PREFIX);
        buffer.append("/").append(getServiceName()).append("/");
        buffer.append(getVersionNumber()).append("/").append(getMethod()).append("?");
        buffer.append("results=" + resultCount);
        buffer.append("&start=" + firstSearchPosition);
        Map params = getParameters();
        for (Object key : params.keySet()) {
            try {
                buffer.append("&");
                buffer.append(URLEncoder.encode(key.toString(), "UTF-8"));
                buffer.append("=");
                buffer.append(URLEncoder.encode(params.get(key).toString(), "UTF-8"));
            } catch (java.io.UnsupportedEncodingException e) {
                throw new MalformedURLException("Failed to construct the url due to bad encoding: " + e.getMessage());
            }
        }
        return new URL(buffer.toString());
    }
    
    /**
     * <p>Creates and returns a ResultsList populated with the search results. This
     * method blocks on I/O, and should be called from a {@link org.jdesktop.swingx.ws.BackgroundWorker}
     * or SwingWorker.</p>
     *
     * <p>This method calls the search with the default values for <code>resultCount</code>
     * (the number of results to return) and <code>firstResultPosition</code> (the position
     * of the first result in the total set of results to return).</p>
     *
     * @see #search(int, int)
     */
    public final ResultsList<T> search() {
        return search(-1, -1);
    }
    
    /**
     * <p>Creates and returns a ResultsList populated with the search results. This
     * method blocks on I/O, and should be called from a {@link org.jdesktop.swingx.ws.BackgroundWorker}
     * or SwingWorker. The criteria used to perform the search is the criteria
     * found in the beans themselves. For instance, YahooWebSearch contains
     * many bean methods to alter the search, such as
     * {@link org.jdesktop.swingx.ws.yahoo.search.websearch#setCountry(Country)},
     * {@link org.jdesktop.swingx.ws.yahoo.search.websearch#setLanguage(Language)}, and
     * {@link org.jdesktop.swingx.ws.yahoo.search.websearch#setQuery(String)}.</p>
     *
     * <p>This method calls the search with the given values for <code>resultCount</code>
     * (the number of results to return) and <code>firstResultPosition</code> (the position
     * of the first result in the total set of results to return).</p>

     * @param firstSearchPosition 1 based position in the stream of all results from
     *        which to collect <code>resultCount</code> number of results. If < 1, then
     *        1 is used.
     *
     * @param resultCount number of results to return. If < 1, then the default result
     *        count will be used for the search (differs based on Yahoo search).
     *
     * @return the list of results. Each concrete class will define the actual
     *         type of these returned results.
     */
    public final ResultsList<T> search(int firstResultPosition, int resultCount) {
        InputStream in = null;
        try {
            in = constructUrl(firstResultPosition, resultCount).openStream();
            return readResults(in);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
