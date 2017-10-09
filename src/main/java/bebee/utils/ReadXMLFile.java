package bebee.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class ReadXMLFile {

    public static void main(String argv[]) {

        try {
            File fXmlFile = new File("/Users/Rivas/Desktop/20minutos.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("description");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String content = eElement.getTextContent();
                    String content2 = content.replace("<p>AP</p>", "");
                    String content3 = content2.replace("<p>", System.getProperty("line.separator"));
                    String result = content3.replace("</p>", "");

                    String array = parsePunctuation(result);

                    BufferedWriter output = null;
                    File file = new File("/Users/Rivas/Desktop/noticias2/pais"+temp+".txt");
                    output = new BufferedWriter(new FileWriter(file));
                    output.write(array);

                    output.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parsePunctuation(String document){
        String words = document.replaceAll("[^a-zA-Zá-ú\\n ]", "");
        return words;
    }
}