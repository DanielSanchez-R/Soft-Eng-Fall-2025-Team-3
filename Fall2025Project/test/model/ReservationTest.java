package model;

import org.junit.Test;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link Reservation} model class.
 * Verifies that constructors, getters, and setters behave correctly.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class ReservationTest {

    /**
     * Tests the no-argument constructor and all setter methods.
     */
    @Test
    public void testNoArgConstructorAndSetters() {
        Reservation r = new Reservation();

        LocalDateTime dateTime = LocalDateTime.of(2025, 10, 25, 19, 30);
        LocalDateTime createdAt = LocalDateTime.of(2025, 10, 20, 12, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2025, 10, 21, 13, 15);

        r.setId(1);
        r.setCustomerName("Alice Johnson");
        r.setContact("575-555-8899");
        r.setTableId(4);
        r.setDateTime(dateTime);
        r.setPartySize(4);
        r.setStatus("confirmed");

        r.setReferenceId("REF-123456");
        r.setNotes("Birthday party, bring cake.");
        r.setCustomerId(42);
        r.setCreatedAt(createdAt);
        r.setModifiedAt(modifiedAt);

        assertEquals(1, r.getId());
        assertEquals("Alice Johnson", r.getCustomerName());
        assertEquals("575-555-8899", r.getContact());
        assertEquals(4, r.getTableId());
        assertEquals(dateTime, r.getDateTime());
        assertEquals(4, r.getPartySize());
        assertEquals("confirmed", r.getStatus());

        assertEquals("REF-123456", r.getReferenceId());
        assertEquals("Birthday party, bring cake.", r.getNotes());
        assertEquals(Integer.valueOf(42), r.getCustomerId());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(modifiedAt, r.getModifiedAt());
    }

    /**
     * Tests the full constructor that initializes every field.
     */
    @Test
    public void testAllArgsConstructor() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 5, 18, 0);
        LocalDateTime createdAt = LocalDateTime.of(2025, 11, 1, 9, 30);
        LocalDateTime modifiedAt = LocalDateTime.of(2025, 11, 2, 10, 45);

        Reservation r = new Reservation(
                2,
                "Bob Smith",
                "bob@example.com",
                7,
                dateTime,
                2,
                "cancelled",
                "REF-ABC-999",
                "Window seat requested.",
                99,
                createdAt,
                modifiedAt
        );

        assertEquals(2, r.getId());
        assertEquals("Bob Smith", r.getCustomerName());
        assertEquals("bob@example.com", r.getContact());
        assertEquals(7, r.getTableId());
        assertEquals(dateTime, r.getDateTime());
        assertEquals(2, r.getPartySize());
        assertEquals("cancelled", r.getStatus());

        assertEquals("REF-ABC-999", r.getReferenceId());
        assertEquals("Window seat requested.", r.getNotes());
        assertEquals(Integer.valueOf(99), r.getCustomerId());
        assertEquals(createdAt, r.getCreatedAt());
        assertEquals(modifiedAt, r.getModifiedAt());
    }

    /**
     * Tests that nullable fields (notes, customerId) are allowed.
     */
    @Test
    public void testNullableCustomerIdAndNotesAllowed() {
        LocalDateTime now = LocalDateTime.now();

        Reservation r = new Reservation(
                3,
                "Walk-in Guest",
                "no-contact",
                3,
                now,
                2,
                "confirmed",
                "REF-WALKIN",
                null,
                null,
                now,
                now
        );

        assertNull("Customer ID should be nullable for guests", r.getCustomerId());
        assertNull("Notes can be nullable", r.getNotes());
        assertEquals("REF-WALKIN", r.getReferenceId());
    }
}

