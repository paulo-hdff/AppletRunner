/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.print;

import java.awt.GraphicsEnvironment;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import javax.swing.JOptionPane;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

/**
 *
 * @author paulo
 */
public class HDFFBypassDocPrintJob implements DocPrintJob {

    protected HDFFBypassPrintService printService;
    protected List<PrintJobListener> printJobListeneres = new ArrayList<>();
    protected List<PrintJobAttributeListener> printJobAttributeListeneres = new ArrayList<>();

    public HDFFBypassDocPrintJob(HDFFBypassPrintService printService) {
        this.printService = printService;
    }

    @Override
    public PrintService getPrintService() {
        return printService;
    }

    @Override
    public PrintJobAttributeSet getAttributes() {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        System.out.println("HDFFDocPrintJob.getAttributes()");
        return new HashPrintJobAttributeSet();
    }

    @Override
    public void addPrintJobListener(PrintJobListener listener) {
        System.out.println("HDFFDocPrintJob.addPrintJobListener(...)");
        if (!printJobListeneres.contains(listener)) {
            printJobListeneres.add(listener);
        }
    }

    @Override
    public void removePrintJobListener(PrintJobListener listener) {
        System.out.println("HDFFDocPrintJob.removePrintJobListener(...)");
        printJobListeneres.remove(listener);
    }

    @Override
    public void addPrintJobAttributeListener(PrintJobAttributeListener listener, PrintJobAttributeSet attributes) {
        System.out.println("HDFFDocPrintJob.addPrintJobAttributeListener(...)");
        if (!printJobAttributeListeneres.contains(listener)) {
            printJobAttributeListeneres.add(listener);
        }
    }

    @Override
    public void removePrintJobAttributeListener(PrintJobAttributeListener listener) {
        System.out.println("HDFFDocPrintJob.removePrintJobAttributeListener(...)");
        printJobAttributeListeneres.remove(listener);
    }

    @Override
    public void print(Doc doc, PrintRequestAttributeSet attributes) throws PrintException {
        System.out.println("a imprimir ");
        if( attributes!=null ) {
            for (Attribute a : attributes.toArray()) {
                System.out.println(" - " + a.getName());
            }
        }

        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();

            InputStream in = doc.getStreamForBytes();
            if (in != null) {
                byte[] buffer = new byte[1024];
                for (int length; (length = in.read(buffer)) != -1;) {
                    result.write(buffer, 0, length);
                }
            } else {
                Reader reader = doc.getReaderForText();
                if (reader != null) {
                    StringBuilder builder = new StringBuilder();
                    char[] buffer = new char[1024];
                    for (int length; (length = reader.read(buffer)) != -1;) {
                        builder.append(buffer, 0, length);
                    }
                    result.write(builder.toString().getBytes("US_ASCII"));
                    //result.write(builder.toString().getBytes("ISO_8859_1"));
                }
            }

            System.out.println("tamanho dos dados de impressão: "+result.size());
            
            //PDF to ZPL: https://stackoverflow.com/a/41445567
            //ZPL to PDF: https://stackoverflow.com/a/20386205
            //            https://labelary.com/service.html
            if (result.size() > 0) {
                RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(new ByteArrayInputStream(result.toByteArray()));
                PDDocument document = Loader.loadPDF(buffer);
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPageable(new PDFPageable(document));
                job.setPrintService(printService.getBypassPrintService());
                job.print();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if(!GraphicsEnvironment.isHeadless()) {
                String message = e.getLocalizedMessage();
                if( message==null ) {
                    message = "Erro desconhecido";
                }
                if( message.contains("Connection refused") ) {
                    message += "\nVerifique se a impressora está ligada";
                }
                JOptionPane.showMessageDialog(null, message, "Ocorreu um erro a imprimir", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

}
