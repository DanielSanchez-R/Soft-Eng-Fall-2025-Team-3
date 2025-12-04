package model;

/**
 * TableInfo represents an individual restaurant table within the
 * booking and pricing system. Each table contains identifying information,
 * capacity details, zone location, and pricing attributes used during
 * reservation and billing operations.
 * <p>
 * This class functions as a simple model/POJO, often mapped to a database
 * table named <b>Tables</b> or <b>TableInfo</b> through a DAO layer.
 * It is utilized by staff and admin modules to add, update, or display
 * table information in the restaurant layout and reservation console.
 * </p>
 *
 * <h3>Attributes:</h3>
 * <ul>
 *   <li><b>id</b> — Unique database identifier for the table record.</li>
 *   <li><b>tableNumber</b> — Display label or physical table number (e.g., "A5").</li>
 *   <li><b>capacity</b> — Number of guests the table can seat.</li>
 *   <li><b>zone</b> — Location zone such as "Patio", "VIP", or "Main Hall".</li>
 *   <li><b>basePrice</b> — Standard reservation price for the table.</li>
 *   <li><b>surcharge</b> — Additional charge applied for premium zones or peak times.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <ul>
 *   <li>Used by staff and admin modules for managing table configurations.</li>
 *   <li>Referenced by the reservation subsystem to calculate booking prices.</li>
 *   <li>Displayed in UI layouts showing table availability and pricing tiers.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * TableInfo vipTable = new TableInfo("V3", 4, "VIP", 50.00, 10.00);
 * double total = vipTable.getTotalPrice(); // 60.00
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d1
 * @since 2025-10
 */
public class TableInfo {

    /** Unique identifier for the table (primary key in the database). */
    private int id;

    /** The label or number identifying this table (e.g., "A1", "B5"). */
    private String tableNumber;

    /** The seating capacity of this table (number of guests). */
    private int capacity;

    /** The zone or section where this table is located (e.g., Patio, VIP, Main Hall). */
    private String zone;

    /** The base reservation price for this table. */
    private double basePrice;

    /** Additional charge applied for premium zones or special conditions. */
    private double surcharge;

    /**
     * Default constructor required for frameworks and ORM mapping.
     */
    public TableInfo() {}

    /**
     * Constructs a new TableInfo object with the specified attributes.
     *
     * @param tableNumber  the identifying label or number for the table
     * @param capacity     the number of guests the table can accommodate
     * @param zone         the zone or location of the table
     * @param basePrice    the base reservation price
     * @param surcharge    the additional surcharge for the table
     */
    public TableInfo(String tableNumber, int capacity, String zone, double basePrice, double surcharge) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.zone = zone;
        this.basePrice = basePrice;
        this.surcharge = surcharge;
    }

    /** @return the table’s unique database ID */
    public int getId() { return id; }

    /** @param id sets the unique database ID for this table */
    public void setId(int id) { this.id = id; }

    /** @return the identifying label or table number */
    public String getTableNumber() { return tableNumber; }

    /** @param tableNumber sets the identifying label or number for this table */
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }

    /** @return the number of guests this table can accommodate */
    public int getCapacity() { return capacity; }

    /** @param capacity sets the seating capacity for this table */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /** @return the zone or area this table is located in */
    public String getZone() { return zone; }

    /** @param zone sets the zone or section for this table */
    public void setZone(String zone) { this.zone = zone; }

    /** @return the base reservation price for this table */
    public double getBasePrice() { return basePrice; }

    /** @param basePrice sets the base reservation price for this table */
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    /** @return the additional surcharge for premium zones or conditions */
    public double getSurcharge() { return surcharge; }

    /** @param surcharge sets the additional surcharge for this table */
    public void setSurcharge(double surcharge) { this.surcharge = surcharge; }

    /**
     * Calculates and returns the total price for the table, combining
     * the base price and any applicable surcharge.
     *
     * @return the total price (base + surcharge)
     */
    public double getTotalPrice() { return basePrice + surcharge; }
}
