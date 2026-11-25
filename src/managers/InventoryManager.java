package managers;

import models.Product;
import util.InsufficientStockException;
import util.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/** Product catalog and stock control. */
public class InventoryManager {
    public static final int LOW_STOCK_LIMIT = 10;

    private List<Product> products = new ArrayList<>();

    public List<Product> getAllProducts() { return products; }

    public void setProducts(List<Product> products) {
        this.products = products;
        if (this.products.isEmpty()) seedDefaultProducts();
    }

    public Product findById(String productId) {
        for (Product p : products) {
            if (p.getProductId().equalsIgnoreCase(productId.trim())) return p;
        }
        return null;
    }

    public List<Product> searchByName(String keyword) {
        List<Product> results = new ArrayList<>();
        String k = keyword.trim().toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(k)) results.add(p);
        }
        return results;
    }

    /** Exact ID match first, otherwise name search. */
    public List<Product> search(String query) {
        Product byId = findById(query);
        if (byId != null) return List.of(byId);
        return searchByName(query);
    }

    public Product getProduct(String productId) throws InvalidInputException {
        Product p = findById(productId);
        if (p == null) throw new InvalidInputException("Product not found: " + productId);
        return p;
    }

    // Auto-generates IDs like P001, P002... continuing from the highest existing number
    public String generateProductId() {
        int max = 0;
        for (Product p : products) {
            try {
                max = Math.max(max, Integer.parseInt(p.getProductId().substring(1)));
            } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
                // custom IDs that don't follow P### are skipped
            }
        }
        return String.format("P%03d", max + 1);
    }

    public Product addProduct(String name, String category, double buyingPrice,
                              double sellingPrice, int stock) throws InvalidInputException {
        try {
            Product p = new Product(generateProductId(), name, category, buyingPrice, sellingPrice, stock);
            products.add(p);
            return p;
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    public void updateProduct(String productId, String name, String category,
                              double buyingPrice, double sellingPrice) throws InvalidInputException {
        Product p = getProduct(productId);
        try {
            // validate everything first so a bad field doesn't leave the product half-edited
            new Product(p.getProductId(), name, category, buyingPrice, sellingPrice, p.getStock());
            p.setName(name);
            p.setCategory(category);
            p.setBuyingPrice(buyingPrice);
            p.setSellingPrice(sellingPrice);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    public void deleteProduct(String productId) throws InvalidInputException {
        products.remove(getProduct(productId));
    }

    public void restock(String productId, int quantity) throws InvalidInputException {
        Product p = getProduct(productId);
        try {
            p.addStock(quantity);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(e.getMessage());
        }
    }

    public void deductStock(String productId, int quantity)
            throws InvalidInputException, InsufficientStockException {
        getProduct(productId).reduceStock(quantity);
    }

    public List<Product> getLowStockProducts() {
        List<Product> low = new ArrayList<>();
        for (Product p : products) {
            if (p.isLowStock(LOW_STOCK_LIMIT)) low.add(p);
        }
        return low;
    }

    public void displayProducts(List<Product> list) {
        if (list.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        for (Product p : list) System.out.println(p);
    }

    public void displayAllProducts() {
        System.out.println("\n--- PRODUCT LIST (" + products.size() + " items) ---");
        displayProducts(products);
    }

    public void displayLowStock() {
        List<Product> low = getLowStockProducts();
        System.out.println("\n--- LOW STOCK ALERTS (stock <= " + LOW_STOCK_LIMIT + ") ---");
        if (low.isEmpty()) System.out.println("All products are sufficiently stocked.");
        else displayProducts(low);
    }

    // 75 default items: 15 per category. {name, buying price, selling price, stock}
    private void seedDefaultProducts() {
        Object[][] food = {
            {"Lucky Me Pancit Canton 60g", 11.0, 14.0, 60}, {"Lucky Me Beef Mami 55g", 9.0, 12.0, 50},
            {"Nissin Cup Noodles Seafood", 22.0, 28.0, 30}, {"Payless Xtra Big Instant Mami", 8.5, 11.0, 40},
            {"Argentina Corned Beef 150g", 38.0, 45.0, 30}, {"Purefoods Corned Beef 150g", 42.0, 50.0, 24},
            {"555 Sardines Tomato Sauce 155g", 19.0, 24.0, 48}, {"Ligo Sardines Red 155g", 21.0, 26.0, 48},
            {"Mega Sardines Green 155g", 20.0, 25.0, 36}, {"Century Tuna Flakes in Oil 155g", 34.0, 40.0, 30},
            {"CDO Meat Loaf 150g", 24.0, 30.0, 24}, {"Libby's Vienna Sausage 130g", 32.0, 38.0, 20},
            {"Spam Classic 340g", 185.0, 210.0, 8}, {"Maling Luncheon Meat 397g", 120.0, 140.0, 10},
            {"Rice 1kg (Dinorado)", 52.0, 60.0, 50},
        };
        Object[][] beverages = {
            {"Coca-Cola Mismo 290ml", 13.0, 17.0, 48}, {"Royal Tru-Orange Mismo 290ml", 13.0, 17.0, 36},
            {"Sprite Mismo 290ml", 13.0, 17.0, 36}, {"Coca-Cola 1.5L", 62.0, 72.0, 24},
            {"C2 Green Tea Apple 230ml", 15.0, 20.0, 36}, {"Zesto Orange Juice Drink 200ml", 8.0, 11.0, 50},
            {"Nescafe Classic Stick 2g", 1.5, 2.0, 200}, {"Nescafe 3-in-1 Original Twin", 12.0, 15.0, 80},
            {"Kopiko Brown Coffee Twin", 12.0, 15.0, 80}, {"Great Taste White Twin", 12.0, 15.0, 80},
            {"Milo Sachet 22g", 8.0, 10.0, 100}, {"Bear Brand Powdered Milk 33g", 12.0, 15.0, 60},
            {"Tang Orange Sachet 20g", 18.0, 22.0, 40}, {"Cobra Energy Drink 350ml", 20.0, 25.0, 24},
            {"Nature's Spring Water 500ml", 9.0, 12.0, 48},
        };
        Object[][] snacks = {
            {"Piattos Cheese 40g", 15.0, 19.0, 30}, {"Nova Country Cheddar 40g", 15.0, 19.0, 30},
            {"Chippy BBQ 27g", 9.0, 12.0, 40}, {"Oishi Prawn Crackers 60g", 13.0, 16.0, 30},
            {"Clover Chips Cheese 26g", 8.0, 10.0, 40}, {"Skyflakes Crackers 25g", 6.0, 8.0, 60},
            {"Fita Crackers 30g", 6.5, 8.0, 50}, {"Rebisco Crackers 32g", 6.0, 8.0, 50},
            {"Hansel Mocha Sandwich", 6.0, 8.0, 50}, {"Cream-O Vanilla 33g", 7.0, 9.0, 50},
            {"Magic Flakes 28g", 6.0, 8.0, 40}, {"Choco Mucho 32g", 8.5, 11.0, 40},
            {"Cloud 9 Classic 30g", 8.5, 11.0, 40}, {"Hany Chocolate", 1.0, 1.5, 150},
            {"Boy Bawang Cornick 100g", 18.0, 22.0, 24},
        };
        Object[][] condiments = {
            {"Datu Puti Soy Sauce 200ml", 12.0, 15.0, 40}, {"Datu Puti Vinegar 200ml", 11.0, 14.0, 40},
            {"Silver Swan Soy Sauce 385ml", 22.0, 27.0, 24}, {"UFC Banana Ketchup 320g", 27.0, 33.0, 24},
            {"Rufina Patis 350ml", 28.0, 34.0, 18}, {"Mang Tomas All-Purpose Sauce 330g", 38.0, 45.0, 12},
            {"Ajinomoto Umami Seasoning 11g", 3.5, 5.0, 100}, {"Knorr Chicken Cube", 6.0, 8.0, 80},
            {"Magic Sarap 8g", 3.5, 5.0, 100}, {"Iodized Salt 250g", 10.0, 13.0, 30},
            {"White Sugar 1/4 kg", 22.0, 26.0, 40}, {"Cooking Oil 250ml (Pouch)", 28.0, 34.0, 30},
            {"Garlic 1/4 kg", 35.0, 42.0, 15}, {"Onion 1/4 kg", 30.0, 37.0, 15},
            {"Mama Sita's Oyster Sauce 30g", 9.0, 12.0, 30},
        };
        Object[][] toiletries = {
            {"Safeguard Soap 60g", 23.0, 28.0, 30}, {"Palmolive Shampoo Sachet", 6.0, 8.0, 100},
            {"Sunsilk Shampoo Sachet", 6.0, 8.0, 100}, {"Head & Shoulders Sachet", 7.0, 9.0, 80},
            {"Colgate Toothpaste 25ml", 22.0, 27.0, 30}, {"Hapee Toothbrush", 12.0, 16.0, 24},
            {"Surf Powder Detergent 66g", 7.0, 9.0, 100}, {"Tide Bar 125g", 13.0, 16.0, 40},
            {"Downy Fabric Conditioner Sachet", 7.0, 9.0, 80}, {"Joy Dishwashing Liquid Sachet", 6.0, 8.0, 80},
            {"Zonrox Bleach 250ml", 15.0, 19.0, 24}, {"Modess Napkin (Single)", 7.0, 9.0, 40},
            {"Tissue Roll 1-ply", 10.0, 13.0, 36}, {"Baygon Mosquito Coil", 4.0, 6.0, 60},
            {"Candle (Medium)", 5.0, 7.0, 50},
        };
        addSeed("Food & Canned Goods", food);
        addSeed("Beverages", beverages);
        addSeed("Snacks & Biscuits", snacks);
        addSeed("Condiments & Cooking", condiments);
        addSeed("Toiletries & Household", toiletries);
        System.out.println("Seeded " + products.size() + " default products.");
    }

    private void addSeed(String category, Object[][] rows) {
        for (Object[] r : rows) {
            products.add(new Product(generateProductId(), (String) r[0], category,
                    (Double) r[1], (Double) r[2], (Integer) r[3]));
        }
    }
}
