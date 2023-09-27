/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author paulo
 */
public class Stub implements AppletStub {

    private URL documentBase = null;
    private String codeBase;
    private Map<String,String> parameters = new HashMap<>();
    
    public Stub() {}
    
    public Stub(URL documentBase, String codeBase, Map<String,String> parameters) {
        this.documentBase = documentBase;
        this.codeBase = codeBase;
        this.parameters = parameters;
    }
    
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        System.out.println("documentBaseUrl = "+documentBase);
        return documentBase;
    }

    @Override
    public URL getCodeBase() {
        try {
            URL codeBaseUrl = new URL(documentBase, codeBase.endsWith("/")?codeBase:codeBase+"/");
//            System.out.println("codeBaseUrl = "+codeBaseUrl);
            return codeBaseUrl;
        } catch(Exception ex) {
            Logger.getLogger(Stub.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public String getParameter(String name) {
        //System.out.println("parameter: "+name+"="+parameters.get(name));
        return parameters.get(name);
    }

    @Override
    public AppletContext getAppletContext() {
        return new Context();
    }

    @Override
    public void appletResize(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
