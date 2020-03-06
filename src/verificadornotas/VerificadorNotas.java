/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verificadornotas;

import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Phillipi
 */
public class VerificadorNotas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            JSONObject config = lerJSON("config.json");
            Config.estado = config.getString("estado");
            Config.ambiente_nfce = config.getString("ambiente_nfce");
            Config.certificado_path = config.getString("certificado_path");
            Config.certificado_senha = config.getString("certificado_senha");
            JSONArray notas = config.getJSONArray("notas");
            System.out.println("Total de XMLs para resolver: "+notas.length());
            resolverXMLs(notas);
        } catch (IOException | JSONException ex){
            System.out.println(ex.getMessage());
        }
    }
    
    public static void resolverXMLs(JSONArray array){
        Map<String,Integer> notas = new HashMap<>();
        for(int i=0; i<array.length(); i++){
            notas.put(array.getString(i), 1);
        }
        File dirxmls = new File("notas");
        File[] arquivos = dirxmls.listFiles();
        for(int i=0; i<arquivos.length; i++){
            String xmldir = arquivos[i].getAbsolutePath();
            String nota = arquivos[i].getName().replaceAll("[^0-9]", "");
            if(!notas.containsKey(nota) && !notas.isEmpty()){
                System.out.println("Pulou "+xmldir);
                continue;
            }
            System.out.println("Consultando "+nota);
            TRetConsSitNFe retorno = NFCeLib.getInstance().consultarNFCe(nota);
            if(retorno != null){
                try {
                    NfeProc nfeProc = new NfeProc();
                    String xmlProtNfe = XmlNfeUtil.objectToXml(retorno.getProtNFe());
                    String xmlNfeProc = nfeProc.criaNfeProc(nfeProc.lerXML(xmldir), xmlProtNfe);
                    gravarXML(xmlNfeProc, xmldir.replace(".xml", "-resolvido.xml"));
                } catch (IOException | NfeException | JAXBException ex) {
                    System.out.println(ex.getMessage());
                }
            }else{
                System.out.println("ERRO SEFAZ");
            }
        }
    }
    
    public static JSONObject lerJSON(String filename) throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        return new JSONObject(sb.toString());
    }
    
    public static void gravarXML(String buffer, String path){
        try {
            File arquivo = new File(path);
            FileOutputStream fos = new FileOutputStream(arquivo);
            fos.write(buffer.getBytes());
            fos.close(); 
        }catch (IOException ex){
            Logger.getLogger(VerificadorNotas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
