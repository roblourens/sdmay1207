/*
 * $Id: Region.java 14 2006-07-14 18:08:23Z rbair $
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
 * Region codes supported by Yahoo! search services
 *
 * @author rbair
 */
public enum Region {
    ARGENTINA("ar"),
    AUSTRALIA("au"),
    AUSTRIA("at"),
    BELGIUM("be"),
    BRAZIL("br"),
    CANADA("ca"),
    CATALAN("ct"),
    DENMARK("dk"),
    FINLAND("fi"),
    FRANCE("fr"),
    GERMANY("de"),
    INDIA("in"),
    INDONESIA("id"),
    ITALY("it"),
    MALAYSIA("my"),
    MEXICO("mx"),
    NETHERLANDS("nl"),
    NORWAY("no"),
    PHILLIPINES("ph"),
    RUSSIAN_FEDERATION("ru"),
    SINGAPORE("sg"),
    SPAIN("es"),
    SWEDEN("se"),
    SWITZERLAND("ch"),
    THAILAND("th"),
    UNITED_KINGDOM("uk"),
    UNITED_STATES("us");
    
    private String code;
    Region(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
}
