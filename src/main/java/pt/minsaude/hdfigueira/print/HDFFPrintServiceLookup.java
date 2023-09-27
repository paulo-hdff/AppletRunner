/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.print;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author paulo
 *
 * https://stackoverflow.com/questions/1655297/print-to-specific-printer-ipp-uri-in-java
 *
 */
public class HDFFPrintServiceLookup extends PrintServiceLookup {

    protected List<PrintService> printServices;

    public static void register() {
        PrintServiceLookup.registerServiceProvider(new HDFFPrintServiceLookup());
    }

    public HDFFPrintServiceLookup() {
        printServices = new ArrayList<>();

        loadPrinters();
    }

    protected void loadPrinters() {

        File printersFile = new File("printers.yml");
        if (!printersFile.exists()) {
            File path = getPath();
            if (path != null) {
                printersFile = new File(path, "printers.yml");
            }
        }

        if (printersFile.exists()) {
            System.out.println("loading printers from " + printersFile.getAbsolutePath());

            try {
                List<Map<String,Object>> config;
                Yaml yaml = new Yaml();
                //System.out.println(yaml.load(new FileInputStream(printersFile)));
                config = yaml.load(new FileInputStream(printersFile));

                for(Map<String,Object> printer : config) {
                    String name = printer.keySet().iterator().next();
                    //System.out.println(name);
                    
                    Map prop = (Map)printer.get(name);
                    String host = (String)prop.get("host");
                    int port = 9100;
                    try {
                        port = (Integer)prop.get("port");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    
                    List<String> doc = (List)prop.get("type");
                    //System.out.println(doc);
                    
                    List<DocFlavor> docFlavors = new ArrayList<>();
                    for(String docType : doc) {
                        switch(docType.toUpperCase()) {
                            case "TEXT":
                                docFlavors.add(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST);
                                docFlavors.add(DocFlavor.CHAR_ARRAY.TEXT_PLAIN);
                                docFlavors.add(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST);
                                docFlavors.add(DocFlavor.STRING.TEXT_PLAIN);
                                docFlavors.add(DocFlavor.READER.TEXT_PLAIN);
                                break;
                            case "PDF":
                                docFlavors.add(DocFlavor.INPUT_STREAM.PDF);
                                docFlavors.add(DocFlavor.BYTE_ARRAY.PDF);
                                break;
                            case "POSTSCRIPT":
                                docFlavors.add(DocFlavor.INPUT_STREAM.POSTSCRIPT);
                                docFlavors.add(DocFlavor.BYTE_ARRAY.POSTSCRIPT);
                                break;
                            case "PCL":
                                docFlavors.add(DocFlavor.INPUT_STREAM.PCL);
                                docFlavors.add(DocFlavor.BYTE_ARRAY.PCL);
                                break;
                        }
                        //docFlavors.add(DocFlavor.INPUT_STREAM.AUTOSENSE);
                    }
                    HDFFPrintService pr = new HDFFPrintService(name, new InetSocketAddress(host, port), docFlavors);
                    printServices.add(pr);
                    
                    
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    @Override
    public PrintService[] getPrintServices(DocFlavor flavor, AttributeSet attributes) {
        return printServices.toArray(new PrintService[]{});
    }

    @Override
    public PrintService[] getPrintServices() {
        return printServices.toArray(new PrintService[]{});
    }

    @Override
    public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors, AttributeSet attributes) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public PrintService getDefaultPrintService() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    protected static File getPath_() {
        try {
            File path = new File(HDFFPrintServiceLookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (path.isDirectory()) {
                return path;
            }
            return path.getParentFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static File getPath() {
        try {
            File path = null;
            URL url = HDFFPrintServiceLookup.class.getProtectionDomain().getCodeSource().getLocation();
            
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
