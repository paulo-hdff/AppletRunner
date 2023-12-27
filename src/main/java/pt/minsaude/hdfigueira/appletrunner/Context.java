/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author paulo
 */
public class Context implements AppletContext {

    protected static Map<URL,BufferedImage> imageCache = new HashMap<>();
    protected Stub stub = null;
    
    public Context() {
    }
    
    public Context(Stub stub) {
        this.stub = stub;
    }
    
    @Override
    public AudioClip getAudioClip(URL url) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Image getImage(URL url) {
        if( imageCache.containsKey(url) ) {
//            System.out.println("getImage cached: "+url);
            return imageCache.get(url);
        }
//        System.out.println("getImage: "+url);
        BufferedImage img = null;
        try {
            img = ImageIO.read(url);
            imageCache.put(url, img);
        } catch(Exception ex) {
            if( stub!=null && stub.getURLClassLoader()!=null ) {
                String codeBase = stub.getCodeBase().toString();
                String strUrl = url.toString();
                if( strUrl.startsWith(codeBase) ) {
                    String path = "!/"+strUrl.substring(codeBase.length());
//                    System.out.println("path="+path);
                    for(URL u : stub.getURLClassLoader().getURLs()) {
                        try {
//                            System.out.println("url="+u);
                            if( u.toString().toLowerCase().endsWith(".jar") ) {
                                URL imageUrl = new URL("jar:"+u+path);
//                                System.out.println("imageUrl="+imageUrl);
                                img = ImageIO.read(imageUrl);
                                imageCache.put(url, img);
                                return img;
                            }
                        } catch(Exception exx) {}
                    }
                }
            }
            Logger.getLogger(Context.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            try {
                System.out.println("<<<< Não encontrou imagem: "+url);
                img = ImageIO.read(getClass().getResource("1x1.gif"));
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return img;
    }

    @Override
    public Applet getApplet(String string) {
        return null;
    }

    @Override
    public Enumeration<Applet> getApplets() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void showDocument(URL url) {
        //System.out.println("url: "+url);
        
        if( ExternalApplications.executeLink(url.toString().replace(" ", "+")) ) {
            return;
        }

        //https://stackoverflow.com/questions/5226212/how-to-open-the-default-webbrowser-using-java
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
//                Desktop.getDesktop().browse(url.toURI());
                Desktop.getDesktop().browse(new URI(url.toString().replace(" ", "+")));
            } catch (Exception ex) {
                Logger.getLogger(Context.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void showDocument(URL url, String string) {
        showDocument(url);
    }

    @Override
    public void showStatus(String string) {
        System.out.println("STATUS: "+string);
    }

    @Override
    public void setStream(String string, InputStream in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public InputStream getStream(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Iterator<String> getStreamKeys() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
