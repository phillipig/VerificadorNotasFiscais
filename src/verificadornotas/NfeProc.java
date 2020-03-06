/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verificadornotas;

import java.io.BufferedReader;  
import java.io.ByteArrayInputStream;  
import java.io.ByteArrayOutputStream;  
import java.io.FileInputStream;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;  
import javax.xml.transform.Transformer;  
import javax.xml.transform.TransformerException;  
import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.dom.DOMSource;  
import javax.xml.transform.stream.StreamResult;  
import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;  
import org.xml.sax.SAXException;  

/**
 *
 * @author phill
 */
public class NfeProc {
    
    public String criaNfeProc(String xmlEnviNFe, String xmlRetConsReciNFe) {  
        String nfeProc = null;
        try {  
            Document document = documentFactory(xmlEnviNFe);  
            NodeList nodeListNfe = document.getElementsByTagName("NFe");  
            NodeList nodeListInfNfe = document.getElementsByTagName("infNFe");  
            for (int i = 0; i < nodeListNfe.getLength(); i++) {  
                Element el = (Element) nodeListInfNfe.item(i);  
                String chaveNFe = el.getAttribute("Id");                  
                String xmlNFe = outputXML(nodeListNfe.item(i));  
                String xmlProtNFe = getProtNFe(xmlRetConsReciNFe, chaveNFe.replace("NFe", "")); 
                nfeProc = buildNFeProc(xmlNFe, xmlProtNFe);
                info("ChaveNFe.....: " + chaveNFe);               
                info("NFe..........: " + xmlNFe);  
                info("ProtNFe......: " + xmlProtNFe);  
                info("NFeProc......: " + nfeProc);  
            }  
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException ex) {   
            error(ex.getMessage());
        }  
        return nfeProc;
    }  
  
    private String buildNFeProc(String xmlNFe, String xmlProtNFe) {  
        StringBuilder nfeProc = new StringBuilder();  
        nfeProc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")  
            .append("<nfeProc versao=\"4.00\" xmlns=\"http://www.portalfiscal.inf.br/nfe\">")  
            .append(xmlNFe)  
            .append(xmlProtNFe)  
            .append("</nfeProc>");  
        return nfeProc.toString();  
    }  
      
    private String getProtNFe(String xml, String chaveNFe) throws SAXException, IOException, ParserConfigurationException, TransformerException {  
        Document document = documentFactory(xml);  
        NodeList nodeListProtNFe = document.getElementsByTagName("protNFe");
        NodeList nodeListChNFe = document.getElementsByTagName("chNFe");  
        for (int i = 0; i < nodeListProtNFe.getLength(); i++) {  
            Element el = (Element) nodeListChNFe.item(i);  
            String chaveProtNFe = el.getTextContent(); 
            if (chaveNFe.contains(chaveProtNFe)) {  
                return outputXML(nodeListProtNFe.item(i));  
            }  
        }         
        return "";  
    }  
  
    private String outputXML(Node node) throws TransformerException {  
        ByteArrayOutputStream os = new ByteArrayOutputStream();  
        TransformerFactory tf = TransformerFactory.newInstance();  
        Transformer trans = tf.newTransformer();  
        trans.transform(new DOMSource(node), new StreamResult(os));  
        String xml = os.toString();  
        if ((xml != null) && (!"".equals(xml))) {  
            xml = xml.replaceAll("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>", "");  
        } 
        return xml;  
    }  
      
    private Document documentFactory(String xml) throws SAXException, IOException, ParserConfigurationException {  
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        factory.setNamespaceAware(true);  
        Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));  
        return document;  
    }  
  
    public String lerXML(String fileXML) throws IOException {    
        String linha = "";    
        StringBuilder xml = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileXML)));    
        while ((linha = in.readLine()) != null) {    
            xml.append(linha);    
        }    
        in.close();    
        return xml.toString();    
    }  
    
    private void error(String error) {  
        System.out.println("| ERROR: " + error);  
    }  
    
    private void info(String info) {  
        System.out.println("| INFO: " + info);  
    }
}
