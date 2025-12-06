package model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link User} model class.
 * Ensures that getter and setter methods function correctly.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class UserTest {

    /**
     * Tests setting and retrieving all User fields.
     */
    @Test
    public void testUserFields() {
        User u = new User();
        u.setId(1);
        u.setName("Test User");
        u.setEmail("test@test.com");
        u.setPassword("pass");

        assertEquals(1, u.getId());
        assertEquals("Test User", u.getName());
        assertEquals("test@test.com", u.getEmail());
        assertEquals("pass", u.getPassword());
    }
}
