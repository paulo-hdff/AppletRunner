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
public class HDFFBypassPrintService implements PrintService {
    protected String printerName;
    PrintService printService;

    public HDFFBypassPrintService(String printerName, PrintService printService) {
        this.printerName = printerName;
        this.printService = printService;
        
        System.out.println("a criar impressora: "+printerName);
    }
    
    public PrintService getBypassPrintService() {
        return printService;
    }
    
    @Override
    public String getName() {
        //nome da impressora
        return printerName;
    }

    @Override
    public DocPrintJob createPrintJob() {
        return new HDFFBypassDocPrintJob(this);
    }

    @Override
    public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        printService.addPrintServiceAttributeListener(listener);
    }

    @Override
    public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {
        printService.removePrintServiceAttributeListener(listener);
    }

    @Override
    public PrintServiceAttributeSet getAttributes() {
        return printService.getAttributes();
    }

    @Override
    public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
        return printService.getAttribute(category);
    }

    @Override
    public DocFlavor[] getSupportedDocFlavors() {
        return printService.getSupportedDocFlavors();
    }

    @Override
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        return printService.isDocFlavorSupported(flavor);
    }

    @Override
    public Class<?>[] getSupportedAttributeCategories() {
        return printService.getSupportedAttributeCategories();
    }

    @Override
    public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
        return printService.isAttributeCategorySupported(category);
    }

    @Override
    public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
        return printService.getDefaultAttributeValue(category);
    }

    @Override
    public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor, AttributeSet attributes) {
        return printService.getSupportedAttributeValues(category, flavor, attributes);
    }

    @Override
    public boolean isAttributeValueSupported(Attribute attrval, DocFlavor flavor, AttributeSet attributes) {
        return printService.isAttributeValueSupported(attrval, flavor, attributes);
    }

    @Override
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
        return printService.getUnsupportedAttributes(flavor, attributes);
    }

    @Override
    public ServiceUIFactory getServiceUIFactory() {
        return printService.getServiceUIFactory();
    }
    
}
