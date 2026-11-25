package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Payment made for a sale. Calculates change for cash payments. UTANG = charged to a customer's credit. */
public class Payment {
    public enum Method { CASH, CARD, E_WALLET, UTANG }

    private final Method method;
    private final BigDecimal amountDue;
    private final BigDecimal amountTendered;
    private final BigDecimal change;
    private final LocalDateTime paidAt;

    public Payment(Method method, BigDecimal amountDue, BigDecimal amountTendered) {
        if (amountTendered.compareTo(amountDue) < 0)
            throw new IllegalArgumentException(
                "Insufficient payment. Due: " + amountDue + ", tendered: " + amountTendered);
        this.method = method;
        this.amountDue = amountDue;
        this.amountTendered = amountTendered;
        this.change = amountTendered.subtract(amountDue);
        this.paidAt = LocalDateTime.now();
    }

    public Method getMethod()              { return method; }
    public BigDecimal getAmountDue()       { return amountDue; }
    public BigDecimal getAmountTendered()  { return amountTendered; }
    public BigDecimal getChange()          { return change; }
    public LocalDateTime getPaidAt()       { return paidAt; }

    // Nested inside a sales.csv row, so fields use ';'. Format: method;amountDue;amountTendered
    public String toCSV() {
        return method + ";" + amountDue.toPlainString() + ";" + amountTendered.toPlainString();
    }

    public static Payment fromCSV(String field) {
        String[] p = field.split(";", -1);
        return new Payment(Method.valueOf(p[0]), new BigDecimal(p[1]), new BigDecimal(p[2]));
    }
}
