package com.mac;

import com.amazonaws.services.lambda.runtime.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * XMLReader is a class that provides methods to read and process XML documents.
 *
 * This class contains methods to read a raw message key from an XML document,
 * read a list of raw messages from an XML document, and convert a node to a string.
 *
 * @author Varun Verma
 */
public class XMLReader {

    /**
     * Reads the raw message key from an XML document.
     *
     * This method takes a Document object and a Lambda Context object as input.
     * It uses XPath to find the storeId attribute in the XML document and returns the value of the storeId attribute.
     * If an error occurs during the process, the method logs the error message and returns an empty string.
     *
     * @param doc The Document object representing the XML file.
     * @param context The Lambda context object containing runtime information.
     * @return The value of the storeId attribute in the XML document as a string.
     */
    public static String readRawMessageKey(Document doc, Context context) {

        try {
            // Create XPathFactory and XPath instance
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            // Compile XPath expression to match storeId element
            XPathExpression expr = xpath.compile("/TLD/@storeId");

            // Evaluate the XPath expression against the document
            String storeId = (String) expr.evaluate(doc, XPathConstants.STRING);
            context.getLogger().log("readRawMessageKey Read storeId: "+ storeId);
            return storeId;
        } catch (Exception ex) {
            context.getLogger().log("Error occurred in Method: readRawMessageKey: "+ex.getMessage());
        }
        return "";
    }

    /**
     * Reads a list of raw messages from an XML document.
     *
     * This method takes a Document object and a Lambda Context object as input.
     * It reads the storeId attribute from the XML document, finds all Event elements in the XML document,
     * checks the Type attribute of each Event element, and adds the XML string of the Event element to a list
     * if the Type attribute is "Ev_Custom" or "TRX_Sale".
     * If an error occurs during the process, the method logs the error message and returns null.
     *
     * @param stldDoc The Document object representing the XML file.
     * @param context The Lambda context object containing runtime information.
     * @return A list of XML strings of the Event elements with Type attribute "Ev_Custom" or "TRX_Sale".
     */
    public static List<String> readRawMessageList(Document stldDoc, Context context) {
        long startTime = System.currentTimeMillis();
        context.getLogger().log("ENTRY - Method: readRawMessageList, Timestamp: "+ startTime);
        try {
            List<String> rawList = new ArrayList<>();
            String storeId = readRawMessageKey(stldDoc, context);

            NodeList nodeList = stldDoc.getElementsByTagName("Event");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eventElement = (Element) node;

                    // Check if Type attribute is Ev_Custom
                    if (eventElement.getAttribute("Type").equals("Ev_Custom") || eventElement.getAttribute("Type").equals("TRX_Sale")) {
                        eventElement.setAttribute("storeId", storeId);
                        String content = null;
                        try {
                            content = nodeToString(node);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        rawList.add(content);
                    }
                }

            }
            context.getLogger().log("Method: readRawMessageList - rawList size: "+ rawList.size());
            return rawList;
        } catch (Exception ex) {
            context.getLogger().log("Error occurred in Method: readRawMessageList: "+ex.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            context.getLogger().log("EXIT - Method: readRawMessageList, Timestamp: "+endTime+", Duration: "+duration+"ms");
        }
        return null;
    }

    private static String nodeToString(Node node) throws Exception {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.getBuffer().toString();
    }

}