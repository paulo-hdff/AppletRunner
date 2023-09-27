/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author paulo
 */
public class HttpServer extends Thread {
    
    ServerSocket server;
    Map<String, String> pages = new CaseInsensitiveMap<>();
    
    protected static HttpServer INSTANCE = new HttpServer();
    
    public static HttpServer getInstance() {
        return INSTANCE;
    }
    
    private HttpServer() {
        this.setDaemon(true);
        this.setName("http_server");
        try {
            try {
                server = new ServerSocket(8080);
            } catch(Exception e) {
                e.printStackTrace();
                server = new ServerSocket(0);
            }
            start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addRequest(String url, String body) {
        pages.put(url, body);
    }
    
    @Override
    public void run() {
        while( true ) {
            try {
                final Socket s = server.accept();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("recebeu ligação: "+s.getRemoteSocketAddress());
                            InputStream in = s.getInputStream();
                            BufferedReader br = new BufferedReader(new InputStreamReader(in));
                            String line;
                            String request = null;
                            while( (line=br.readLine())!=null ) {
                                System.out.println(">> "+line);
                                if( request==null ) {
                                    System.out.println("request = "+line);
                                    try {
                                        String[] parts = line.split(" ");
                                        request = parts[1];
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if( line.isEmpty() ) {
                                    break;
                                }
                            }
                            //System.out.println("FIM dos headers");

                            String html = pages.get(request);

                            OutputStream out = s.getOutputStream();
                            OutputStreamWriter o = new OutputStreamWriter(out);
                            if( html == null ) {
                                o.write("HTTP/1.1 404 Not found\r\n");
                                //o.write("Content-Length: "+html.length()+"\r\n");
                                o.write("Connection: close\r\n");
                                o.write("Content-Type: text/html; charset=ISO-8859-15\r\n");
                                o.write("\r\n");
                            } else {
                                o.write("HTTP/1.1 200 OK\r\n");
                                //o.write("Content-Length: "+html.length()+"\r\n");
                                o.write("Connection: close\r\n");
                                o.write("Content-Type: text/html; charset=ISO-8859-15\r\n");
                                o.write("\r\n");
                                o.write(html);
                            }
                            o.close();
                            s.close();
                            System.out.println("Fim de request "+request);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch(Exception e) {
                e.printStackTrace();
                //break;
            }
        }
        /*
        try {
            server.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        */
    }
    
    public int getPort() {
        return server.getLocalPort();
    }
    
    
}
