package model;

import java.util.ArrayList;
import java.util.List;

/**
 * MenuItem represents a single food or drink item available in the
 * restaurant’s digital menu. Each menu item includes descriptive details,
 * pricing information, and availability status to ensure accurate, real-time
 * menu presentation for both staff and customers.
 * <p>
 * This class is used across multiple modules:
 * <ul>
 *   <li><b>Admin/Staff Module:</b> For creating, updating, and managing menu items.</li>
 *   <li><b>Customer Module:</b> For browsing available menu items and placing orders.</li>
 *   <li><b>Reporting Module:</b> For generating sales or inventory summaries.</li>
 * </ul>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Supports draft and publish modes for menu editing workflows.</li>
 *   <li>Includes an availability toggle for quickly marking items as out-of-stock.</li>
 *   <li>Organized by category (e.g., appetizer, entrée, dessert, beverage).</li>
 *   <li>Supports optional per-order customization fields (size, toppings) when used in the cart.</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * MenuItem pizza = new MenuItem(
 *     "Margherita Pizza",
 *     "Classic tomato sauce, mozzarella, and fresh basil",
 *     "Entrée",
 *     12.99,
 *     true,
 *     false
 * );
 * </pre>
 *
 * @author Daniel Sanchez
 * @version 1.d3
 * @since 2025-10
 */
public class MenuItem {

    /** Unique identifier for the menu item (primary key in the database). */
    private int id;

    /** Name of the menu item (e.g., "Cheeseburger", "Caesar Salad"). */
    private String name;

    /** Short description of the menu item’s ingredients or details. */
    private String description;

    /** Category this menu item belongs to (e.g., appetizer, entrée, dessert). */
    private String category;

    /** Current price of the menu item. */
    private double price;

    /** Indicates whether the item is currently available to customers. */
    private boolean available;

    /** Indicates whether the item is in draft mode (not yet published to customers). */
    private boolean draft;

    /**
     * Optional per-order size customization (e.g., "small", "medium", "large").
     * <p>
     * This field is not persisted in the menu database; it is intended to
     * represent the customer’s chosen size when the item is placed in the cart.
     * </p>
     */
    private String size;

    /**
     * Optional list of per-order toppings or modifiers (e.g., "pepperoni", "mushrooms").
     * <p>
     * Like {@link #size}, this field is only used for the customer’s cart view
     * and is not part of the core menu record stored in the database.
     * </p>
     */
    private List<String> toppings;

    /**
     * Default no-argument constructor.
     * <p>
     * Required for frameworks, reflection-based tools, and data mappers.
     * Initializes {@link #toppings} as an empty list to avoid {@code null} checks.
     * </p>
     */
    public MenuItem() {
        this.toppings = new ArrayList<String>();
    }

    /**
     * Constructs a {@code MenuItem} with all details specified.
     *
     * @param name         the name of the menu item
     * @param description  a short description of the item
     * @param category     the category (appetizer, entrée, dessert, etc.)
     * @param price        the item’s price
     * @param available    whether the item is available for order
     * @param draft        whether the item is in draft mode (hidden from public view)
     */
    public MenuItem(String name, String description, String category,
                    double price, boolean available, boolean draft) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.available = available;
        this.draft = draft;
        this.toppings = new ArrayList<String>();
    }

    /** @return the unique database ID of this menu item */
    public int getId() { return id; }

    /** @param id sets the unique database ID for this menu item */
    public void setId(int id) { this.id = id; }

    /** @return the name of the menu item */
    public String getName() { return name; }

    /** @param name sets the name of the menu item */
    public void setName(String name) { this.name = name; }

    /** @return a short description of the menu item */
    public String getDescription() { return description; }

    /** @param description sets the description of the menu item */
    public void setDescription(String description) { this.description = description; }

    /** @return the category of the menu item (e.g., entrée, dessert) */
    public String getCategory() { return category; }

    /** @param category sets the menu item’s category */
    public void setCategory(String category) { this.category = category; }

    /** @return the current price of the menu item */
    public double getPrice() { return price; }

    /** @param price sets the menu item’s price */
    public void setPrice(double price) { this.price = price; }

    /** @return true if the item is currently available, otherwise false */
    public boolean isAvailable() { return available; }

    /** @param available sets the item’s availability status */
    public void setAvailable(boolean available) { this.available = available; }

    /** @return true if the item is in draft mode (not published), otherwise false */
    public boolean isDraft() { return draft; }

    /** @param draft sets the draft status for this menu item */
    public void setDraft(boolean draft) { this.draft = draft; }

    /**
     * Returns the per-order size customization for this menu item.
     *
     * @return the selected size (e.g., "small", "medium", "large"),
     *         or {@code null} if no size was specified
     */
    public String getSize() { return size; }

    /**
     * Sets the per-order size customization for this menu item.
     *
     * @param size the size chosen by the customer for this item
     */
    public void setSize(String size) { this.size = size; }

    /**
     * Returns the list of toppings or modifiers associated with this item in the cart.
     *
     * @return a mutable list of topping names; never {@code null} but may be empty
     */
    public List<String> getToppings() { return toppings; }

    /**
     * Sets the list of toppings or modifiers for this item.
     *
     * @param toppings a list of topping names selected by the customer;
     *                 may be {@code null}, in which case an empty list is used
     */
    public void setToppings(List<String> toppings) {
        if (toppings == null) {
            this.toppings = new ArrayList<String>();
        } else {
            this.toppings = toppings;
        }
    }
}

