package models;

import java.math.BigDecimal;

/** Anything that carries a monetary total (sales, sale line items, expenses). */
public interface Payable {
    BigDecimal calculateTotal();
}
