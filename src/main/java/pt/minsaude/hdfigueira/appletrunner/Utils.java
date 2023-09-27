/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.io.File;
import java.net.URL;

/**
 *
 * @author paulo
 */
public class Utils {
    
    
    public static void exit(int status) {
        if( OSValidator.isUnix() ) {
            //*não* corre os hooks de saida
            Runtime.getRuntime().halt(status);
        }        
        //corre os hooks de saida
        System.exit(status);
    }
 
    public static String getCurrentCodeSourceLocation() {
        URL url = Utils.class.getProtectionDomain().getCodeSource().getLocation();
        return url.toString();
    }
    
    public static File getPath() {
        try {
            File path = null;
            URL url = Utils.class.getProtectionDomain().getCodeSource().getLocation();
            
            if( url.toString().startsWith("file://") ) {
                String uncPath = url.toString();
                uncPath = "\\\\"+uncPath.replace("file://", "").replace("/", "\\");
                path = new File(uncPath);
            } else {
                path = new File(url.toURI());
            }
            
            if (path.isDirectory()) {
                return path;
            }
            return path.getParentFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }    
    
}
