package com.mac;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

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
}