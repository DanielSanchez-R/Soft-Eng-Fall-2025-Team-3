package model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the {@link Customer} model class.
 * Verifies that getter and setter methods work properly.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class CustomerTest {

    /**
     * Tests setting and retrieving all Customer fields.
     */
    @Test
    public void testCustomerFields() {
        Customer c = new Customer();
        c.setId(10);
        c.setName("John Doe");
        c.setEmail("john@test.com");
        c.setPassword("abc123");
        c.setPhone("555-9999");

        assertEquals(10, c.getId());
        assertEquals("John Doe", c.getName());
        assertEquals("john@test.com", c.getEmail());
        assertEquals("abc123", c.getPassword());
        assertEquals("555-9999", c.getPhone());
    }
}

