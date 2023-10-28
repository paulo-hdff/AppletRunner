/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author paulo
 */
public class ExternalApplicationsTests {
    
    
    public static void main(String[] args) throws Exception {
    
        testTransformUrl();
        
    }


    public static void testTransformUrl() {
        ExternalApplications.loadExternalApplications();
        
        String strUrl = "http://127.0.0.1/bla?a=b&x=#rnd:static:26:an#&y=abc";
//        ExternalApplications.transformUrl(strUrl);
//        strUrl = "http://127.0.0.1/bla?a=b&x=#rnd:static:26:an#";
//        ExternalApplications.transformUrl(strUrl);
//        strUrl = "http://127.0.0.1/bla?a=b&x=#rnd:static-x:26:an#&y=abc&z=#rnd:global-x:26:an#&w=aa";
//        ExternalApplications.transformUrl(strUrl);
//        ExternalApplications.transformUrl(strUrl);
//        strUrl = "http://127.0.0.1/ble?a=b&x=#rnd:static-x:26:an#&y=abc&z=#rnd:global-x:26:an#&w=aa";
//        ExternalApplications.transformUrl(strUrl);
//        strUrl = "http://127.0.0.1/ble?a=b&x=#rnd:5#";
//        ExternalApplications.transformUrl(strUrl);
        strUrl = "http://192.168.1.231/doctors/gfapp_call_popup.php?a=1";
        ExternalApplications.executeLink(strUrl);
    }
}
