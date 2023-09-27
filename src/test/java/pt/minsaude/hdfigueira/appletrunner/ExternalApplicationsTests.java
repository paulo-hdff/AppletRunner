/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

/**
 *
 * @author paulo
 */
public class ExternalApplicationsTests {
    
    
    public static void main(String[] args) throws Exception {
    
        ExternalApplications.loadExternalApplications();
        ExternalApplications.executeLink("http://192.168.1.58/pls/sam/SAM.acesso_novo.acesso_sam?sessionid=8082883732023090766435");
    }
    
}
