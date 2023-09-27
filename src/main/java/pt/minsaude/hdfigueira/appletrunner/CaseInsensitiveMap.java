/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pt.minsaude.hdfigueira.appletrunner;

import java.util.HashMap;

/**
 *
 * @author paulo
 */
public class CaseInsensitiveMap<K, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        if (key instanceof String) {
            String keyStr = (String)key;
            return super.put((K)keyStr.toLowerCase(), value);
        }
        return super.put(key, value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            String keyStr = (String)key;
            return super.get(keyStr.toLowerCase());
        }
        return super.get(key);
    }
}
