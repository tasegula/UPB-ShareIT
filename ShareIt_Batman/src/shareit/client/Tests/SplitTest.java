/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client.Tests;

/**
 *
 * @author florin
 */
public class SplitTest {
    
    public void splitt(String s) {
        String delim = "\\.";
        System.out.println(s);
        String[] rez = s.split(delim);
        
        for(int i = 0; i < rez.length; i++) {
            System.out.println(rez[i]);
        }
    }
    
    public static void main(String[] args) {
        
        SplitTest t = new SplitTest();
        
        String str = "ana.are.mere";
        t.splitt(str);
    }
}
