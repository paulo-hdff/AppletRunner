/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author paulo
 */
public class ExternalApplications {
    
    protected static Map<String,String> commands = new HashMap<>();
    protected static Map<String,String> links = new HashMap<>();
    
    public static void loadExternalApplications() {

        File externalApps = new File("apps.yml");
        if (!externalApps.exists()) {
            File path = Utils.getPath();
            if (path != null) {
                externalApps = new File(path, "apps.yml");
            }
        }

        if (externalApps.exists()) {
            System.out.println("loading external applications from " + externalApps.getAbsolutePath());

            try {
                Map<String,Object> config;
                Yaml yaml = new Yaml();
                config = yaml.load(new FileInputStream(externalApps));

                List<Map<String,Object>> linksConfig = (List<Map<String,Object>>)config.get("links");
                List<Map<String,Object>> commandsConfig = (List<Map<String,Object>>)config.get("commands");
                
                for(Map<String,Object> link : linksConfig) {
                    String name = link.keySet().iterator().next();
                    System.out.println(name);
                    
                    Map prop = (Map)link.get(name);
                    String regex = (String)prop.get("regex");
                    String command = (String)prop.get("command");
                    
                    System.out.println("   regex: "+regex);
                    System.out.println("   command: "+command);
                    
                    links.put(regex, command);
                }
                
                for(Map<String,Object> command : commandsConfig) {
                    String name = command.keySet().iterator().next();
                    System.out.println(name);
                    
                    Map prop = (Map)command.get(name);
                    String regex = (String)prop.get("regex");
                    String cmd = (String)prop.get("command");
                    
                    System.out.println("   regex: "+regex);
                    System.out.println("   command: "+cmd);
                    
                    commands.put(regex, cmd);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }
    
    
    public static boolean executeCommand(String cmd) {
        return execute(cmd, commands, false);
    }
    
    public static boolean executeLink(String link) {
        return execute(link, links, false);
    }
    
    public static boolean executeLink(String link, boolean force) {
        return execute(link, links, force);
    }
    
    protected static boolean execute(String cmd, Map<String,String> map, boolean force) {
        System.out.println("executing: \""+cmd+"\"");
        for(String regex : map.keySet()) {
            //System.out.println("testing "+regex);
            if( cmd.matches(regex)) {
                cmd = cmd.replaceAll(regex, map.get(regex));
                System.out.println("running "+cmd);
                return run(cmd);
            }
        }
        
        if( force ) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(cmd));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        
        return false;
    }
    
    protected static boolean run(String cmd) {
        String[] parts = cmd.split(" ", 2);
        String command = parts[0].toLowerCase();
        String param = null;
        if( parts.length>1 ) {
            param = parts[1];
        }
        
        switch (command) {
            case "#follow#":
                return runFollow(param);
            case "#browser_gc#":
                runChrome(param);
                return true;
            case "#browser_ie#":
                runIE(param);
                return true;
            case "#exit#":
                runExit(param);
                return true;
            default:
                //mostrar erro
                String message = "Comando deconhecido: '"+parts[0]+"'";
                System.out.println(message);
                if(!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null, message, "Ocorreu um erro", JOptionPane.ERROR_MESSAGE);
                }
        }
        
        return false;
    }
    
    
    protected static boolean runFollow(String strUrl) {
        try {
            Document doc = Jsoup.connect(strUrl).get();
            System.out.println(doc.html());

            String action = null;
            String method = null;
            String params = null;
            
            Elements forms = doc.select("form");
            for(Element form : forms) {
                action = form.attr("action");
                method = form.attr("method");
                Elements inputs = doc.select("input");
                for(Element input : inputs) {
                    if( params==null ) {
                        params = input.attr("name")+"="+URLEncoder.encode(input.attr("value"),"UTF-8");
                    } else {
                        params += "&"+input.attr("name")+"="+URLEncoder.encode(input.attr("value"),"UTF-8");
                    }
                }
            }
            
            if( action!=null && method!=null && !method.toLowerCase().equals("post") ) {
                if( params!=null && action.contains("?") && !action.endsWith("?") ) {
                    params = "&"+params;
                }
                return executeLink(action+(params==null?"":params),true);
            } else if( doc.html().contains("SAM.dados_pce?sessionid") || (method!=null && method.toLowerCase().equals("post")) ) {
                String html = doc.html();
                URL base = new URL(new URL(doc.baseUri()),".");
                html = html.replace("src=\"SAM", "src=\""+base+"SAM");
                html = html.replace("src=\"..", "src=\""+base+"..");
                html = html.replace("href=\"..", "href=\""+base+"..");
                
                String request = "/"+strUrl.substring(strUrl.indexOf("?"));
                HttpServer.getInstance().addRequest(request, html);
                String newUrl = "http://localhost:"+HttpServer.getInstance().getPort()+request;
                //return executeLink(newUrl,true);
                runIE(newUrl);
                return true;
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    
    //abrir URL num browser especifico
    //https://stackoverflow.com/questions/24239046/java-open-url-in-chrome-browser-only
    //https://stackoverflow.com/questions/45660482/open-a-url-in-chrome-using-java-in-linux-and-mac
    //https://stackoverflow.com/questions/12061541/java-opening-url-in-internet-explorer-using-java-desktop
    
    protected static void runChrome(String url) {
        try {
            //Windows
            if( OSValidator.isWindows() ) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c","start chrome \""+url+"\""});
            } else if( OSValidator.isUnix() ) {
                //Linux
                Runtime.getRuntime().exec(new String[]{"bash", "-c", "google-chrome \""+url+"\""});
            } else if( OSValidator.isMac() ) {
                //MacOS
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-a", "/Applications/Google Chrome.app", url});
            } else {
                //mostrar erro
                String message = "Sistema operativo deconhecido";
                System.out.println(message);
                if(!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null, message, "Ocorreu um erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    protected static void runIE(String url) {
        try {
            //Windows
            if( OSValidator.isWindows() ) {
                //Runtime.getRuntime().exec(new String[]{"iexplore.exe "+url});
                Runtime.getRuntime().exec(new String[]{"cmd", "/c","start iexplore \""+url+"\""});
            } else {
                //mostrar erro
                String message = "Só é possível executar o Internet Explorer no Windows";
                System.out.println(message);
                if(!GraphicsEnvironment.isHeadless()) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        int response = JOptionPane.showConfirmDialog(null, 
                                message+"\nQuer executar no browser por defeito?", 
                                "Ocorreu um erro", JOptionPane.YES_NO_OPTION);
                        if( response==JOptionPane.YES_OPTION ) {
                            try {
                                Desktop.getDesktop().browse(new URI(url));
                            } catch (Exception ex) {
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, message, "Ocorreu um erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    protected static void runExit(String status) {
        int st = 0;
        if( status!=null && !status.isEmpty() ) {
            try {
                st = Integer.parseInt(status);
            } catch(Exception e) {}
        }
        Utils.exit(st);
    }
}
