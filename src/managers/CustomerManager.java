package managers;

import models.Customer;
import util.InvalidInputException;
import java.util.ArrayList;
import java.util.List;

public class CustomerManager {
    private List<Customer> customers;

    public CustomerManager() {
        this.customers = new ArrayList<>();
    }

    public List<Customer> getAllCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    // Auto-generates IDs like C001, C002...
    public String generateCustomerId() {
        int nextNum = customers.size() + 1;
        return String.format("C%03d", nextNum);
    }

    public Customer registerCustomer(String name, String contactNumber) throws InvalidInputException {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }
        String id = generateCustomerId();
        Customer newCustomer = new Customer(id, name.trim(),
                (contactNumber == null || contactNumber.isBlank()) ? "N/A" : contactNumber.trim(), 0.0);
        customers.add(newCustomer);
        return newCustomer;
    }

    public Customer findCustomerById(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(customerId)) {
                return c;
            }
        }
        return null;
    }

    public List<Customer> searchCustomersByName(String keyword) {
        List<Customer> results = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(c);
            }
        }
        return results;
    }

    // Used when a customer buys items on credit (Utang)
    public boolean recordUtang(String customerId, double amount) {
        Customer c = findCustomerById(customerId);
        if (c != null && amount > 0) {
            c.addUtang(amount);
            return true;
        }
        return false;
    }

    // Handles both partial and full debt payments
    public boolean processDebtPayment(String customerId, double paymentAmount) {
        Customer c = findCustomerById(customerId);
        if (c == null) {
            System.out.println("Customer not found.");
            return false;
        }
        if (paymentAmount <= 0) {
            System.out.println("Payment amount must be greater than zero.");
            return false;
        }
        if (paymentAmount > c.getUtangBalance()) {
            System.out.printf("Payment (PHP %.2f) exceeds remaining balance (PHP %.2f).%n",
                    paymentAmount, c.getUtangBalance());
            return false;
        }

        c.payUtang(paymentAmount);
        return true;
    }

    public double getRemainingBalance(String customerId) {
        Customer c = findCustomerById(customerId);
        return (c != null) ? c.getUtangBalance() : -1;
    }

    public void displayAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No registered customers.");
            return;
        }
        System.out.println("\n--- CUSTOMER RECORDS ---");
        for (Customer c : customers) {
            System.out.println(c);
        }
    }

    public void displayCustomersWithUtang() {
        boolean found = false;
        System.out.println("\n--- CUSTOMERS WITH OUTSTANDING UTANG ---");
        for (Customer c : customers) {
            if (c.getUtangBalance() > 0) {
                System.out.println(c);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No customers currently have unpaid balances.");
        }
    }
}