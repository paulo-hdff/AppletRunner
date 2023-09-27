/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.print;

import java.io.FileInputStream;
import java.io.IOException;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Finishings;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.Sides;
import pt.minsaude.hdfigueira.Hosts;

/**
 *
 * @author paulo
 */
public class Main {

    public static void main(String[] args) throws Exception {

        Hosts.loadHostsFile();
        HDFFPrintServiceLookup.register();
        
        printPS();

    }

    private static void printPS() {
        DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(MediaSizeName.ISO_A4);
        aset.add(new Copies(2));
        aset.add(Sides.TWO_SIDED_LONG_EDGE);
        aset.add(Finishings.STAPLE);

        PrintService[] pservices = PrintServiceLookup.lookupPrintServices(flavor, aset);
        if (pservices.length > 0) {
            for (int i = 0; i < pservices.length; i++) {
                System.out.println("selected printer " + pservices[i].getName());

                DocPrintJob pj = pservices[i].createPrintJob();

                try {
                    FileInputStream fis = new FileInputStream("exemplo.pdf");
                    Doc doc = new SimpleDoc(fis, flavor, null);
                    if( pservices[i] instanceof HDFFPrintService ) {
                        pj.print(doc, aset);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (PrintException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Não encontrou impressoras");
        }

    }

}
