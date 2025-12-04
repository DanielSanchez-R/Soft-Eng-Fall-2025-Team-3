package model;

/**
 * Represents the total revenue for a specific month and year.
 * @author Daniel Sanchez
 * @version 4.0
 */
public class MonthlyRevenue {
    private int month;
    private int year;
    private double revenue;

    /**
     * Default constructor.
     */
    public MonthlyRevenue() {}

    /**
     * Constructs a MonthlyRevenue record with all fields set.
     *
     * @param month   the month of the record (1–12)
     * @param year    the year of the record
     * @param revenue the total revenue for that month
     */
    public MonthlyRevenue(int month, int year, double revenue) {
        this.month = month;
        this.year = year;
        this.revenue = revenue;
    }

    /**
     * @return the month of the revenue record
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month.
     *
     * @param month the month to assign
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the year of the revenue record
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param year the year to assign
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the revenue amount for the given month and year
     */
    public double getRevenue() {
        return revenue;
    }

    /**
     * Sets the revenue amount.
     *
     * @param revenue the revenue value to assign
     */
    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}

