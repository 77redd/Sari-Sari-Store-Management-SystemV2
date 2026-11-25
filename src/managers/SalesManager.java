package managers;

import models.Expense;
import models.Payment;
import models.Product;
import models.SaleItem;
import models.Sales;
import util.InsufficientStockException;
import util.InvalidInputException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records purchases, processes payments, generates receipts and records expenses. */
public class SalesManager {
    private final InventoryManager inventory;
    private final List<Sales> sales = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private int saleCounter = 1;
    private int expenseCounter = 1;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SalesManager(InventoryManager inventory) {
        this.inventory = inventory;
    }

    // ---------- Loading saved records (FileHandler) ----------
    public void loadSales(List<Sales> saved) {
        sales.clear();
        sales.addAll(saved);
        for (Sales s : saved) saleCounter = Math.max(saleCounter, idNumber(s.getSaleId()) + 1);
    }

    public void loadExpenses(List<Expense> saved) {
        expenses.clear();
        expenses.addAll(saved);
        for (Expense e : saved) expenseCounter = Math.max(expenseCounter, idNumber(e.getExpenseId()) + 1);
    }

    private int idNumber(String id) {
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 0;
        }
    }

    // ---------- Sales ----------
    /** Starts a cart. It is only recorded once paid, so cancelled carts leave no trace. */
    public Sales startSale(String customerName) {
        return new Sales(String.format("S%05d", saleCounter++), customerName);
    }

    /** Adds a product to the cart, checking stock against what is already in the cart. */
    public SaleItem addItem(Sales sale, String productId, int qty)
            throws InvalidInputException, InsufficientStockException {
        if (qty <= 0) throw new InvalidInputException("Quantity must be greater than zero.");
        Product p = inventory.getProduct(productId);
        int wanted = sale.getQuantityOf(p.getProductId()) + qty;
        if (wanted > p.getStock())
            throw new InsufficientStockException("Insufficient stock for " + p.getName()
                    + ". Available: " + p.getStock() + ", in cart after adding: " + wanted);
        SaleItem item = new SaleItem(p.getProductId(), p.getName(),
                BigDecimal.valueOf(p.getSellingPrice()), BigDecimal.valueOf(p.getBuyingPrice()), qty);
        sale.addItem(item);
        return item;
    }

    /**
     * Processes payment, deducts stock and records the sale. Returns the change due.
     * Nothing is changed if stock or the tendered amount is insufficient.
     */
    public BigDecimal processPayment(Sales sale, Payment.Method method, BigDecimal tendered)
            throws InvalidInputException, InsufficientStockException {
        if (sale.getItems().isEmpty()) throw new InvalidInputException("Cart is empty.");
        for (SaleItem i : sale.getItems()) {
            Product p = inventory.getProduct(i.getProductId());
            if (i.getQuantity() > p.getStock())
                throw new InsufficientStockException("Insufficient stock for " + p.getName());
        }
        Payment payment;
        try {
            payment = new Payment(method, sale.getTotal(), tendered);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
        for (SaleItem i : sale.getItems()) inventory.deductStock(i.getProductId(), i.getQuantity());
        sale.setPayment(payment);
        sales.add(sale);
        return payment.getChange();
    }

    public String generateReceipt(Sales sale) {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(46);
        sb.append(line).append('\n');
        sb.append("                 SALES RECEIPT\n");
        sb.append(line).append('\n');
        sb.append("Receipt #: ").append(sale.getSaleId()).append('\n');
        sb.append("Date     : ").append(sale.getDateTime().format(FMT)).append('\n');
        sb.append("Customer : ").append(sale.getCustomerName()).append('\n');
        sb.append("-".repeat(46)).append('\n');
        sb.append(String.format("%-18s %4s %10s %10s%n", "Item", "Qty", "Price", "Subtotal"));
        for (SaleItem i : sale.getItems()) {
            sb.append(String.format("%-18s %4d %10.2f %10.2f%n",
                    truncate(i.getProductName(), 18), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()));
        }
        sb.append("-".repeat(46)).append('\n');
        sb.append(String.format("%-30s %15.2f%n", "TOTAL:", sale.getTotal()));
        if (sale.isPaid()) {
            Payment p = sale.getPayment();
            sb.append(String.format("%-30s %15s%n", "Payment method:", p.getMethod()));
            sb.append(String.format("%-30s %15.2f%n", "Amount tendered:", p.getAmountTendered()));
            sb.append(String.format("%-30s %15.2f%n", "Change:", p.getChange()));
        } else {
            sb.append("*** UNPAID ***\n");
        }
        sb.append(line).append('\n');
        sb.append("          Thank you for your purchase!\n");
        return sb.toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    /** Only completed (paid) sales. */
    public List<Sales> getPaidSales() {
        List<Sales> paid = new ArrayList<>();
        for (Sales s : sales) if (s.isPaid()) paid.add(s);
        return Collections.unmodifiableList(paid);
    }

    public List<Sales> getAllSales() { return Collections.unmodifiableList(sales); }

    // ---------- Expenses ----------
    public Expense recordExpense(String description, String category, BigDecimal amount, LocalDate date)
            throws InvalidInputException {
        if (description == null || description.isBlank()) throw new InvalidInputException("Description cannot be empty.");
        if (amount.signum() <= 0) throw new InvalidInputException("Expense amount must be positive.");
        Expense e = new Expense(String.format("E%05d", expenseCounter++), description.trim(),
                (category == null || category.isBlank()) ? "General" : category.trim(), amount, date);
        expenses.add(e);
        return e;
    }

    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }
}
