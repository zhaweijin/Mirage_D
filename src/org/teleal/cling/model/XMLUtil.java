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

package org.teleal.cling.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.R.integer;

import com.mirage.dlna.R.string;
import com.mirage.dmp.ImageDisplay;
import com.mirage.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XML handling and printing shortcuts.
 * <p>
 * This class exists because Android 2.1 does not offer any way to print an <code>org.w3c.dom.Document</code>,
 * and it also doesn't implement the most trivial methods to build a DOM (although the API is provided, they
 * fail at runtime). We might be able to remove this class once compatibility for Android 2.1 can be
 * dropped.
 * </p>
 *
 * @author Christian Bauer
 */
public class XMLUtil {

	private static String TAG = "XMLUtil";
    /* TODO: How it should be done (nice API, eh?)
    public static String documentToString(Document document) throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        transFactory.setAttribute("indent-number", 4);
        Transformer transformer = transFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        StringWriter out = new StringWriter();
        transformer.transform(new DOMSource(d), new StreamResult(out));
        return out.toString();
    }
    */

    // TODO: Evil methods to print XML on Android 2.1 (there is no TransformerFactory)

    public static String documentToString(Document document) throws Exception {
        return documentToString(document, true);
    }

    public static String documentToString(Document document, boolean standalone) throws Exception {
        String prol = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"" + (standalone ? "yes" : "no") + "\"?>";
        return prol + nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
    }

    public static String documentToFragmentString(Document document) throws Exception {
        return nodeToString(document.getDocumentElement(), new HashSet(), document.getDocumentElement().getNamespaceURI());
    }

    protected static String nodeToString(Node node, Set<String> parentPrefixes, String namespaceURI) throws Exception {
        StringBuilder b = new StringBuilder();

        if (node == null) {
            return "";
        }

        if (node instanceof Element) {
            Element element = (Element) node;
            b.append("<");
            b.append(element.getNodeName());

            Map<String, String> thisLevelPrefixes = new HashMap();
            if (element.getPrefix() != null && !parentPrefixes.contains(element.getPrefix())) {
                thisLevelPrefixes.put(element.getPrefix(), element.getNamespaceURI());
            }

            if (element.hasAttributes()) {
                NamedNodeMap map = element.getAttributes();
                for (int i = 0; i < map.getLength(); i++) {
                    Node attr = map.item(i);
                    if (attr.getNodeName().startsWith("xmlns")) continue;
                    if (attr.getPrefix() != null && !parentPrefixes.contains(attr.getPrefix())) {
                        thisLevelPrefixes.put(attr.getPrefix(), element.getNamespaceURI());
                    }
                    b.append(" ");
                    b.append(attr.getNodeName());
                    b.append("=\"");
                    b.append(attr.getNodeValue());
                    b.append("\"");
                }
            }

            if (namespaceURI != null && !thisLevelPrefixes.containsValue(namespaceURI) &&
                    !namespaceURI.equals(element.getParentNode().getNamespaceURI())) {
                b.append(" xmlns=\"").append(namespaceURI).append("\"");
            }

            for (Map.Entry<String, String> entry : thisLevelPrefixes.entrySet()) {
                b.append(" xmlns:").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                parentPrefixes.add(entry.getKey());
            }

            NodeList children = element.getChildNodes();
            boolean hasOnlyAttributes = true;
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() != Node.ATTRIBUTE_NODE) {
                    hasOnlyAttributes = false;
                    break;
                }
            }
            if (!hasOnlyAttributes) {
                b.append(">");
                for (int i = 0; i < children.getLength(); i++) {
                    b.append(nodeToString(children.item(i), parentPrefixes, children.item(i).getNamespaceURI()));
                }
                b.append("</");
                b.append(element.getNodeName());
                b.append(">");
            } else {
                b.append("/>");
            }

            for (String thisLevelPrefix : thisLevelPrefixes.keySet()) {
                parentPrefixes.remove(thisLevelPrefix);
            }

        } else if (node.getNodeValue() != null) {
            b.append(encodeText(node.getNodeValue()));
        }

        return b.toString();
    }

    protected static String encodeText(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\"", "&quot;");
        return s;
    }

    public static Element appendNewElement(Document document, Element parent, Enum el) {
        return appendNewElement(document, parent, el.toString());
    }

    public static Element appendNewElement(Document document, Element parent, String element) {
        Element child = document.createElement(element);
        parent.appendChild(child);
        return child;
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content) {
        return appendNewElementIfNotNull(document, parent, el, content, null);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, Enum el, Object content, String namespace) {
        return appendNewElementIfNotNull(document, parent, el.toString(), content, namespace);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content) {
        return appendNewElementIfNotNull(document, parent, element, content, null);
    }

    public static Element appendNewElementIfNotNull(Document document, Element parent, String element, Object content, String namespace) {
        if (content == null) return parent;
        return appendNewElement(document, parent, element, content, namespace);
    }

    public static Element appendNewElement(Document document, Element parent, String element, Object content) {
        return appendNewElement(document, parent, element, content, null);
    }

    public static Element appendNewElement(Document document, Element parent, String element, Object content, String namespace) {
        Element childElement;
        if (namespace != null) {
            childElement = document.createElementNS(namespace, element);
        } else {
            childElement = document.createElement(element);
        }

        if (content != null) {
            // TODO: We'll have that on Android 2.2:
            // childElement.setTextContent(content.toString());
            // Meanwhile:
            childElement.appendChild(document.createTextNode(content.toString()));
        }

        parent.appendChild(childElement);
        return childElement;
    }

    // TODO: Of course, there is no Element.getTextContent() either...
    public static String getTextContent(Node node) {
//    	Utils.print(TAG, "gettext");
        StringBuffer buffer = new StringBuffer();
        NodeList childList = node.getChildNodes();
        int size = childList.getLength(); 
        Node child;
        for (int i = 0; i < size; i++) {
                child = childList.item(i);
                if (child.getNodeType() != Node.TEXT_NODE)
                    continue; // skip non-text nodes
                buffer.append(child.getNodeValue());
         }
        return buffer.toString();
    }
    
//	// TODO: Of course, there is no Element.getTextContent() either...
//	public static String getTextContent(Node node) {
//		StringBuffer buffer = new StringBuffer(34);
//		NodeList childList = node.getChildNodes();
//		// Utils.print(TAG, childList.getLength()+"");
//		int BUFFER_MAX = 65536;  //2《16次方 
//		int size = childList.getLength();
//		int capSize = 0;
//		int tempSize = 0;
//		boolean remainder = false;
//		String result = "";
//		for (int i = 0; i < size; i++) {
//			Node child = childList.item(i);
//			if (child.getNodeType() != Node.TEXT_NODE)
//				continue; // skip non-text nodes
//			capSize = capSize + child.getNodeValue().length();
////			Utils.print("xmlutil", "xmlutil"+capSize);
//			if (capSize > BUFFER_MAX-1) {
//				Utils.print("xmlutil", "xmlutil"+capSize);
//				remainder = false;
//				capSize =0;
//				result = result + buffer.toString();
//				buffer.delete(0, buffer.length());
//				Utils.print("buffer--size", "xmlutil--"+buffer.length());
////				tempSize = tempSize + child.getNodeValue().length();
////				if (tempSize > BUFFER_MAX-1) {
////					tempSize = 0;
////					remainder = false;
////					result = result + buffer.toString();
////					buffer.delete(0, buffer.length());
////					Utils.print("xmlutil", "xmlutilADD");
////				} else {
////					buffer.append(child.getNodeValue());
////					remainder = true;
////				}
//			} else {
//				buffer.append(child.getNodeValue());
//				remainder = true;
//			}
//		}
//
//		
//		if(remainder){
//			result = result + buffer.toString();
//			buffer.delete(0, buffer.length());
//		}
//			
//		
//		return result;
//	}
    
    
//    public static String getTextContent(Node node){
//    	
//    	try {
//    		//store file
//        	File dirFile = new File("/data/data/com.mirage.dlna/tempXML/");
//    		if(!dirFile.exists())
//    			dirFile.mkdirs();
//    		
//    		String filepath = dirFile.getAbsolutePath() + "/"+ "exchage.xml";
//    		Utils.print(TAG, "filepath:" + filepath);
//    		File tempFile = new File(filepath);
//    		BufferedWriter writer=null;
//    		if(!tempFile.exists()){
//    			tempFile.createNewFile();
//    			writer=new BufferedWriter(new FileWriter(tempFile));
//    			
//    			NodeList childList = node.getChildNodes();
//    	        int size = childList.getLength(); 
//    	        for (int i = 0; i < size; i++) {
//    	                Node child = childList.item(i);
//    	                if (child.getNodeType() != Node.TEXT_NODE)
//    	                    continue; // skip non-text nodes
//    	                writer.write(child.getNodeValue());       
//    	         }
//    	         writer.flush();
//    	         writer.close();
//    		}
//    		Utils.print(TAG, "store finished");
//    		//read file
////    		FileReader in=null;
////            BufferedReader read=null;
////            in = new FileReader(dirFile.getAbsolutePath() + "/"+ "exchage.xml");
////            read=new BufferedReader(in);
////            String s="";
////            String result="";
////            while ((s = read.readLine()) != null) {
////                   result = result + s;
////             }
////             read.close();
////             tempFile.delete();
////             
////             Utils.print("result", result);
//    		
//    		
//    		String result="";
//    		
//            long contentlength = tempFile.length();
//            ByteArrayOutputStream outstream = new ByteArrayOutputStream(contentlength>0?(int)contentlength:1024);
//            byte[] buffer = new byte[4096];
//            int len;
//            
//            InputStream is = new BufferedInputStream( new FileInputStream(tempFile));
//            while ((len = is.read(buffer)) > 0) {
//                outstream.write(buffer, 0, len);
//            }           
//            outstream.close();
//            result = outstream.toString();
//            is.close();
//            
//            
//            tempFile.delete();
//            
//            return result;
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			return "";
//		}
//
//    }

}