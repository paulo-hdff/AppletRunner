/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import com.alibaba.dcm.DnsCacheManipulator;
import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import pt.minsaude.hdfigueira.Hosts;
import pt.minsaude.hdfigueira.print.HDFFPrintServiceLookup;
import oracle.forms.demos.StartAppInterface;

/**
 * java.lang.ClassCastException: sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData
 * https://www.mathworks.com/matlabcentral/answers/373897-external-monitor-throws-java-exception
 * 
 * http://www.perfectabstractions.com/blog/how-to-install-windows-fonts-in-java-on-linux
 * 
 * @author paulo
 */
public class AppletRunner {

    public AppletRunner(URL url) throws IOException {
        
        
        Document doc = Jsoup.connect(url.toString()).get();
        Elements applets = doc.select("object");
        for(Element applet : applets) {
            if( "object".equals(applet.nodeName()) ) {
                
                Map<String,String> parameters = parseElementParameters(applet);
                System.out.println(parameters);
                
                if( url.toString().contains("config=sonho") ) {
                    parameters.put("separateFrame","false");
                }

                //parseObjectTag(applet);
                String archive = applet.attr("archive");
                if( archive==null || archive.isEmpty() ) {
                    archive = parameters.get("archive");
                }
                System.out.println("archive = "+archive);

                String codebase = applet.attr("codebase");
                if( codebase==null || codebase.contains("/java.sun.com") ) {
                    codebase = parameters.get("CODEBASE");
                    System.out.println("codebase dos parametros = "+codebase);
                }
                System.out.println("codebase = "+codebase);

                String code = applet.attr( "code");
                if( code==null || code.isEmpty() ) {
                    code = parameters.get("code");
                }
                System.out.println("code = "+code);

                ClassLoader cl = getClassLoader(url, codebase, archive);
                
                if( code!=null ) {
                    if( code.endsWith(".class") ) {
                        code = code.substring(0, code.length()-6);
                    }
                    try {
                        System.out.println("a criar o stub");
                        Stub stub = new Stub(url, codebase, parameters);
                        createApplet(cl, code, stub);
                    } catch(Exception e) {
                        throw new IOException(e);
                    }
                }
                
            }
        }
    }

    private ClassLoader getClassLoader(URL base, String codeBase, String archive) throws MalformedURLException {
        if( !codeBase.endsWith("/") ) {
            codeBase += "/";
        }
        base = new URL(base, codeBase);
        List<URL> urls = new ArrayList<>();
        
        System.out.println("PATH = "+Utils.getPath());
        
        URL url = null;
        File libFile = new File("demo-1.0.jar");
        if (!libFile.exists()) {
            File path = Utils.getPath();
            if (path != null) {
                libFile = new File(path, "demo-1.0.jar");
            }
        }

        if (libFile.exists()) {
            url = libFile.toURI().toURL();
        }
        if( url!=null ) {
            System.out.println(url.toString());
            urls.add(url);
        }
        
        String[] archives = archive.split(",");
        for(String jar : archives) {
            jar = jar.trim();
            URL u = new URL(base, jar);
            if( OSValidator.isUnix() ) {
                u = downloadFile(u);
            }
            System.out.println(u.toString());
            urls.add(u);
        }
        
        URLClassLoader urlcl = new URLClassLoader(urls.toArray(new URL[]{}));
        
        return urlcl;
    }
    
    private URL downloadFile(URL url) {
        try {
            //System.out.println(url);
            //System.out.println(url.getFile());
            String filename = url.getFile();
            if( filename.contains("/") ) {
                filename = filename.substring(filename.lastIndexOf("/")+1);
            }
            //File tmpFile = File.createTempFile(url.getFile(), ".jar");
            String dir = System.getProperty("java.io.tmpdir");
            File tmpFile = new File(dir + File.separator + filename);
            tmpFile.deleteOnExit();
            
            byte[] buffer = new byte[1024];
            int len;
            OutputStream out = new FileOutputStream(tmpFile);
            InputStream in = url.openStream();
            while( (len=in.read(buffer))>0 ) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
            return tmpFile.toURI().toURL();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return url;
    }
    
    private String getAttribute(String html, String tag) {
        String htmlLower = html.toLowerCase();
        int start = htmlLower.indexOf(tag.toLowerCase()+"=");
        if( start>=0 ) {
            start += tag.length()+1;
            String delim = html.substring(start, start+1);
            start++;
            int end = html.indexOf(delim, start);
            return html.substring(start, end);
        }
        return null;
    } 
    
    private Map<String,String> parseParameters(String html) {
        Map<String,String> map = new HashMap<>();
        
        String htmlLower = html.toLowerCase();
        
        int start = 0;
        while( start!=-1 ) {
            start = htmlLower.indexOf("<param ", start);
            if( start==-1 ) break;
            int end = htmlLower.indexOf(">", start);
            String param = html.substring(start, end);
            start = end+1;
            
            String name = getAttribute(param, "name");
            String value = getAttribute(param, "value");
            
            map.put(name, value);
        }
        
        return map;
    }
    
    private Map<String,String> parseElementParameters(Element el) {
//        Map<String,String> map = new HashMap<>();
        Map<String,String> map = new CaseInsensitiveMap();
        for(Node node : el.childNodes()) {
            //System.out.println(node.nodeName());
            if( "param".equals(node.nodeName()) ) {
                //System.out.println(node.attr("name")+"="+ node.attr("value"));
                map.put(node.attr("name"), node.attr("value"));
            }
        }
        return map;
    }
    
    private void createApplet(final ClassLoader cl, final String className, Stub stub) throws Exception {
        
        StartAppInterface sai = new StartAppInterface() {
            @Override
            public boolean execute(Object o) {
                //System.out.println("call to StartAppInterface.execute: "+o);
                if( !ExternalApplications.executeCommand(o.toString()) ) {
                    try {
                        Runtime.getRuntime().exec(o.toString().split(" "));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                
                return true;
            }
        };
        
        try {
            Class<?> cls = cl.loadClass("oracle.forms.demos.Execute");
            Object obj = cls.newInstance();
            Method method = cls.getDeclaredMethod("addListener", new Class[] {StartAppInterface.class});
            method.invoke(obj, sai);
        } catch(Throwable e) {
            e.printStackTrace(System.out);
        }        
        
        Class c = cl.loadClass(className);
        final Applet applet = (Applet)c.newInstance();
        applet.setStub(stub);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFrame frame = new JFrame("AppletRunner - "+className);
                
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosed(WindowEvent e) {
                        Runtime.getRuntime().halt(0);
                    }
                });
                
                //TODO: passar para um ficheiro de configuração
                for(URL url :  ((URLClassLoader)cl).getURLs()) {
                    if( url.toString().toLowerCase().contains("gifs_sam") ) {
                        try {
                            frame.setIconImage(ImageIO.read(new URL("jar:"+url.toString()+"!/logo.gif")));
                        } catch(Exception e) {
                        }
                    }
                    if( url.toString().toLowerCase().contains("sonho_icons") ) {
                        try {
                            frame.setIconImage(ImageIO.read(new URL("jar:"+url.toString()+"!/ico_sonho.gif")));
                        } catch(Exception e) {
                        }
                    }
                }

                frame.setSize(800, 600);
                frame.getContentPane().add(applet);
                frame.setVisible(true);

                applet.init();
                applet.start();
                
                Thread monitorEmptyWindow = new Thread("monitor-empty-window") {
                    @Override
                    public void run() {
                        while( true ) {
                        
                            if( frame.getContentPane().getComponentCount()==0 ||
                                    ((Container)(frame.getContentPane().getComponents()[0])).getComponentCount()==0
                                    ) {

                                System.out.println("JANELA VAZIA!");
                                frame.dispose();
                                
                                Thread exit = new Thread("kill-app") {
                                    @Override
                                    public void run() {
                                        try {Thread.sleep(2000);} catch(Exception e) {}
                                        Runtime.getRuntime().halt(0);
                                    }
                                };
                                exit.setDaemon(true);
                                exit.start();
                            }
                        
                            try {
                                Thread.sleep(1000);
                            } catch(Exception ex) {}
                        }
                        
                    }
                };
                monitorEmptyWindow.setDaemon(true);
                monitorEmptyWindow.start();
            }
        });
        
    }
    
    private void printComponents(Container c) {
        printComponents(c, "");
        
    }
    private void printComponents(Container c, String indent) {
        indent += "   ";
        System.out.println(indent+c.getName()+": "+c.getComponentCount());
        for(Component cc : c.getComponents()) {
            if( cc instanceof Container) {
                printComponents((Container)cc, indent);
            }
        }
    }
    
    private static void setSystemProperties() {
        Properties props = new Properties(System.getProperties());

        props.put("acl.read", "+");
        props.put("acl.read.default", "");
        props.put("acl.write", "+");
        props.put("acl.write.default", "");

        // Standard browser properties
        props.put("browser.version", "1.1");
        props.put("browser.vendor", "Sun Microsystems, Inc.");
        props.put("java.vendor", "Sun Microsystems, Inc.");

        // Set HTTP User-Agent
        props.put("http.agent", "Mozilla/4.0 (" + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")");

        // turn on error stream buffering
        props.put("sun.net.http.errorstream.enableBuffering", "true");

        // Define which packages can NOT be accessed by applets
        props.put("package.restrict.access.sun", "true");
        props.put("package.restrict.access.com.sun.deploy", "true");

        // Define which JSS packages can NOT be accessed by applets
        props.put("package.restrict.access.org.mozilla.jss", "true");

        props.put("package.restrict.access.netscape", "false");

        // Define which packages can NOT be extended by applets
        props.put("package.restrict.definition.java", "true");
        props.put("package.restrict.definition.sun", "true");
        props.put("package.restrict.definition.netscape", "true");
        props.put("package.restrict.definition.com.sun.deploy", "true");

        // Define which JSS packages can NOT be extended by applets
        props.put("package.restrict.definition.org.mozilla.jss", "true");

        props.put("java.version.applet", "true");
        props.put("java.vendor.applet", "true");
        props.put("java.vendor.url.applet", "true");
        props.put("java.class.version.applet", "true");
        props.put("os.name.applet", "true");
        props.put("os.version.applet", "true");
        props.put("os.arch.applet", "true");
        props.put("file.separator.applet", "true");
        props.put("path.separator.applet", "true");
        props.put("line.separator.applet", "true");

        // Install new protocol handler
        String pkgs = (String) props.getProperty("java.protocol.handler.pkgs");
        if (pkgs != null) {
            props.put("java.protocol.handler.pkgs", pkgs + "|sun.plugin.net.protocol|com.sun.deploy.net.protocol");
        } else {
            props.put("java.protocol.handler.pkgs", "sun.plugin.net.protocol|com.sun.deploy.net.protocol");
        }

        // Set allow default user interaction in HTTP/HTTPS
        java.net.URLConnection.setDefaultAllowUserInteraction(true);

        // Set default SSL handshaking protocols to SSLv3 and SSLv2Hello
        // because some servers may not be able to handle TLS. #46268654.
        //
        // Set only if users hasn't set it manually.
        //
//        if (props.get("https.protocols") == null &&
//	    Config.getBooleanProperty(Config.SEC_TLS_KEY) == false) {
//            props.put("https.protocols", "SSLv3,SSLv2Hello");
//	}
        // Remove Proxy Host & Port
        props.remove("proxyHost");
        props.remove("proxyPort");
        props.remove("http.proxyHost");
        props.remove("http.proxyPort");
        props.remove("https.proxyHost");
        props.remove("https.proxyPort");
        props.remove("ftpProxyHost");
        props.remove("ftpProxyPort");
        props.remove("ftpProxySet");
        props.remove("gopherProxyHost");
        props.remove("gopherProxyPort");
        props.remove("gopherProxySet");
        props.remove("socksProxyHost");
        props.remove("socksProxyPort");

        // Add new system property for proxy authentication
        props.put("http.auth.serializeRequests", "true");

        System.setProperties(props);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        URL url = null;

        
        if( args.length>0 ) {
            url = new URL(args[0]);
        }

        if( url==null ) {
            System.out.println("Falta o parâmetro com o URL da página com a applet");
            System.exit(0);
        }
        
        System.out.println("CWD: "+(new File(".").getAbsolutePath()));
        System.out.println("Current JAR: "+Utils.getCurrentCodeSourceLocation());
        Hosts.loadHostsFile();
        HDFFPrintServiceLookup.register();
        ExternalApplications.loadExternalApplications();

        setSystemProperties();

        System.out.println("Vai correr a applet");
        
        AppletRunner appletRunner = new AppletRunner(url);
    }
    
}
