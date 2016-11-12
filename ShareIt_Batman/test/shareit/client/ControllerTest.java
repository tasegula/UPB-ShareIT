/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import shareit.FileRequest;

/**
 *
 * @author andrei
 */
public class ControllerTest {
    
    @Test
    public void testGetLogItems() {
        FileRequest f = new FileRequest("file", "deLaMine", "pentruTine");
        Controller.getInstance().clearLog();
        Controller.getInstance().saveLogItem(f);
        Controller.getInstance().saveLogItem(f);
        Controller.getInstance().saveLogItem(f);
        ArrayList<Object> log = Controller.getInstance().getLogItems();
        assertEquals(3, log.size());
        assertEquals("file", ((FileRequest)log.get(1)).filename);
    }
    
}
