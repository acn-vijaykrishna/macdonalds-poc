package com.mac;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class XMLReader {

    public static String read() {
        try {
            // Get the ClassLoader
            ClassLoader classLoader = XMLReader.class.getClassLoader();

            // Load the file as an InputStream
            InputStream inputStream = classLoader.getResourceAsStream("test.xml");

            // Check if the file was found
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found! " + "example.xml");
            } else {
                System.out.println("File found!");
            }

            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            System.out.println("dBuilder: " + dBuilder);
            Document doc = dBuilder.parse(inputStream);

            doc.getDocumentElement().normalize();

            // Process the XML document
//            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

            // Convert the Document to String
            String xmlString = documentToString(doc);
//            System.out.println(xmlString);

            // Further processing of the XML document can be done here
            return xmlString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    public static String readLoyaltyKey() {

        try {
            Document doc = getDoc();
            // Create XPathFactory and XPath instance
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // Compile XPath expression to match storeId element
            XPathExpression expr = xpath.compile("/TLD/@storeId");

            // Evaluate the XPath expression against the document
            String storeId = (String) expr.evaluate(doc, XPathConstants.STRING);

            return storeId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Document getDoc() {
        try {
            // Get the ClassLoader
            ClassLoader classLoader = XMLReader.class.getClassLoader();

            // Load the file as an InputStream
            InputStream inputStream = classLoader.getResourceAsStream("test.xml");

            // Check if the file was found
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found! " + "example.xml");
            } else {
                System.out.println("File found!");
            }

            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputStream);

            //doc.getDocumentElement().normalize();

            return doc;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<String> readLoyaltyList() {
        List<String> rawList = new ArrayList<>();
        String storeId = readLoyaltyKey();
        Document doc = getDoc();

        NodeList nodeList = doc.getElementsByTagName("Event");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eventElement = (Element) node;

                // Check if Type attribute is Ev_Custom
                if (eventElement.getAttribute("Type").equals("Ev_Custom") ||
                    eventElement.getAttribute("Type").equals("TRX_Sale")) {
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

        return rawList;
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