/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.print;

import java.awt.GraphicsEnvironment;
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

/**
 *
 * @author paulo
 */
public class HDFFDocPrintJob implements DocPrintJob {

    protected PrintService printService;
    protected List<PrintJobListener> printJobListeneres = new ArrayList<>();
    protected List<PrintJobAttributeListener> printJobAttributeListeneres = new ArrayList<>();

    protected InetSocketAddress address;

    public HDFFDocPrintJob(PrintService printService, InetSocketAddress address) {
        this.printService = printService;
        this.address = address;
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
        //conversão ZPL para epson
        //https://www.neodynamic.com/products/zpl-printer-web-api-docker/
        //http://labelary.com/viewer.html
        
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
            if (result.size() > 0) {

                // StandardCharsets.UTF_8.name() > JDK 7
//                System.out.println("------------------");
//                System.out.println(result.toString("UTF-8").substring(0, 100));
//                System.out.println("------------------");

//            Socket client = new Socket("192.168.77.50", 9100);
                Socket client = new Socket(address.getAddress(), address.getPort());
                OutputStream out = client.getOutputStream();
                out.write(result.toByteArray());
                client.close();
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
