package com.mac;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class XMLReader {

    private static final Logger logger = LogManager.getLogger(XMLReader.class);

    public static String read() {
        long startTime = System.currentTimeMillis();
        logger.info("ENTRY - Method: read, Timestamp: {}", startTime);
        try {
            // Get the ClassLoader
            ClassLoader classLoader = XMLReader.class.getClassLoader();

            // Load the file as an InputStream
            InputStream inputStream = classLoader.getResourceAsStream("test.xml");

            // Check if the file was found
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found! " + "example.xml");
            }

            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            doc.getDocumentElement().normalize();


            // Convert the Document to String
            String xmlString = documentToString(doc);
            //            System.out.println(xmlString);

            // Further processing of the XML document can be done here
            logger.debug("Output in read method Value= {} ",xmlString);
            logger.info("Successfully read the XML file");
            return xmlString;

        } catch (Exception ex) {
            logger.error("Error occurred in Method: XMLReader.read:",ex);
        } finally {
            long endTime = System.currentTimeMillis();
            logger.info("EXIT - Method: XMLReader.read, Timestamp: {}, Duration: {} ms", endTime, endTime - startTime);
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

    public static String readRawMessageKey(Document doc) {

        try {
            // Create XPathFactory and XPath instance
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            // Compile XPath expression to match storeId element
            XPathExpression expr = xpath.compile("/TLD/@storeId");

            // Evaluate the XPath expression against the document
            String storeId = (String) expr.evaluate(doc, XPathConstants.STRING);
            logger.info("readRawMessageKey Read storeId: {}", storeId);
            return storeId;
        } catch (Exception ex) {
            logger.error("Error occurred in Method: readRawMessageKey:",ex);
        }
        return "";
    }

    public static Document getDoc() {
        logger.info("ENTRY - Method: getDoc");
        try {
            // Get the ClassLoader
            ClassLoader classLoader = XMLReader.class.getClassLoader();

            // Load the file as an InputStream
            InputStream inputStream = classLoader.getResourceAsStream("test.xml");

            // Check if the file was found
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found! " + "example.xml");
            }

            // Parse the XML file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            logger.info("Exit - Method: getDoc");
            return doc;

        } catch (Exception ex) {
            logger.error("Error occurred in Method: getDoc:",ex);
        }
        return null;
    }
    public static List<String> readRawMessageList(Document stldDoc) {
        logger.info("ENTRY - Method: readRawMessageList");
        List<String> rawList = new ArrayList<>();
        String storeId = readRawMessageKey(stldDoc);

        NodeList nodeList = stldDoc.getElementsByTagName("Event");
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
        logger.info("EXIT - Method: readRawMessageList - rawList size: {}", rawList.size());
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