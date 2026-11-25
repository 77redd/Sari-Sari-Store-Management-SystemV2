import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import managers.CustomerManager;
import managers.InventoryManager;
import managers.SalesManager;
import managers.SalesReportManager;
import managers.UserManager;
import models.Cashier;
import models.Customer;
import models.Expense;
import models.Payment;
import models.Product;
import models.SaleItem;
import models.Sales;
import models.User;
import util.FileHandler;
import util.InsufficientStockException;
import util.InvalidInputException;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static final InventoryManager inventoryManager = new InventoryManager();
    private static final SalesManager salesManager = new SalesManager(inventoryManager);
    private static final SalesReportManager reportManager = new SalesReportManager(salesManager);
    private static final CustomerManager customerManager = new CustomerManager();
    private static final UserManager userManager = new UserManager();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   SARI-SARI STORE MANAGEMENT SYSTEM");
        System.out.println("==========================================");

        // 1. Initialize and load saved records on startup
        FileHandler.initializeFiles();
        loadAllData();

        boolean appRunning = true;
        while (appRunning) {
            System.out.println("\n--- MAIN LOGIN ---");
            System.out.println("1. Login");
            System.out.println("2. Exit & Save");
            int choice = getIntInput("Choose an option: ");

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    saveAllData();
                    System.out.println("All records saved. Goodbye!");
                    appRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    private static void handleLogin() {
        String username = getStringInput("Enter Username: ");
        String password = getStringInput("Enter Password: ");

        User currentUser = userManager.login(username, password);
        if (currentUser == null) {
            System.out.println("Invalid username or password.");
            return;
        }
        System.out.println("Welcome, " + currentUser.getUsername() + " [" + currentUser.getRole() + "]");
        System.out.println("Permissions: " + currentUser.getRolePermissions());

        if (currentUser.canManageUsers()) {
            showOwnerMenu(currentUser);
        } else {
            showCashierMenu(currentUser);
        }
        userManager.logout();
    }

    private static void showOwnerMenu(User owner) {
        boolean loggedIn = true;
        while (loggedIn) {
            owner.showMenu(); // Polymorphism: Owner prints its own menu
            int choice = getIntInput("Select an operation: ");
            switch (choice) {
                case 1: manageProducts(); break;
                case 2: restockMenu(); break;
                case 3: newSale(); break;
                case 4: manageUtang(); break;
                case 5: expenseMenu(); break;
                case 6: reportMenu(); break;
                case 7: manageCashiers(); break;
                case 8:
                    saveAllData();
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1-8.");
            }
        }
    }

    private static void showCashierMenu(User cashier) {
        boolean loggedIn = true;
        while (loggedIn) {
            cashier.showMenu(); // Polymorphism: Cashier prints its own menu
            int choice = getIntInput("Select an operation: ");
            switch (choice) {
                case 1: newSale(); break;
                case 2: searchProducts(); break;
                case 3: recordUtangPayment(); break;
                case 4:
                    saveAllData();
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1-4.");
            }
        }
    }

    // ===================== INVENTORY =====================
    private static void manageProducts() {
        while (true) {
            System.out.println("\n--- MANAGE PRODUCTS ---");
            System.out.println("1. View All Products");
            System.out.println("2. Search Product (ID or name)");
            System.out.println("3. Add Product");
            System.out.println("4. Edit Product");
            System.out.println("5. Delete Product");
            System.out.println("6. Back");
            int choice = getIntInput("Choose: ");
            try {
                switch (choice) {
                    case 1: inventoryManager.displayAllProducts(); break;
                    case 2: searchProducts(); break;
                    case 3: {
                        String name = getStringInput("Product name: ");
                        String category = getStringInput("Category: ");
                        double cost = getDoubleInput("Buying price: ");
                        double price = getDoubleInput("Selling price: ");
                        int stock = getIntInput("Initial stock: ");
                        Product p = inventoryManager.addProduct(name, category, cost, price, stock);
                        System.out.println("Added: " + p);
                        break;
                    }
                    case 4: {
                        Product p = inventoryManager.getProduct(getStringInput("Product ID to edit: "));
                        System.out.println("Current: " + p);
                        System.out.println("(Press Enter to keep the current value)");
                        String name = getOptionalString("New name: ", p.getName());
                        String category = getOptionalString("New category: ", p.getCategory());
                        double cost = getOptionalDouble("New buying price: ", p.getBuyingPrice());
                        double price = getOptionalDouble("New selling price: ", p.getSellingPrice());
                        inventoryManager.updateProduct(p.getProductId(), name, category, cost, price);
                        System.out.println("Updated: " + p);
                        break;
                    }
                    case 5: {
                        Product p = inventoryManager.getProduct(getStringInput("Product ID to delete: "));
                        if (confirm("Delete " + p.getName() + "? (y/n): ")) {
                            inventoryManager.deleteProduct(p.getProductId());
                            System.out.println("Product deleted.");
                        }
                        break;
                    }
                    case 6: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void searchProducts() {
        String query = getStringInput("Enter product ID or name: ");
        inventoryManager.displayProducts(inventoryManager.search(query));
    }

    private static void restockMenu() {
        inventoryManager.displayLowStock();
        if (!confirm("Restock a product now? (y/n): ")) return;
        try {
            Product p = inventoryManager.getProduct(getStringInput("Product ID: "));
            int qty = getIntInput("Quantity to add: ");
            inventoryManager.restock(p.getProductId(), qty);
            System.out.println("Restocked. " + p);
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ===================== POS =====================
    private static void newSale() {
        Sales sale = salesManager.startSale(getOptionalString("Customer name (Enter for Walk-in): ", ""));
        System.out.println("\n--- NEW SALE " + sale.getSaleId() + " ---");
        System.out.println("Commands: product ID or name to add | 'r' remove item | 'v' view cart | 'p' pay | 'x' cancel");

        while (true) {
            String input = getStringInput("Item> ");
            if (input.equalsIgnoreCase("x")) {
                System.out.println("Sale cancelled. No stock was deducted.");
                return;
            } else if (input.equalsIgnoreCase("v")) {
                printCart(sale);
            } else if (input.equalsIgnoreCase("r")) {
                String id = getStringInput("Product ID to remove: ");
                System.out.println(sale.removeItem(id) ? "Item removed." : "Item not in cart.");
            } else if (input.equalsIgnoreCase("p")) {
                if (sale.getItems().isEmpty()) {
                    System.out.println("Cart is empty.");
                } else if (checkout(sale)) {
                    return;
                }
            } else {
                addToCart(sale, input);
            }
        }
    }

    private static void addToCart(Sales sale, String query) {
        List<Product> matches = inventoryManager.search(query);
        if (matches.isEmpty()) {
            System.out.println("No product matches '" + query + "'.");
            return;
        }
        Product p;
        if (matches.size() == 1) {
            p = matches.get(0);
        } else {
            inventoryManager.displayProducts(matches);
            p = inventoryManager.findById(getStringInput("Enter exact product ID: "));
            if (p == null) {
                System.out.println("Product not found.");
                return;
            }
        }
        System.out.println(p);
        int qty = getIntInput("Quantity: ");
        try {
            SaleItem item = salesManager.addItem(sale, p.getProductId(), qty);
            System.out.printf("Added %d x %s. Cart total: PHP %.2f%n", qty, item.getProductName(), sale.getTotal());
        } catch (InvalidInputException | InsufficientStockException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printCart(Sales sale) {
        if (sale.getItems().isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        for (SaleItem i : sale.getItems()) {
            System.out.printf("%-5s %-28s x%-4d PHP %8.2f%n",
                    i.getProductId(), i.getProductName(), i.getQuantity(), i.calculateTotal());
        }
        System.out.printf("TOTAL: PHP %.2f%n", sale.getTotal());
    }

    /** Returns true when the sale was completed. */
    private static boolean checkout(Sales sale) {
        printCart(sale);
        System.out.println("Payment method: 1. Cash  2. Card  3. E-Wallet  4. Utang (credit)  5. Back");
        int m = getIntInput("Choose: ");
        BigDecimal total = sale.getTotal();
        Customer utangCustomer = null;
        Payment.Method method;
        BigDecimal tendered;
        switch (m) {
            case 1:
                method = Payment.Method.CASH;
                tendered = BigDecimal.valueOf(getDoubleInput("Amount tendered: PHP ")).setScale(2, RoundingMode.HALF_UP);
                break;
            case 2: method = Payment.Method.CARD; tendered = total; break;
            case 3: method = Payment.Method.E_WALLET; tendered = total; break;
            case 4:
                utangCustomer = pickCustomer();
                if (utangCustomer == null) return false;
                sale.setCustomerName(utangCustomer.getName());
                method = Payment.Method.UTANG;
                tendered = total;
                break;
            default:
                return false;
        }
        try {
            BigDecimal change = salesManager.processPayment(sale, method, tendered);
            if (utangCustomer != null) {
                customerManager.recordUtang(utangCustomer.getCustomerId(), total.doubleValue());
                System.out.printf("PHP %.2f added to %s's utang. New balance: PHP %.2f%n",
                        total, utangCustomer.getName(), utangCustomer.getUtangBalance());
            }
            System.out.println(salesManager.generateReceipt(sale));
            if (change.signum() > 0) System.out.printf("Change: PHP %.2f%n", change);
            return true;
        } catch (InvalidInputException | InsufficientStockException e) {
            System.out.println("Payment failed: " + e.getMessage());
            return false;
        }
    }

    // ===================== CUSTOMERS / UTANG =====================
    private static void manageUtang() {
        while (true) {
            System.out.println("\n--- CUSTOMER UTANG ---");
            System.out.println("1. View All Customers");
            System.out.println("2. View Customers With Utang");
            System.out.println("3. Register Customer");
            System.out.println("4. Record Utang Payment");
            System.out.println("5. Check Remaining Balance");
            System.out.println("6. Back");
            int choice = getIntInput("Choose: ");
            switch (choice) {
                case 1: customerManager.displayAllCustomers(); break;
                case 2: customerManager.displayCustomersWithUtang(); break;
                case 3: registerCustomer(); break;
                case 4: recordUtangPayment(); break;
                case 5: {
                    Customer c = pickCustomer();
                    if (c != null) System.out.printf("%s owes PHP %.2f%n", c.getName(), c.getUtangBalance());
                    break;
                }
                case 6: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static Customer registerCustomer() {
        try {
            Customer c = customerManager.registerCustomer(getStringInput("Customer name: "),
                    getOptionalString("Contact number (optional): ", ""));
            System.out.println("Registered: " + c);
            return c;
        } catch (InvalidInputException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /** Lets the user pick a customer by ID or name; offers registration when none match. */
    private static Customer pickCustomer() {
        String query = getStringInput("Customer ID or name (or 'new' to register): ");
        if (query.equalsIgnoreCase("new")) return registerCustomer();
        Customer c = customerManager.findCustomerById(query);
        if (c != null) return c;
        List<Customer> matches = customerManager.searchCustomersByName(query);
        if (matches.size() == 1) return matches.get(0);
        if (matches.isEmpty()) {
            System.out.println("No customer found.");
            return null;
        }
        for (Customer m : matches) System.out.println(m);
        c = customerManager.findCustomerById(getStringInput("Enter exact customer ID: "));
        if (c == null) System.out.println("No customer found.");
        return c;
    }

    private static void recordUtangPayment() {
        customerManager.displayCustomersWithUtang();
        Customer c = pickCustomer();
        if (c == null) return;
        System.out.printf("Current balance: PHP %.2f%n", c.getUtangBalance());
        double amount = getDoubleInput("Payment amount: PHP ");
        if (customerManager.processDebtPayment(c.getCustomerId(), amount)) {
            System.out.printf("Payment recorded. Remaining balance: PHP %.2f%n", c.getUtangBalance());
        }
    }

    // ===================== EXPENSES =====================
    private static void expenseMenu() {
        while (true) {
            System.out.println("\n--- EXPENSES ---");
            System.out.println("1. Record Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Back");
            int choice = getIntInput("Choose: ");
            switch (choice) {
                case 1:
                    try {
                        Expense e = salesManager.recordExpense(
                                getStringInput("Description: "),
                                getOptionalString("Category (e.g. Rent, Utilities, Supplies): ", "General"),
                                BigDecimal.valueOf(getDoubleInput("Amount: PHP ")).setScale(2, RoundingMode.HALF_UP),
                                getDateInput("Date (yyyy-MM-dd, Enter for today): ", LocalDate.now()));
                        System.out.println("Recorded: " + e);
                    } catch (InvalidInputException ex) {
                        System.out.println("Error: " + ex.getMessage());
                    }
                    break;
                case 2:
                    if (salesManager.getExpenses().isEmpty()) System.out.println("No expenses recorded.");
                    for (Expense e : salesManager.getExpenses()) System.out.println(e);
                    break;
                case 3: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== REPORTS =====================
    private static void reportMenu() {
        while (true) {
            System.out.println("\n--- SALES & PROFIT REPORTS ---");
            System.out.println("1. Today");
            System.out.println("2. This Month");
            System.out.println("3. Custom Date Range");
            System.out.println("4. Compare This Month vs Last Month");
            System.out.println("5. View Sales Transactions");
            System.out.println("6. Back");
            int choice = getIntInput("Choose: ");
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.withDayOfMonth(1);
            switch (choice) {
                case 1: System.out.println(reportManager.generateReport(today, today)); break;
                case 2: System.out.println(reportManager.generateReport(monthStart, today)); break;
                case 3: {
                    LocalDate from = getDateInput("From (yyyy-MM-dd): ", null);
                    LocalDate to = getDateInput("To (yyyy-MM-dd, Enter for today): ", today);
                    if (to.isBefore(from)) {
                        System.out.println("'To' date must not be before 'From' date.");
                    } else {
                        System.out.println(reportManager.generateReport(from, to));
                    }
                    break;
                }
                case 4: {
                    LocalDate lastStart = monthStart.minusMonths(1);
                    LocalDate lastEnd = monthStart.minusDays(1);
                    System.out.println(reportManager.comparePeriods(monthStart, today, lastStart, lastEnd));
                    break;
                }
                case 5:
                    if (salesManager.getPaidSales().isEmpty()) System.out.println("No sales recorded.");
                    for (Sales s : salesManager.getPaidSales()) {
                        System.out.printf("%s | %s | %-15s | %-8s | PHP %10.2f%n", s.getSaleId(),
                                s.getDateTime().toLocalDate(), s.getCustomerName(),
                                s.getPayment().getMethod(), s.getTotal());
                    }
                    break;
                case 6: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== USER ACCOUNTS =====================
    private static void manageCashiers() {
        while (true) {
            System.out.println("\n--- CASHIER ACCOUNTS ---");
            System.out.println("1. View All Accounts");
            System.out.println("2. Add Cashier");
            System.out.println("3. Remove Cashier");
            System.out.println("4. Reset Cashier Password");
            System.out.println("5. Back");
            int choice = getIntInput("Choose: ");
            switch (choice) {
                case 1: userManager.displayAllUsers(); break;
                case 2: {
                    Cashier c = userManager.addCashier(getStringInput("New username: "), getStringInput("Password: "));
                    if (c != null) System.out.println("Added: " + c);
                    break;
                }
                case 3:
                    if (userManager.removeCashier(getStringInput("Cashier user ID: "))) {
                        System.out.println("Cashier removed.");
                    }
                    break;
                case 4:
                    if (userManager.updateCashierPassword(getStringInput("Cashier user ID: "),
                            getStringInput("New password: "))) {
                        System.out.println("Password updated.");
                    } else {
                        System.out.println("Cashier ID not found.");
                    }
                    break;
                case 5: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ===================== PERSISTENCE =====================
    private static void loadAllData() {
        System.out.println("Loading saved data...");
        FileHandler.loadAll(inventoryManager, salesManager, customerManager, userManager);
        System.out.printf("Loaded %d products, %d sales, %d expenses, %d customers, %d users.%n",
                inventoryManager.getAllProducts().size(), salesManager.getPaidSales().size(),
                salesManager.getExpenses().size(), customerManager.getAllCustomers().size(),
                userManager.getAllUsers().size());
    }

    private static void saveAllData() {
        System.out.println("Saving all data to files...");
        FileHandler.saveAll(inventoryManager, salesManager, customerManager, userManager);
    }

    // --- Safe Scanner Input Helpers (Prevents InputMismatchException crashes) ---
    private static String readLine() {
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            // Input stream closed (e.g. Ctrl+Z / piped input ended): save instead of crashing
            System.out.println("\nInput ended. Saving and exiting.");
            saveAllData();
            System.exit(0);
            return "";
        }
    }

    public static String getStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }

    public static String getOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = readLine();
        return input.isEmpty() ? defaultValue : input;
    }

    public static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a whole number.");
            }
        }
    }

    public static double getDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            try {
                double value = Double.parseDouble(input);
                if (Double.isFinite(value)) return value;
            } catch (NumberFormatException e) {
                // fall through to message
            }
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }

    public static double getOptionalDouble(String prompt, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            if (input.isEmpty()) return defaultValue;
            try {
                double value = Double.parseDouble(input);
                if (Double.isFinite(value)) return value;
            } catch (NumberFormatException e) {
                // fall through to message
            }
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }

    /** Reads a yyyy-MM-dd date; blank returns defaultValue (null default means the date is required). */
    public static LocalDate getDateInput(String prompt, LocalDate defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            if (input.isEmpty() && defaultValue != null) return defaultValue;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date! Use yyyy-MM-dd (e.g. 2026-11-26).");
            }
        }
    }

    private static boolean confirm(String prompt) {
        return getOptionalString(prompt, "n").equalsIgnoreCase("y");
    }
}
