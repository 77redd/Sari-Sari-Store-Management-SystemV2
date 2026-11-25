package models;

public class Cashier extends User {

    public Cashier(String userId, String username, String password) {
        super(userId, username, password, "CASHIER");
    }

    @Override
    public boolean canManageInventory() {
        return false; // Cashiers can view products during sales, not edit/delete stock
    }

    @Override
    public boolean canViewProfitReports() {
        return false;
    }

    @Override
    public boolean canManageUsers() {
        return false;
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== CASHIER MENU (" + getUsername() + ") ===");
        System.out.println("1. New Sale (POS)");
        System.out.println("2. Check Product Price & Stock");
        System.out.println("3. Record Customer Utang Payment");
        System.out.println("4. Logout");
    }

    @Override
    public String getRolePermissions() {
        return "Limited access: POS, product lookup, utang payments";
    }
}