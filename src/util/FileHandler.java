package util;

import managers.CustomerManager;
import managers.InventoryManager;
import managers.SalesManager;
import managers.UserManager;
import models.Customer;
import models.Expense;
import models.Product;
import models.Sales;
import models.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FileHandler {
    private static final String DATA_DIR = "data/";

    // File paths for each module
    public static final String PRODUCTS_FILE = DATA_DIR + "products.csv";
    public static final String SALES_FILE = DATA_DIR + "sales.csv";
    public static final String EXPENSES_FILE = DATA_DIR + "expenses.csv";
    public static final String CUSTOMERS_FILE = DATA_DIR + "customers.csv";
    public static final String USERS_FILE = DATA_DIR + "users.csv";

    // Ensures the data/ folder and empty files exist on first run
    public static void initializeFiles() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String[] files = {PRODUCTS_FILE, SALES_FILE, EXPENSES_FILE, CUSTOMERS_FILE, USERS_FILE};
        for (String filePath : files) {
            File file = new File(filePath);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                System.out.println("Error creating file " + filePath + ": " + e.getMessage());
            }
        }
    }

    /** Strips characters used as CSV delimiters (, ; |) and line breaks from free-text fields. */
    public static String clean(String text) {
        return text == null ? "" : text.replaceAll("[,;|\\r\\n]", " ").trim();
    }

    // Generic method to read all non-empty lines from a CSV file
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from " + filePath + ": " + e.getMessage());
        }
        return lines;
    }

    // Generic method to overwrite a CSV file with updated records
    public static void writeLines(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Parses every line with the given fromCSV method. Corrupt lines are skipped with a warning,
     * and the original file is copied to *.bak so the next save cannot silently lose them.
     */
    private static <T> List<T> load(String filePath, Function<String, T> parser) {
        List<T> records = new ArrayList<>();
        int skipped = 0;
        for (String line : readLines(filePath)) {
            try {
                records.add(parser.apply(line));
            } catch (RuntimeException e) {
                skipped++;
                System.out.println("Skipping invalid record in " + filePath + ": " + line);
            }
        }
        if (skipped > 0) {
            try {
                Files.copy(Paths.get(filePath), Paths.get(filePath + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Original kept as " + filePath + ".bak");
            } catch (IOException e) {
                System.out.println("Could not back up " + filePath + ": " + e.getMessage());
            }
        }
        return records;
    }

    /** Loads all records on startup. Empty products/users files trigger the default seed data. */
    public static void loadAll(InventoryManager inventory, SalesManager sales,
                               CustomerManager customers, UserManager users) {
        inventory.setProducts(load(PRODUCTS_FILE, Product::fromCSV));
        sales.loadSales(load(SALES_FILE, Sales::fromCSV));
        sales.loadExpenses(load(EXPENSES_FILE, Expense::fromCSV));
        customers.setCustomers(load(CUSTOMERS_FILE, Customer::fromCSV));
        users.setUsers(load(USERS_FILE, User::fromCSV));
    }

    /** Saves every record back to data/*.csv. */
    public static void saveAll(InventoryManager inventory, SalesManager sales,
                               CustomerManager customers, UserManager users) {
        List<String> lines = new ArrayList<>();
        for (Product p : inventory.getAllProducts()) lines.add(p.toCSV());
        writeLines(PRODUCTS_FILE, lines);

        lines = new ArrayList<>();
        for (Sales s : sales.getPaidSales()) lines.add(s.toCSV());
        writeLines(SALES_FILE, lines);

        lines = new ArrayList<>();
        for (Expense e : sales.getExpenses()) lines.add(e.toCSV());
        writeLines(EXPENSES_FILE, lines);

        lines = new ArrayList<>();
        for (Customer c : customers.getAllCustomers()) lines.add(c.toCSV());
        writeLines(CUSTOMERS_FILE, lines);

        lines = new ArrayList<>();
        for (User u : users.getAllUsers()) lines.add(u.toCSV());
        writeLines(USERS_FILE, lines);
    }
}
