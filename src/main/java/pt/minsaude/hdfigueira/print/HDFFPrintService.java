/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.print;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;

/**
 *
 * @author paulo
 */
public class HDFFPrintService implements PrintService {

    protected List<PrintServiceAttributeListener> printServiceAttributeListeneres = new ArrayList<>();
    
    protected String printerName;
    protected InetSocketAddress address;
    protected List<DocFlavor> docFlavors;

    public HDFFPrintService(String printerName, InetSocketAddress address, List<DocFlavor> docFlavors) {
        this.printerName = printerName;
        this.address = address;
        this.docFlavors = docFlavors;
        
        System.out.println("a criar impressora: "+printerName);
        System.out.println("   - endereço: "+address);
        System.out.println("   - formatos: "+getFlavorsAsString(docFlavors));
    }
    
    @Override
    public String getName() {
        //nome da impressora
        return printerName;
    }

    @Override
    public DocPrintJob createPrintJob() {
        return new HDFFDocPrintJob(this, address);
    }

    @Override
    public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        System.out.println("HDFFPrintService.addPrintServiceAttributeListener(...)");
        if( !printServiceAttributeListeneres.contains(listener) ) {
            printServiceAttributeListeneres.add(listener);
        }
    }

    @Override
    public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        System.out.println("HDFFPrintService.removePrintServiceAttributeListener(...)");
        printServiceAttributeListeneres.remove(listener);
    }

    @Override
    public PrintServiceAttributeSet getAttributes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public DocFlavor[] getSupportedDocFlavors() {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        System.out.println("HDFFPrintService.getSupportedDocFlavors()");
//        return new DocFlavor[] { DocFlavor.INPUT_STREAM.AUTOSENSE };
        return docFlavors.toArray(new DocFlavor[] {});
    }

    @Override
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        System.out.println("HDFFPrintService.isDocFlavorSupported(...)");
        System.out.println("   "+flavor.toString());
//        if( flavor instanceof DocFlavor.INPUT_STREAM ) {
//            return true;
//        }
        return docFlavors.contains(flavor);
//        return false;
    }

    @Override
    public Class<?>[] getSupportedAttributeCategories() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor, AttributeSet attributes) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ServiceUIFactory getServiceUIFactory() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    protected static String getFlavorsAsString(List<DocFlavor> docFlavors) {
        List<String> lista = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(DocFlavor df : docFlavors) {
            String type = df.getMediaType()+"/"+df.getMediaSubtype();
            if( lista.contains(type) ) {
                continue;
            }
            lista.add(type);
            if( sb.length()>0 ) {
                sb.append(", ");
            }
            sb.append(type);
        }
        return sb.toString();
    }
}
