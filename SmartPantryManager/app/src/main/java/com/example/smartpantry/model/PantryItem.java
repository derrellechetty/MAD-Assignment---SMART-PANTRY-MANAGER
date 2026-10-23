package com.example.smartpantry.model;

/**
 * One ingredient the user currently has at home.
 * Plain Java (no Android imports) so the matching logic can be unit tested on the JVM.
 */
public class PantryItem {

    private long id;              // database primary key (-1 = not saved yet)
    private String name;
    private double quantity;
    private String unit;
    private long expiryMillis;    // 0 = no expiry date

    public PantryItem(long id, String name, double quantity, String unit, long expiryMillis) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryMillis = expiryMillis;
    }

    /** Convenience constructor for a brand-new item. */
    public PantryItem(String name, double quantity, String unit, long expiryMillis) {
        this(-1, name, quantity, unit, expiryMillis);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public long getExpiryMillis() { return expiryMillis; }
    public void setExpiryMillis(long expiryMillis) { this.expiryMillis = expiryMillis; }

    public boolean hasExpiry() { return expiryMillis > 0; }
}
