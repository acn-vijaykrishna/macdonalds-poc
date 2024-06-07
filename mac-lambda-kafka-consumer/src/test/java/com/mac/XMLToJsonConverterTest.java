package com.mac;

import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringWriter;

public class XMLToJsonConverterTest {

    public static void main(String[] args) {
        try {
            // Get the ClassLoader
            ClassLoader classLoader = XMLToJsonConverterTest.class.getClassLoader();

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
            doc.getDocumentElement().normalize();

            // Convert the Document to JSON
            String jsonString = convertDocumentToJsonString(doc);
            System.out.println(jsonString);

            // You can now pass this JSON string to another function
            consumeJSONString(jsonString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String convertDocumentToJsonString(Document doc) throws Exception {
        // Convert Document to XML String
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String xmlString = writer.getBuffer().toString();

        // Convert XML String to JSON Object
        JSONObject jsonObject = XML.toJSONObject(xmlString);

        // Convert JSON Object to String
        return jsonObject.toString(4); // Pretty print with 4 spaces indentation
    }

    private static void consumeJSONString(String jsonString) {
        // Function to consume the JSON string
        System.out.println("Consuming JSON String:");
        System.out.println(jsonString);
    }
}