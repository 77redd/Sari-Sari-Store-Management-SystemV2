package models;

import util.FileHandler;

public class Customer {
    private String customerId;
    private String name;
    private String contactNumber;
    private double utangBalance;

    public Customer(String customerId, String name, String contactNumber, double utangBalance) {
        this.customerId = customerId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.utangBalance = utangBalance;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public double getUtangBalance() { return utangBalance; }

    public void setName(String name) { this.name = name; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public void addUtang(double amount) {
        if (amount > 0) {
            this.utangBalance += amount;
        }
    }

    public boolean payUtang(double amount) {
        if (amount > 0 && amount <= this.utangBalance) {
            this.utangBalance -= amount;
            return true;
        }
        return false;
    }

    // For FileHandler saving/loading. CSV: customerId,name,contactNumber,utangBalance
    public String toCSV() {
        return String.join(",", customerId, FileHandler.clean(name),
                FileHandler.clean(contactNumber), String.valueOf(utangBalance));
    }

    public static Customer fromCSV(String line) {
        String[] parts = line.split(",", -1);
        return new Customer(
            parts[0],
            parts[1],
            parts[2],
            Double.parseDouble(parts[3])
        );
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %-18s | Contact: %-12s | Utang Balance: PHP %.2f",
                customerId, name, contactNumber, utangBalance);
    }
}