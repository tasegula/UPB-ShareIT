/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author andrei
 */
public class ValidationsTest {

    public ValidationsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of validateUsername method, of class Validations.
     */
    @Test
    public void testValidateUsername() {
        Assert.assertNull(Validations.validateUsername("1UsernameValid"));
        Assert.assertNotNull(Validations.validateUsername("1 Username Invalid"));
        Assert.assertNotNull(Validations.validateUsername(""));
        Assert.assertNotNull(Validations.validateUsername(null));
    }

    /**
     * Test of validatePassword method, of class Validations.
     */
    @Test
    public void testValidatePassword() {
        Assert.assertNotNull(Validations.validatePassword(""));
        Assert.assertNotNull(Validations.validatePassword(null));
        Assert.assertNull(Validations.validatePassword("O parol@ tare"));
    }

}
