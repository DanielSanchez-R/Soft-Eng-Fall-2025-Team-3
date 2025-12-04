package model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the {@link InventoryReportItem} model.
 * Ensures that constructor values are stored and retrieved correctly.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class InventoryReportItemTest {

    /**
     * Tests the InventoryReportItem constructor and getters.
     */
    @Test
    public void testInventoryItem() {
        InventoryReportItem item = new InventoryReportItem(1, "Cheese", 5, true);

        assertEquals("Cheese", item.getName());
        assertEquals(5, item.getStock());
        assertTrue(item.isLowStock());
    }
}

