package com.mac;


import org.json.JSONObject;
import org.json.XML;


public class XMLToJsonConverter {

    public static String convertXmlToJson(String xmlString) {
        try {
            // Convert the XML string to a JSON object
            JSONObject jsonObject = XML.toJSONObject(xmlString);

            // Convert the JSON object to a JSON string
            return jsonObject.toString(4); // 4 is the indentation level for pretty print
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convert(String xmlString) {
        String jsonString = convertXmlToJson(xmlString);

        if (jsonString != null) {
            System.out.println("Converted JSON: " + jsonString);
        } else {
            System.out.println("Failed to convert XML to JSON.");
        }
        return jsonString;
    }

}