package models;

import util.FileHandler;
import util.InsufficientStockException;

public class Product {

    // Attributes
    private String productId;
    private String name;
    private String category;
    private double buyingPrice;
    private double sellingPrice;
    private int stock;

    // Constructor
    public Product(String productId, String name, String category,
                   double buyingPrice, double sellingPrice, int stock) {

        setProductId(productId);
        setName(name);
        setCategory(category);
        setBuyingPrice(buyingPrice);
        setSellingPrice(sellingPrice);
        setStock(stock);
    }

    // Add stock
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be greater than zero."
            );
        }

        stock += quantity;
    }

    // Reduce stock
    public void reduceStock(int quantity) throws InsufficientStockException {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be greater than zero."
            );
        }

        if (quantity > stock) {
            throw new InsufficientStockException(
                "Insufficient stock for " + name + ". Available: " + stock + ", requested: " + quantity
            );
        }

        stock -= quantity;
    }

    // Check low stock
    public boolean isLowStock(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException(
                "Low stock limit cannot be negative."
            );
        }

        return stock <= limit;
    }

    // Get product description
    public String getDescription() {
        return "Product ID: " + productId +
               "\nProduct Name: " + name +
               "\nCategory: " + category +
               "\nBuying Price: " + buyingPrice +
               "\nSelling Price: " + sellingPrice +
               "\nAvailable Stock: " + stock;
    }

    // Getters
    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public int getStock() {
        return stock;
    }

    // Setters
    public void setProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Product ID cannot be empty."
            );
        }

        this.productId = productId.trim();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Product name cannot be empty."
            );
        }

        this.name = name.trim();
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Category cannot be empty."
            );
        }

        this.category = category.trim();
    }

    public void setBuyingPrice(double buyingPrice) {
        if (!Double.isFinite(buyingPrice) || buyingPrice < 0) {
            throw new IllegalArgumentException(
                "Invalid buying price."
            );
        }

        this.buyingPrice = buyingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        if (!Double.isFinite(sellingPrice) || sellingPrice < 0) {
            throw new IllegalArgumentException(
                "Invalid selling price."
            );
        }

        this.sellingPrice = sellingPrice;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException(
                "Stock cannot be negative."
            );
        }

        this.stock = stock;
    }

    // CSV: productId,name,category,buyingPrice,sellingPrice,stock
    public String toCSV() {
        return String.join(",", productId, FileHandler.clean(name), FileHandler.clean(category),
                String.valueOf(buyingPrice), String.valueOf(sellingPrice), String.valueOf(stock));
    }

    public static Product fromCSV(String line) {
        String[] p = line.split(",", -1);
        return new Product(p[0], p[1], p[2],
                Double.parseDouble(p[3]), Double.parseDouble(p[4]), Integer.parseInt(p[5]));
    }

    @Override
    public String toString() {
        return String.format("%-5s | %-28s | %-22s | Cost %8.2f | Price %8.2f | Stock %4d",
                productId, name, category, buyingPrice, sellingPrice, stock);
    }
}
