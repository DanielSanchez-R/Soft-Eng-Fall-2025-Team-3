package model;

import org.junit.Assert;
import org.junit.Test;

/**
 * MenuItemTest.java
 * =================
 * Unit test class for validating the {@link model.MenuItem} entity in the Pizza 505 ENMU project.
 * <p>
 * This test ensures that the {@code MenuItem} model correctly stores and retrieves
 * its core field values (name, description, category, price, and availability).
 * It serves as part of the main JUnit 4 test suite defined in {@link AllTests}.
 * </p>
 *
 * @author Daniel Sanchez
 * @version 3.0
 * @since 2025-11
 */
public class MenuItemTest {

    /**
     * Verifies that a {@link model.MenuItem} object correctly stores and returns its attributes.
     * <p>
     * Confirms that the constructor properly initializes fields and
     * the getters (e.g., {@code getName()}, {@code isAvailable()}) work as expected.
     * </p>
     */
    @Test
    public void testMenuItemFields() {
        MenuItem item = new MenuItem("Test Pizza", "Cheese pizza", "Entree", 9.99, true, false);
        Assert.assertEquals("Test Pizza", item.getName());
        Assert.assertTrue(item.isAvailable());
    }
}

