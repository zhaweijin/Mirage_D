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
package org.teleal.common.xhtml;

import org.teleal.common.xml.DOM;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;

/**
 * @author Christian Bauer
 */
public class XHTML extends DOM {

    public static final String NAMESPACE_URI = "http://www.w3.org/1999/xhtml";
    public static final String SCHEMA_RESOURCE = "org/teleal/common/schemas/xhtml1-strict.xsd";

    public static Source[] createSchemaSources() {
        // We can't put this into a static field, the crazy parsers actually seem to modify the sourcs
        // and they can not be shared!
        return new Source[]{new StreamSource(
                // Also, we assume that it's in the same classloader scope
                XHTML.class.getClassLoader().getResourceAsStream(SCHEMA_RESOURCE)
        )};
    }

    public enum ELEMENT {
        html, head, title, meta, link, script, style,
        body, div, span, p, object, a, img, pre,
        h1, h2, h3, h4, h5, h6,
        table, thead, tfoot, tbody, tr, th, td,
        ul, ol, li, dl, dt, dd,
        form, input, select, option;
    }

    public enum ATTR {
        id, style, title,
        type, href, name, content, scheme, rel, rev,
        colspan, rowspan, src, alt,
        action, method;

        public static final String CLASS = "class";
    }

    public XHTML(Document dom) {
        super(dom);
    }

    public Root createRoot(XPath xpath, ELEMENT elememt) {
        super.createRoot(elememt.name());
        return getRoot(xpath);
    }

    @Override
    public String getRootElementNamespace() {
        return NAMESPACE_URI;
    }

    @Override
    public Root getRoot(XPath xpath) {
        return new Root(xpath, getW3CDocument().getDocumentElement());
    }

    @Override
    public XHTML copy() {
        return new XHTML((Document)getW3CDocument().cloneNode(true));
    }

}