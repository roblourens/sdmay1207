/*
 * $Id: YahooImageSearch.java 39 2006-07-17 20:43:55Z rbair $
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

package org.jdesktop.swingx.ws.yahoo.search.imagesearch;

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
import org.jdesktop.swingx.ws.yahoo.search.ResultsArrayList;
import org.jdesktop.swingx.ws.yahoo.search.ResultsList;
import org.jdesktop.swingx.ws.yahoo.search.Type;
import org.jdesktop.swingx.ws.yahoo.search.YahooSearch;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A non visual JavaBean that leverages the Yahoo! image search service.</p>
 *
 * @author rbair
 */
public final class YahooImageSearch extends YahooSearch {
    private String appId;
    private String query;
    private Type type;
    private FileFormat format;
    private boolean adultOk;
    private Coloration coloration;
    //can include up to 30 different sites
    private Set<String> sites = new HashSet<String>();
    
    /** Creates a new instance of YahooWebContextSearch */
    public YahooImageSearch() {
    }

    /**
     * @inheritDoc
     */
    protected final String getVersionNumber() {
        return "V1";
    }

    /**
     * @inheritDoc
     */
    protected final String getServiceName() {
        return "ImageSearchService";
    }
    
    /**
     * @inheritDoc
     */
    protected final String getMethod() {
        return "imageSearch";
    }

    /**
     * @inheritDoc
     */
    protected final Map getParameters() {
        Map params = new HashMap();
        params.put("appid", appId);
        params.put("query", query);
        if (type != null) {
            params.put("type", type.getCode());
        }
        if (format != null) {
            params.put("format", format.getCode());
        }
        if (coloration != null) {
            params.put("coloration", coloration.getCode());
        }
        params.put("adult_ok", adultOk ? 1 : 0);
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
     * @return the Type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type code. See 
     * <a href="http://developer.yahoo.com/search/image/V1/imageSearch.html">Yahoo!</a> for more info
     */
    public void setType(Type type) {
        Type old = getType();
        this.type = type;
        firePropertyChange("type", old, getType());
    }

    /**
     * @return the file format code
     */
    public FileFormat getFileFormat() {
        return format;
    }

    /**
     * Sets the file format code. See 
     * <a href="http://developer.yahoo.com/search/image/V1/imageSearch.html">Yahoo!</a> for more info
     */
    public void setFileFormat(FileFormat format) {
        FileFormat old = getFileFormat();
        this.format = format;
        firePropertyChange("fileFormat", old, getFileFormat());
    }

    /**
     * @return true if adult content is ok
     */
    public boolean isAdultOk() {
        return adultOk;
    }

    /**
     * Sets the adult ok boolean. See 
     * <a href="http://developer.yahoo.com/search/image/V1/imageSearch.html">Yahoo!</a> for more info
     */
    public void setAdultOk(boolean adultOk) {
        boolean old = isAdultOk();
        this.adultOk = adultOk;
        firePropertyChange("adultOk", old, isAdultOk());
    }
    
    /**
     * @return the coloration code
     */
    public Coloration getColoration() {
        return coloration;
    }

    /**
     * Sets the format code. See 
     * <a href="http://developer.yahoo.com/search/image/V1/imageSearch.html">Yahoo!</a> for more info
     */
    public void setColoration(Coloration coloration) {
        Coloration old = getColoration();
        this.coloration = coloration;
        firePropertyChange("coloration", old, getColoration());
    }

    /**
     *
     * @author rbair
     */
    public static final class Result {
        private String title;
        private String summary;
        private URL url;
        private URL clickUrl;
        private URL refererUrl;
        private int fileSize;
        private FileFormat fileFormat;
        private int height;
        private int width;
        private URL thumbnail;
        private String publisher;
        //String restrictions?
        private String copyright;

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public URL getUrl() {
            return url;
        }

        public URL getClickUrl() {
            return clickUrl;
        }

        public URL getRefererUrl() {
            return refererUrl;
        }

        public int getFileSize() {
            return fileSize;
        }

        public FileFormat getFileFormat() {
            return fileFormat;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public URL getThumbnail() {
            return thumbnail;
        }

        public String getPublisher() {
            return publisher;
        }

        public String getCopyright() {
            return copyright;
        }
    }
    
    private final class Parser extends DefaultHandler {
        private StringBuilder buffer;
        private ResultsList<Result> results;
        private Result result;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("ResultSet".equals(qName)) {
                results = new ResultsArrayList<Result>(YahooImageSearch.this,
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
                } else if ("RefererUrl".equals(qName)) {
                    result.refererUrl = new URL(buffer.toString());
                } else if ("FileSize".equals(qName)) {
                    result.fileSize = Integer.parseInt(buffer.toString());
                } else if ("FileFormat".equals(qName)) {
                    String format = buffer.toString();
                    for (FileFormat f : FileFormat.values()) {
                        if (f.getCode().equals(format)) {
                            result.fileFormat = f;
                            break;
                        }
                    }
                } else if ("Height".equals(qName)) {
                    result.height = Integer.parseInt(buffer.toString());
                } else if ("Width".equals(qName)) {
                    result.width = Integer.parseInt(buffer.toString());
                } else if ("Thumbnail".equals(qName)) {
//                    result.thumbnail = new URL(buffer.toString());
                } else if ("Publisher".equals(qName)) {
                    result.publisher = buffer.toString();
                } else if ("Restrictions".equals(qName)) {
                    System.out.println(buffer.toString());
                } else if ("Copyright".equals(qName)) {
                    result.copyright = buffer.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void characters(char[] chars, int start, int offset) throws SAXException {
            if (buffer != null) {
                buffer.append(chars, start, offset);
            } else {
                System.out.println(new String(chars, start, offset));
            }
        }
    }
    
//    public static void main(String[] args) {
//        YahooImageSearch yahoo = new YahooImageSearch();
//        yahoo.appId = "swing-aerith";
//        yahoo.query = "world cup";
//        ResultsList<Result> results = yahoo.search();
//        int count = 0;
//        for (Result r : results) {
//            System.out.println(count + results.getFirstResultPosition() + ".\t" + r.title);
//            System.out.println("\t" + r.summary);
//            System.out.println("\t" + r.clickUrl);
//            count++;
//        }
//    }
}
