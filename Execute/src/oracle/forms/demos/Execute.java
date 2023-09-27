/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package oracle.forms.demos;

import java.util.ArrayList;
import java.util.List;
import oracle.forms.properties.ID;
import oracle.forms.ui.VBean;


public class Execute extends VBean {

    private static final ID STARTAPP;
    
    //private static String search = "c:\\bm\\showchrome.exe ";
    private static final List<StartAppInterface> listeners = new ArrayList<>();
    
    public static void addListener(StartAppInterface l) {
        if( !listeners.contains(l) ) {
            listeners.add(l);
        }
    }
    
    public static void removeListener(StartAppInterface l) {
        listeners.remove(l);
    }
    
    @Override
    public boolean setProperty(final ID id, final Object value) {
        try {
            if (id == Execute.STARTAPP) {
                //System.out.println("running "+value.toString());
//                if( value!=null ) {
//                    String strValue = value.toString();
//                    if( strValue!=null && strValue.toLowerCase().startsWith(search) ) {
//                        //new Context().showDocument(new URL(value.toString().substring(search.length())));
//                        return true;
//                    }
//                }
//                Runtime.getRuntime().exec(value.toString());
//                return true;
                for(StartAppInterface l : listeners) {
                    if( l.execute(value) ) {
                        return true;
                    }
                }
            }
            return super.setProperty(id, value);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    static {
        STARTAPP = ID.registerProperty("startAPP");
    }

}
