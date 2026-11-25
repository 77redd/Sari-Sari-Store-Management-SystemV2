# Sari-Sari Store Management System

A console-based Java application designed for local sari-sari store owners and cashiers to manage product inventory, process point-of-sale (POS) transactions, track business expenses, and monitor customer credit (*utang*) records.

Developed in partial fulfillment of the requirements for **OOP101 – Object-Oriented Programming** at **Batangas State University – The National Engineering University (Alangilan Campus)**.

---

## Features

* **Role-Based Access Control:** Separate console menus and permissions for **Store Owners** and **Cashiers**.
* **Inventory Management:** Add, edit, delete, search (by ID or name), restock items, and receive automated low-stock warnings
* **Point-of-Sale (POS) & Receipts:** Process customer purchases, automatically deduct sold stock, compute change, and generate transaction receipts
* **Customer Credit (*Utang*) Ledger:** Register customers, log unpaid credit purchases, process partial or full payments, and check remaining balances
* **Expense & Profit Reporting:** Record store expenses and generate daily/overall sales and profit comparisons
* **Persistent File Storage:** Automatically saves and loads all records using local CSV files so data remains intact between sessions

---

## Object-Oriented Programming (OOP) Concepts Applied

* **Encapsulation:** Private attributes with controlled getters and setters across core models (`Product`, `User`, `Customer`, `Sales`).
* **Inheritance:** Specialized user roles (`Owner` and `Cashier`) inherit shared properties and authentication methods from the base `User` class.
* **Abstraction:** Uses abstract base classes (`User`) and interfaces (`Payable`) to define standard contracts while hiding complex backend calculations and file-handling operations.
* **Polymorphism:** Method overriding (`showMenu()`, `calculateTotal()`) enables different behaviors depending on the logged-in user role or transaction type.
* **Exception Handling & Collections:** Uses `ArrayList` for dynamic record management and `try-catch` validation to prevent crashes from invalid inputs or insufficient stock.

---

## Project Structure

```text
SariSariStoreSystem/
├── src/
│   ├── Main.java                       # Entry point, login loop, and role-based menus
│   ├── models/                         # Core data entities & OOP hierarchy
│   │   ├── Payable.java                # Interface: calculateTotal()
│   │   ├── Product.java
│   │   ├── Sales.java
│   │   ├── SaleItem.java
│   │   ├── Payment.java
│   │   ├── Expense.java
│   │   ├── Customer.java
│   │   ├── User.java
│   │   ├── Owner.java
│   │   └── Cashier.java
│   ├── managers/                       # Business logic & ArrayList controllers
│   │   ├── InventoryManager.java
│   │   ├── SalesManager.java
│   │   ├── SalesReportManager.java
│   │   ├── CustomerManager.java
│   │   └── UserManager.java
│   └── util/
│       ├── FileHandler.java            # CSV file reading and writing
│       ├── InvalidInputException.java
│       └── InsufficientStockException.java
├── data/                               # Local persistent storage (.csv files)
├── docs/                               # Project proposal & user documentation
├── diagrams/                           # UML / Class diagrams
├── screenshots/                        # Application output screenshots
├── RunStore.bat                        # Windows launcher (exe, local build, or source)
└── README.md
```

---

## Download & Run (no Java needed)

1. Open the [**Releases** page](https://github.com/77redd/Sari-Sari-Store-Management-SystemV2/releases) (or download the latest build directly: [SariSariStore.zip](https://github.com/77redd/Sari-Sari-Store-Management-SystemV2/releases/latest/download/SariSariStore.zip)).
2. Under the latest release, download **`SariSariStore.zip`** from *Assets*.
3. Right-click the zip and choose **Extract All...**. Do not run it from inside the zip, or your records will not be saved.
4. Open the extracted `SariSariStore` folder and double-click **`RunStore.bat`** (or `SariSariStore.exe`).

The bundled Java runtime is included, so nothing else needs to be installed. Records are saved in a `data\` folder created next to the `.exe`. Keep that folder to keep your data when you move or update the app.

> Windows SmartScreen may warn that the app is from an unknown publisher because it is unsigned. Click **More info** and then **Run anyway**.

On first run, 75 default products and two accounts are seeded:

| Role    | Username  | Password   |
|---------|-----------|------------|
| Owner   | `admin`   | `admin123` |
| Cashier | `cashier` | `1234`     |

## Run From Source (JDK 17+)

Run from the project root so `data/` resolves correctly:

```bash
javac -d bin src/Main.java src/models/*.java src/managers/*.java src/util/*.java
java -cp bin Main
```

Or double-click `RunStore.bat` in the project root. It runs a locally built `SariSariStore\SariSariStore.exe` if one exists, and otherwise compiles and runs the source.
