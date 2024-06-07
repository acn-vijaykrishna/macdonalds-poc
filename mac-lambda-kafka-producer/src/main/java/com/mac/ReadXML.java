import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadXML {

    public static void readXml() {
        String folderPath = "/src/test/resources/POS_store-db_FR_1000_20240511_1.20240511021353.xml"; // Replace with your folder path

         try {
                // Read XML files from folder
                Files.list(Paths.get(folderPath))
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".xml"))
                        .forEach(XmlReader::parseXmlFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    private static void parseXmlFile(Path path) {
        try {
            File xmlFile = path.toFile();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // Normalize XML structure
            document.getDocumentElement().normalize();

            // Example: print root element
            System.out.println("Root Element :" + document.getDocumentElement().getNodeName());

            // Example: print all child nodes
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    System.out.println("Element Name :" + element.getNodeName() + ", Value: " + element.getTextContent());
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
