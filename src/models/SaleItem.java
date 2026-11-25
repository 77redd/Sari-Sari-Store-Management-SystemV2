package models;

import util.FileHandler;

import java.math.BigDecimal;

/** A single line item in a sale. */
public class SaleItem implements Payable {
    private final String productId;
    private final String productName;
    private final BigDecimal unitPrice;   // selling price
    private final BigDecimal unitCost;    // cost to the business (for profit)
    private int quantity;

    public SaleItem(String productId, String productName,
                    BigDecimal unitPrice, BigDecimal unitCost, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (unitPrice.signum() < 0 || unitCost.signum() < 0)
            throw new IllegalArgumentException("Price and cost cannot be negative.");
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.quantity = quantity;
    }

    public String getProductId()      { return productId; }
    public String getProductName()    { return productName; }
    public BigDecimal getUnitPrice()  { return unitPrice; }
    public BigDecimal getUnitCost()   { return unitCost; }
    public int getQuantity()          { return quantity; }

    public void addQuantity(int extra) {
        if (extra <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        quantity += extra;
    }

    public BigDecimal getSubtotal()   { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public BigDecimal getTotalCost()  { return unitCost.multiply(BigDecimal.valueOf(quantity)); }
    public BigDecimal getProfit()     { return getSubtotal().subtract(getTotalCost()); }

    @Override
    public BigDecimal calculateTotal() { return getSubtotal(); }

    // Nested inside a sales.csv row, so fields use ';'. Format: productId;name;unitPrice;unitCost;quantity
    public String toCSV() {
        return String.join(";", productId, FileHandler.clean(productName),
                unitPrice.toPlainString(), unitCost.toPlainString(), String.valueOf(quantity));
    }

    public static SaleItem fromCSV(String field) {
        String[] p = field.split(";", -1);
        return new SaleItem(p[0], p[1], new BigDecimal(p[2]), new BigDecimal(p[3]), Integer.parseInt(p[4]));
    }
}
