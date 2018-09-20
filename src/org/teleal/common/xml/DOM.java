/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.teleal.common.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import java.net.URI;

/**
 * Wraps a W3C document and provides an entry point for browsing the DOM (in subclasses).
 *
 * @author Christian Bauer
 */
public abstract class DOM {

    public static final URI XML_SCHEMA_NAMESPACE =
            URI.create("http://www.w3.org/2001/xml.xsd");

    public static final String CDATA_BEGIN = "<![CDATA[";
    public static final String CDATA_END = "]]>";

    private Document dom;

    public DOM(Document dom) {
        this.dom = dom;
    }

    public Document getW3CDocument() {
        return dom;
    }

    public Element createRoot(String name) {
        Element el = getW3CDocument().createElementNS(getRootElementNamespace(), name);
        getW3CDocument().appendChild(el);
        return el;
    }

    public abstract String getRootElementNamespace();
    public abstract DOMElement getRoot(XPath xpath);
    public abstract DOM copy();

}
