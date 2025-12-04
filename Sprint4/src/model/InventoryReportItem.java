package model;

/**
 * InventoryReportItem
 * Represents a menu item and its stock level for the inventory report.
 */
public class InventoryReportItem {
    private int itemId;
    private String name;
    private int stock;
    private boolean lowStock;

    public InventoryReportItem() {}

    public InventoryReportItem(int itemId, String name, int stock, boolean lowStock) {
        this.itemId = itemId;
        this.name = name;
        this.stock = stock;
        this.lowStock = lowStock;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isLowStock() {
        return lowStock;
    }

    public void setLowStock(boolean lowStock) {
        this.lowStock = lowStock;
    }
}
