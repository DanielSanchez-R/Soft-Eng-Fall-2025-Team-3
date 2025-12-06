package model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * MenuItemTest
 * ------------
 * Unit tests for the {@link model.MenuItem} domain class.
 *
 * Focuses on:
 * <ul>
 *     <li>Correct behaviour of getters and setters;</li>
 *     <li>Price being stored as a double with expected precision;</li>
 *     <li>Customization fields (size, toppings) used for the ordering flow.</li>
 * </ul>
 * @author Daniel Sanchez
 * @version 4.0
 */
public class MenuItemTest {

    /**
     * Verifies that all core fields can be set and retrieved correctly.
     */
    @Test
    public void testCoreGettersAndSetters() {
        MenuItem item = new MenuItem();

        item.setId(42);
        item.setName("Test Pizza");
        item.setDescription("Unit-test special");
        item.setCategory("Entree");
        item.setPrice(9.99);
        item.setAvailable(true);
        item.setDraft(false);

        assertEquals(42, item.getId());
        assertEquals("Test Pizza", item.getName());
        assertEquals("Unit-test special", item.getDescription());
        assertEquals("Entree", item.getCategory());
        assertEquals(9.99, item.getPrice(), 0.0001);
        assertTrue(item.isAvailable());
        assertFalse(item.isDraft());
    }

    /**
     * Verifies that customization-related properties are stored and retrieved.
     * These are used heavily by the OrderController for Option B pricing.
     */
    @Test
    public void testSizeAndToppingsCustomization() {
        MenuItem item = new MenuItem();

        item.setName("Custom Pizza");
        item.setSize("large");
        item.setToppings(Arrays.asList("pepperoni", "mushrooms"));

        assertEquals("large", item.getSize());

        List<String> toppings = item.getToppings();
        assertNotNull("Toppings list should not be null", toppings);
        assertEquals(2, toppings.size());
        assertTrue(toppings.contains("pepperoni"));
        assertTrue(toppings.contains("mushrooms"));
    }

    /**
     * Simple sanity check: new MenuItem instances should not accidentally
     * start in an "unavailable draft" state without being configured.
     *
     * (We only assert the behaviour that is safe and intentional to rely on.)
     */
    @Test
    public void testNewMenuItemHasSafeDefaultFlags() {
        MenuItem item = new MenuItem();

        // It's reasonable to expect new items are not drafts by default.
        assertFalse("New MenuItem should not be a draft by default", item.isDraft());
    }
}


