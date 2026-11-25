package models;

import util.FileHandler;

public abstract class User {
    private String userId;
    private String username;
    private String password;
    private String role;

    public User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    public boolean checkPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Role-based permission checks for Main.java
    public abstract boolean canManageInventory();
    public abstract boolean canViewProfitReports();
    public abstract boolean canManageUsers();

    // Polymorphic role behavior: each role prints its own menu and describes its permissions
    public abstract void showMenu();
    public abstract String getRolePermissions();

    // CSV: userId,username,password,role
    public String toCSV() {
        return String.join(",", userId, FileHandler.clean(username), FileHandler.clean(password), role);
    }

    public static User fromCSV(String line) {
        String[] parts = line.split(",", -1);
        String id = parts[0];
        String user = parts[1];
        String pass = parts[2];
        String role = parts[3];

        if (role.equalsIgnoreCase("OWNER")) {
            return new Owner(id, user, pass);
        } else {
            return new Cashier(id, user, pass);
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Username: %-15s | Role: %s", userId, username, role);
    }
}
