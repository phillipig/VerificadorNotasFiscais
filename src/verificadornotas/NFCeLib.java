/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verificadornotas;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.Nfe;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author phill
 */
public class NFCeLib {
    
    private ConfiguracoesNfe config;
    private static NFCeLib instance = null;
    
    private NFCeLib(){
        System.out.println("Iniciando Configurações da NFCeLib...");
        config = iniciarConfiguracoes();
    }
    
    public static NFCeLib getInstance() {
        if(instance == null) instance = new NFCeLib();
        return instance;
    }
    
    public synchronized ConfiguracoesNfe iniciarConfiguracoes() {
        try{
            EstadosEnum uf = null;
            AmbienteEnum ambiente = null;
            
            if(Config.estado.equals("ES")) uf = EstadosEnum.ES;
            else if(Config.estado.equals("MG")) uf = EstadosEnum.MG;
            else if(Config.estado.equals("RJ")) uf = EstadosEnum.RJ;
            else if(Config.estado.equals("SP")) uf = EstadosEnum.SP;
            else throw new CertificadoException("Estado "+Config.estado+" não declarado na inicialização de configurações.");
                
            if(Config.ambiente_nfce.equals("P")){
                ambiente = AmbienteEnum.PRODUCAO;
            }else if(Config.ambiente_nfce.equals("H")){
                ambiente = AmbienteEnum.HOMOLOGACAO;
            }else{
                throw new CertificadoException("Ambiente da NFCe incorreto. Deve ser 0 (Produção) ou 1 (Homologação)");
            }
            
            if(!new File(Config.certificado_path).exists()) throw new CertificadoException("Não foi possível localizar o certificado em "+Config.certificado_path+".");
            Certificado certificado = CertificadoService.certificadoPfx(Config.certificado_path, Config.certificado_senha);

            return ConfiguracoesNfe.criarConfiguracoes(uf, ambiente, certificado, "Schemas");
        }catch(CertificadoException | FileNotFoundException ex){
            System.out.println("Erro: "+ex.getMessage());
            return null;
        }
    }
    
    public synchronized TRetConsSitNFe consultarNFCe(String chave){
        try {  
            TRetConsSitNFe retorno = Nfe.consultaXml(config, chave, DocumentoEnum.NFCE);
            System.out.println("Status: "+retorno.getCStat());
            System.out.println("Motivo: "+retorno.getXMotivo());
            System.out.println("Data: "+retorno.getDhRecbto());
            return retorno;
        } catch (NfeException ex) {  
            System.out.println("Erro: "+ex.getMessage());  
            return null;
        }
    }
}