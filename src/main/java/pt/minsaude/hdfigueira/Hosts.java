/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira;

import com.alibaba.dcm.DnsCacheEntry;
import com.alibaba.dcm.DnsCacheManipulator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author paulo
 */
public class Hosts {

    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            //logger.severe("Unable to compile pattern", e);
        }
    }

    public static void loadHostsFile() {

        File hostsFile = new File("hosts");
        if (!hostsFile.exists()) {
            File path = getPath();
            if (path != null) {
                hostsFile = new File(path, "hosts");
            }
        }

        if (hostsFile.exists()) {
            System.out.println("loading hosts from " + hostsFile.getAbsolutePath());

            try {
                BufferedReader br = new BufferedReader(new FileReader(hostsFile));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1 && isIpAddress(parts[0]) ) {
                        for(int i=1; i<parts.length; i++) {
                            DnsCacheManipulator.setDnsCache(parts[i], parts[0]);
                        }
                    }
                }

                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //System.out.println(DnsCacheManipulator.listDnsCache());
        //System.out.println();
        printCache();
    }

    protected static File getPath() {
        try {
            File path = null;
            URL url = Hosts.class.getProtectionDomain().getCodeSource().getLocation();
            
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

    protected static boolean isIpAddress(String ipAddress) {

        Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
        if (m1.matches()) {
            return true;
        }
        Matcher m2 = VALID_IPV6_PATTERN.matcher(ipAddress);
        return m2.matches();
    }
    
    protected static void printCache() {
        for(DnsCacheEntry entry : DnsCacheManipulator.listDnsCache()) {
            System.out.println("   "+entry.getHost()+" "+Arrays.asList(entry.getIps()));
        }
        System.out.println();
    }
}
