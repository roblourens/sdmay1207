/*
 * $Id: YahooContextSearch.java 26 2006-07-17 18:34:01Z rbair $
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.jdesktop.swingx.ws.yahoo.search.Country;
import org.jdesktop.swingx.ws.yahoo.search.Format;
import org.jdesktop.swingx.ws.yahoo.search.Language;
import org.jdesktop.swingx.ws.yahoo.search.License;
import org.jdesktop.swingx.ws.yahoo.search.ResultsArrayList;
import org.jdesktop.swingx.ws.yahoo.search.ResultsList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Non visual JavaBean for calling Yahoo! context search and retrieving the search results.
 * To use, simply specify a query, a context, and your appId. (AppId is a value you get from
 * Yahoo -- each application must have an id).
 * 
 * <p>This bean is related to the <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">
 * Yahoo! Web Search</a> web service.
 *
 * @author rbair
 */
public final class YahooContextSearch extends YahooWebSearchService {
    private String appId;
    private String query;
    private String context;
    private Format format;
    private boolean adultOk;
    private boolean similarOk;
    private Language language;
    private Country country;
    //can include up to 30 different sites
    //TODO add support (haven't added yet because of lack of JavaBeans support in IDEs)
    private Set<String> sites = new HashSet<String>();
    private License license;
    
    /** Creates a new instance of YahooContextSearch */
    public YahooContextSearch() {
    }

    /**
     * @inheritDoc
     */
    protected final String getMethod() {
        return "contextSearch";
    }

    /**
     * @inheritDoc
     */
    protected final Map getParameters() {
        Map params = new HashMap();
        params.put("appid", appId);
        params.put("query", query);
        if (context != null) {
            params.put("context", context);
        }
        if (format != null) {
            params.put("format", format.getCode());
        }
        params.put("adult_ok", adultOk ? 1 : 0);
        params.put("similar_ok", similarOk ? 1 : 0);
        if (language != null) {
            params.put("language", language.getCode());
        }
        if (country != null) {
            params.put("country", country.getCode());
        }
        if (sites.size() > 0) {
            StringBuilder buffer = new StringBuilder();
            for (String site : sites) {
                if (buffer.length() > 0) {
                    buffer.append("&");
                    buffer.append("site=");
                    buffer.append(site);
                } else {
                    buffer.append(site);
                }
            }
            params.put("site", buffer.toString());
        }
        if (license != null) {
            params.put("license", license.getCode());
        }
        return params;
    }
    
    /**
     * @inheritDoc
     */
    protected ResultsList<Result> readResults(InputStream in) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p;
        p = factory.newSAXParser();
        Parser pp = new Parser();
        p.parse(in, pp);
        return pp.results;
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

    /**
     * @return the search context to use
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the search context to use.
     */
    public void setContext(String context) {
        String old = getContext();
        this.context = context;
        firePropertyChange("context", old, getContext());
    }

    /**
     * @return the format code
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Sets the format code. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setFormat(Format format) {
        Format old = getFormat();
        this.format = format;
        firePropertyChange("format", old, getFormat());
    }

    /**
     * @return true if adult content is ok
     */
    public boolean isAdultOk() {
        return adultOk;
    }

    /**
     * Sets the adult ok boolean. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setAdultOk(boolean adultOk) {
        boolean old = isAdultOk();
        this.adultOk = adultOk;
        firePropertyChange("adultOk", old, isAdultOk());
    }

    /**
     * @return true if similar search results are ok
     */
    public boolean isSimilarOk() {
        return similarOk;
    }

    /**
     * Sets the similar ok flag. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setSimilarOk(boolean similarOk) {
        boolean old = isSimilarOk();
        this.similarOk = similarOk;
        firePropertyChange("similarOk", old, isSimilarOk());
    }

    /**
     * @return the language code
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the language code. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setLanguage(Language language) {
        Language old = getLanguage();
        this.language = language;
        firePropertyChange("language", old, getLanguage());
    }

    /**
     * @return the country code
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the country code. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setCountry(Country country) {
        Country old = getCountry();
        this.country = country;
        firePropertyChange("country", old, getCountry());
    }

    /**
     * @return the license code
     */
    public License getLicense() {
        return license;
    }

    /**
     * Sets the license code. See 
     * <a href="http://developer.yahoo.com/search/web/V1/contextSearch.html">Yahoo!</a> for more info
     */
    public void setLicense(License license) {
        License old = getLicense();
        this.license = license;
        firePropertyChange("license", old, getLicense());
    }
    
    /**
     * The Result of performing a search. This object is immutable.
     */
    public static final class Result {
        private String title;
        private String summary;
        private URL url;
        private URL clickUrl;
        private String mimeType;
        private Date modDate;
        private URL cache;

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public URL getUrl() {
            //TODO should create a new URL based on this one, I think.
            return url;
        }

        public URL getClickUrl() {
            //TODO should create a new URL based on this one, I think.
            return clickUrl;
        }

        public String getMimeType() {
            return mimeType;
        }

        public Date getModDate() {
            return modDate == null ? null : new Date(modDate.getTime());
        }

        public URL getCache() {
            //TODO should create a new URL based on this one, I think.
            return cache;
        }
    }
    
    private final class Parser extends DefaultHandler {
        private StringBuilder buffer;
        private ResultsList<Result> results = null;
        private Result result;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("ResultSet".equals(qName)) {
                results = new ResultsArrayList<Result>(YahooContextSearch.this,
                    Integer.parseInt(attributes.getValue("totalResultsAvailable")),
                    Integer.parseInt(attributes.getValue("firstResultPosition")));
            } else if ("Result".equals(qName)) {
                result = new Result();
                results.add(result);
            } else {
                buffer = new StringBuilder();
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            try {
                if ("Title".equals(qName)) {
                    result.title = buffer.toString();
                } else if ("Summary".equals(qName)) {
                    result.summary = buffer.toString();
                } else if ("Url".equals(qName)) {
                    result.url = new URL(buffer.toString());
                } else if ("ClickUrl".equals(qName)) {
                    result.clickUrl = new URL(buffer.toString());
                } else if ("MimeType".equals(qName)) {
                    result.mimeType = buffer.toString();
                } else if ("ModificationDate".equals(qName)) {
                    result.modDate = new Date(Long.parseLong(buffer.toString()));
                } else if ("Cache".equals(qName)) {
//                    System.out.println(buffer.toString());
                    //the description on the website doesn't make sense:
                    //"The URL of the cached result, and its size in bytes"
                    //In reality, I'm just getting the size
//                    result.cache = new URL(buffer.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void characters(char[] chars, int start, int offset) throws SAXException {
            buffer.append(chars, start, offset);
        }
    }
    
//    public static void main(String[] args) {
//        YahooContextSearch yahoo = new YahooContextSearch();
//        yahoo.appId = "swing-aerith";
//        yahoo.query = "world cup";
//        yahoo.context = "brazil";
//        
//        int count = 0;
//        ResultsList<Result> results = (ResultsList<Result>)yahoo.search();
//        for (Result r : results) {
//            System.out.println(count + results.getFirstResultPosition() + ".\t" + r.title);
//            System.out.println("\t" + r.summary);
//            System.out.println("\t" + r.modDate);
//            count++;
//        }
//    }
}