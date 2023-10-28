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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    protected static Map<String,Map<String,String>> commands = new HashMap<>();
    protected static Map<String,Map<String,String>> links = new HashMap<>();
    
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
                    
                    links.put(regex, prop);
                }
                
                for(Map<String,Object> command : commandsConfig) {
                    String name = command.keySet().iterator().next();
                    System.out.println(name);
                    
                    Map prop = (Map)command.get(name);
                    String regex = (String)prop.get("regex");
                    String cmd = (String)prop.get("command");
                    
                    System.out.println("   regex: "+regex);
                    System.out.println("   command: "+cmd);
                    
                    commands.put(regex, prop);
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
    
    protected static boolean execute(String cmd, Map<String,Map<String,String>> map, boolean force) {
        System.out.println("executing: \""+cmd+"\"");
        for(String regex : map.keySet()) {
            if( cmd.matches(regex) ) {
                if( map.get(regex).get("noregex")!=null &&
                        cmd.matches(map.get(regex).get("noregex")) ) {
                    continue;
                }
                cmd = cmd.replaceAll(regex, map.get(regex).get("command"));
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
            case "#transform#":
                transformUrl(param);
                return true;
            case "#exit#":
                runExit(param);
                return true;
            default:
                //mostrar erro
                String message = "Comando deconhecido: '"+parts[0]+"'";
                System.out.println(message);
//                if(!GraphicsEnvironment.isHeadless()) {
//                    JOptionPane.showMessageDialog(null, message, "Ocorreu um erro", JOptionPane.ERROR_MESSAGE);
//                }
        }
        
        return false;
    }
    
    protected static Map<String,String> transformKeys = new HashMap<>();
    protected static Pattern RANDOM = Pattern.compile(".*?(#rnd(:(?:static|global)-.*?)?(:\\d+)?(:.*?)?#).*?");
    
    protected static boolean transformUrl(String strUrl) {
        StringBuilder newUrl = new StringBuilder();
        try {
//            System.out.println("vai testar: "+strUrl);
            Matcher m = RANDOM.matcher(strUrl);
            
            int lastIndex = 0;
            while( m.find() ) {
//                System.out.println("\n\nfind: "+m.group(1));
                
                String key = null;
//                System.out.println("groupCount: "+m.groupCount());
                
                if( m.group(2)!=null ) {
                    String type = m.group(2);
//                    System.out.println("type="+type);
                    if( type.startsWith(":global-") ) {
                        key = type;
                    } else if( type.startsWith(":static-") ) {
                        int paramsIndex = strUrl.indexOf("?");
                        if( paramsIndex==-1 ) {
                            paramsIndex = strUrl.length();
                        }
                        key = strUrl.substring(0, paramsIndex)+"-"+type;
                    }
                }
                
//                System.out.println("key = "+key);
                
                if( key!=null && transformKeys.containsKey(key) ) {
                    String random = transformKeys.get(key);
                    
                    newUrl.append(strUrl, lastIndex, m.start(1)).append(random);
                    lastIndex = m.end(1);
                } else {
                    
                    int size = 10;
                    if( m.group(3)!=null ) {
                        try {
                            size = Integer.parseInt(m.group(3).substring(1));
                        } catch(Exception e) {}
                    }

                    String chars = "x";
                    if( m.group(4)!=null ) {
                        chars = m.group(4).substring(1);
                    }

                    String random = random(size, chars);
                    
                    if( key!=null ) {
                        transformKeys.put(key, random);
                    }
                    
                    newUrl.append(strUrl, lastIndex, m.start(1)).append(random);
                    lastIndex = m.end(1);
                }
            }
            
            if (lastIndex < strUrl.length()) {
                newUrl.append(strUrl, lastIndex, strUrl.length());
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return executeLink(newUrl.toString(),true);
    }
    
    //https://stackoverflow.com/a/1547940/662855
    protected static String RAMDOM_CHARS_A = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    protected static String RAMDOM_CHARS_a = "abcdefghijklmnopqrstuvwxyz";
    protected static String RAMDOM_CHARS_n = "0123456789";
    //                                        -._~:/?#[]@!$&'()*+,;=
    protected static String RAMDOM_CHARS_s = "-._~:/?[]@!$'()*+,;=";
    protected static String RAMDOM_CHARS_ALL = RAMDOM_CHARS_A+RAMDOM_CHARS_a+RAMDOM_CHARS_n+RAMDOM_CHARS_s;
    
    protected static String random(int size, String chars) {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i=0; i<size; i++) {
            switch (chars.charAt(r.nextInt(chars.length()))) {
                case 'a':
                    //letras minusculas
                    sb.append(RAMDOM_CHARS_a.charAt(r.nextInt(RAMDOM_CHARS_a.length())));
                    break;
                case 'A':
                    //letras maiusculas
                    sb.append(RAMDOM_CHARS_A.charAt(r.nextInt(RAMDOM_CHARS_A.length())));
                    break;
                case 's':
                    //simbolos
                    sb.append(RAMDOM_CHARS_s.charAt(r.nextInt(RAMDOM_CHARS_s.length())));
                    break;
                case 'n':
                    //numeros
                    sb.append(RAMDOM_CHARS_n.charAt(r.nextInt(RAMDOM_CHARS_n.length())));
                    break;
                default:
                    //qualquer caracter
                    sb.append(RAMDOM_CHARS_ALL.charAt(r.nextInt(RAMDOM_CHARS_ALL.length())));
                    break;
            }
        }
        return sb.toString();
    }
    
    protected static boolean runFollow(String strUrl) {
        try {
            Document doc = Jsoup.connect(strUrl).get();
            //System.out.println(doc.html());

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
                /*
                String html = doc.html();
                URL base = new URL(new URL(doc.baseUri()),".");
                html = html.replace("src=\"SAM", "src=\""+base+"SAM");
                html = html.replace("src=\"..", "src=\""+base+"..");
                html = html.replace("href=\"..", "href=\""+base+"..");
                */

                //transformar os links em absolutos
                Elements select = doc.select("a");
                for (Element e : select){
                    String absUrl = e.absUrl("href");
                    e.attr("href", absUrl);
                }
                select = doc.select("img");
                for (Element e : select){
                    e.attr("src", e.absUrl("src"));
                }
                select = doc.select("frame");
                for (Element e : select){
                    e.attr("src", e.absUrl("src"));
                }
                
                String html = doc.html();
                
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
