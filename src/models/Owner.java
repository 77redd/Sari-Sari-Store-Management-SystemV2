package models;

public class Owner extends User {

    public Owner(String userId, String username, String password) {
        super(userId, username, password, "OWNER");
    }

    @Override
    public boolean canManageInventory() {
        return true;
    }

    @Override
    public boolean canViewProfitReports() {
        return true;
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }

    @Override
    public void showMenu() {
        System.out.println("\n=== OWNER MENU (" + getUsername() + ") ===");
        System.out.println("1. Manage Products (Add / Edit / Delete / View / Search)");
        System.out.println("2. Restock & View Low-Stock Alerts");
        System.out.println("3. New Sale (POS)");
        System.out.println("4. Manage Customer Utang (Credit)");
        System.out.println("5. Record / View Expenses");
        System.out.println("6. View Sales & Profit Reports");
        System.out.println("7. Manage Cashier Accounts");
        System.out.println("8. Logout");
    }

    @Override
    public String getRolePermissions() {
        return "Full access: inventory, POS, utang, expenses, reports, user accounts";
    }
}