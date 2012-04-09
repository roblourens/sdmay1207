/*
 * $Id: YahooSpellingSuggestion.java 28 2006-07-17 18:40:36Z rbair $
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

package org.jdesktop.swingx.ws.yahoo.search.websearch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.jdesktop.swingx.ws.yahoo.search.ResultsArrayList;
import org.jdesktop.swingx.ws.yahoo.search.ResultsList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>The YahooSpellingSuggestion bean provides a suggested spelling correction for
 * a given term. The <a href="http://developer.yahoo.com/search/web/V1/spellingSuggestion.html">Yahoo!</a>
 * website for more information.</p>
 *
 * <p>The #readResults method in this class simply returns a ResultsList of Strings
 * that met the spelling suggestion requirement.</p>
 *
 * @author rbair
 */
public final class YahooSpellingSuggestion extends YahooWebSearchService {
    private String appId;
    private String query;

    /** Creates a new instance of YahooSpellingSuggestion */
    public YahooSpellingSuggestion() {
    }

    /**
     * @inheritDoc
     */
    protected final String getMethod() {
        return "spellingSuggestion";
    }

    /**
     * @inheritDoc
     */
    protected Map getParameters() {
        Map params = new HashMap();
        params.put("appid", appId);
        params.put("query", query);
        return params;
    }
    
    /**
     * @inheritDoc
     */
    protected ResultsList<String> readResults(InputStream in) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p;
        p = factory.newSAXParser();
        Parser pp = new Parser();
        p.parse(in, pp);
        
        ResultsList<String> results = new ResultsArrayList<String>(this, pp.results.size(), 1);
        for (String s : pp.results) {
            results.add(s);
        }
        
        return results;
    }
    
    /**
     * @return the Yahoo! app id in use for this component
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Sets the Yahoo app id to use with this component. Without an app id, no
     * searches can be executed. You must get an app id from yahoo. See
     * <a href="http://developer.yahoo.com/">the Yahoo docs</a> for more info.
     *
     * @param appId
     */
    public void setAppId(String appId) {
        String old = getAppId();
        this.appId = appId;
        firePropertyChange("appId", old, getAppId());
    }

    /**
     * @return the search query to use
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the search query to use. This follows all the same conventions as the web based
     * Yahoo! search engine
     */
    public void setQuery(String query) {
        String old = getQuery();
        this.query = query;
        firePropertyChange("query", old, getQuery());
    }

    private static final class Parser extends DefaultHandler {
        private StringBuilder buffer;
        private List<String> results = new ArrayList<String>();

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            buffer = new StringBuilder();
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            try {
                if ("Result".equals(qName)) {
                    results.add(buffer.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void characters(char[] chars, int start, int offset) throws SAXException {
            if (buffer != null) {
                buffer.append(chars, start, offset);
            }
        }
    }
    
//    public static void main(String[] args) {
//        YahooSpellingSuggestion yahoo = new YahooSpellingSuggestion();
//        yahoo.appId = "swing-aerith";
//        yahoo.query = "Madnna";
//        ResultsList<String> results = (ResultsList<String>)yahoo.search();
//        int count = 0;
//        for (String r : results) {
//            System.out.println(count + ".\t" + r);
//            count++;
//        }
//    }
}
