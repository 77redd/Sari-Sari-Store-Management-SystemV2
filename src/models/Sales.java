package models;

import util.FileHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A customer transaction made up of items and (eventually) a payment. */
public class Sales implements Payable {
    private final String saleId;
    private String customerName;
    private final LocalDateTime dateTime;
    private final List<SaleItem> items = new ArrayList<>();
    private Payment payment;

    public Sales(String saleId, String customerName) {
        this(saleId, customerName, LocalDateTime.now());
    }

    // Used when loading a saved sale so its original date is kept
    public Sales(String saleId, String customerName, LocalDateTime dateTime) {
        this.saleId = saleId;
        setCustomerName(customerName);
        this.dateTime = dateTime;
    }

    public void addItem(SaleItem item) {
        ensureNotPaid();
        for (SaleItem existing : items) {
            if (existing.getProductId().equals(item.getProductId())) {
                existing.addQuantity(item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public boolean removeItem(String productId) {
        ensureNotPaid();
        return items.removeIf(i -> i.getProductId().equalsIgnoreCase(productId));
    }

    /** Quantity of a product already in this sale (0 if none). */
    public int getQuantityOf(String productId) {
        for (SaleItem i : items) if (i.getProductId().equals(productId)) return i.getQuantity();
        return 0;
    }

    @Override
    public BigDecimal calculateTotal() {
        return items.stream().map(SaleItem::calculateTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() { return calculateTotal(); }

    public BigDecimal getTotalCost() {
        return items.stream().map(SaleItem::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getProfit() { return getTotal().subtract(getTotalCost()); }

    public void setPayment(Payment payment) {
        ensureNotPaid();
        if (items.isEmpty()) throw new IllegalStateException("Cannot pay for an empty sale.");
        this.payment = payment;
    }

    private void ensureNotPaid() {
        if (payment != null) throw new IllegalStateException("Sale " + saleId + " is already paid.");
    }

    public void setCustomerName(String customerName) {
        this.customerName = (customerName == null || customerName.isBlank()) ? "Walk-in" : customerName.trim();
    }

    public boolean isPaid()               { return payment != null; }
    public String getSaleId()             { return saleId; }
    public String getCustomerName()       { return customerName; }
    public LocalDateTime getDateTime()    { return dateTime; }
    public Payment getPayment()           { return payment; }
    public List<SaleItem> getItems()      { return Collections.unmodifiableList(items); }

    // Only paid sales are saved. CSV: saleId,customerName,dateTime,payment,item|item|...
    public String toCSV() {
        StringBuilder itemsField = new StringBuilder();
        for (SaleItem i : items) {
            if (itemsField.length() > 0) itemsField.append('|');
            itemsField.append(i.toCSV());
        }
        return String.join(",", saleId, FileHandler.clean(customerName), dateTime.toString(),
                payment.toCSV(), itemsField.toString());
    }

    public static Sales fromCSV(String line) {
        String[] p = line.split(",", -1);
        Sales sale = new Sales(p[0], p[1], LocalDateTime.parse(p[2]));
        for (String item : p[4].split("\\|")) sale.addItem(SaleItem.fromCSV(item));
        sale.setPayment(Payment.fromCSV(p[3]));
        return sale;
    }
}
