package model;

import java.time.LocalDateTime;

/**
 * Reservation represents a table booking made by a customer in the
 * restaurant booking and ordering system. It stores all relevant details
 * about the reservation, including customer information, table assignment,
 * date and time, party size, and current booking status.
 * <p>
 * This model class serves as the data backbone for reservation management
 * features across customer, staff, and admin modules. It can be persisted
 * in a relational database and is typically handled via a DAO layer.
 * </p>
 *
 * <h3>Typical Status Values:</h3>
 * <ul>
 *   <li><b>confirmed</b> — Reservation successfully created and scheduled.</li>
 *   <li><b>cancelled</b> — Reservation cancelled by customer or staff.</li>
 *   <li><b>no-show</b> — Customer did not arrive for their booking.</li>
 *   <li><b>completed</b> — Reservation fulfilled and closed (optional future use).</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <ul>
 *   <li>Created when a customer reserves a table through the UI.</li>
 *   <li>Modified by staff when marking as seated, cancelled, or no-show.</li>
 *   <li>Queried by admin for reporting and analytics.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * Reservation r = new Reservation(
 *     1, "Alice Johnson", "575-555-8899", 4,
 *     LocalDateTime.of(2025, 10, 25, 19, 30),
 *     4, "confirmed"
 * );
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class Reservation {

    /** Unique identifier for the reservation (primary key in the database). */
    private int id;

    /** Full name of the customer who made the reservation. */
    private String customerName;

    /** Contact information for the customer (phone or email). */
    private String contact;

    /** The ID of the table assigned to this reservation. */
    private int tableId;

    /** Date and time of the scheduled reservation. */
    private LocalDateTime dateTime;

    /** The number of guests included in the reservation. */
    private int partySize;

    /** Current status of the reservation (confirmed, cancelled, no-show). */
    private String status;

    /**
     * Default no-argument constructor.
     * <p>
     * Required for frameworks or ORM tools that use reflection or serialization.
     * </p>
     */
    public Reservation() {}

    /**
     * Constructs a {@code Reservation} object with all details specified.
     *
     * @param id            unique reservation ID
     * @param customerName  the name of the customer making the reservation
     * @param contact       customer contact info (phone/email)
     * @param tableId       ID of the table being reserved
     * @param dateTime      date and time of the reservation
     * @param partySize     number of guests in the reservation
     * @param status        current status (confirmed, cancelled, no-show)
     */
    public Reservation(int id, String customerName, String contact, int tableId,
                       LocalDateTime dateTime, int partySize, String status) {
        this.id = id;
        this.customerName = customerName;
        this.contact = contact;
        this.tableId = tableId;
        this.dateTime = dateTime;
        this.partySize = partySize;
        this.status = status;
    }

    /** @return the unique reservation ID */
    public int getId() { return id; }

    /** @param id sets the unique reservation ID */
    public void setId(int id) { this.id = id; }

    /** @return the name of the customer for this reservation */
    public String getCustomerName() { return customerName; }

    /** @param customerName sets the customer’s name */
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /** @return the contact info (phone/email) associated with this reservation */
    public String getContact() { return contact; }

    /** @param contact sets the customer’s contact information */
    public void setContact(String contact) { this.contact = contact; }

    /** @return the ID of the table assigned to this reservation */
    public int getTableId() { return tableId; }

    /** @param tableId sets the table ID for this reservation */
    public void setTableId(int tableId) { this.tableId = tableId; }

    /** @return the scheduled date and time of the reservation */
    public LocalDateTime getDateTime() { return dateTime; }

    /** @param dateTime sets the reservation’s date and time */
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    /** @return the number of guests for this reservation */
    public int getPartySize() { return partySize; }

    /** @param partySize sets the number of guests for this reservation */
    public void setPartySize(int partySize) { this.partySize = partySize; }

    /** @return the current status of the reservation (confirmed, cancelled, etc.) */
    public String getStatus() { return status; }

    /** @param status sets the current reservation status */
    public void setStatus(String status) { this.status = status; }
}
