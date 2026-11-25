package managers;

import models.Expense;
import models.Sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Calculates and compares sales and profit over date ranges. */
public class SalesReportManager {
    private final SalesManager salesManager;

    public SalesReportManager(SalesManager salesManager) {
        this.salesManager = salesManager;
    }

    public BigDecimal totalSales(LocalDate from, LocalDate to) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Sales s : salesManager.getPaidSales())
            if (inRange(s.getDateTime().toLocalDate(), from, to)) sum = sum.add(s.getTotal());
        return sum;
    }

    public BigDecimal totalCostOfGoods(LocalDate from, LocalDate to) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Sales s : salesManager.getPaidSales())
            if (inRange(s.getDateTime().toLocalDate(), from, to)) sum = sum.add(s.getTotalCost());
        return sum;
    }

    public BigDecimal grossProfit(LocalDate from, LocalDate to) {
        return totalSales(from, to).subtract(totalCostOfGoods(from, to));
    }

    public BigDecimal totalExpenses(LocalDate from, LocalDate to) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Expense e : salesManager.getExpenses())
            if (inRange(e.getDate(), from, to)) sum = sum.add(e.getAmount());
        return sum;
    }

    /** Net profit = sales - cost of goods - expenses. */
    public BigDecimal netProfit(LocalDate from, LocalDate to) {
        return grossProfit(from, to).subtract(totalExpenses(from, to));
    }

    public String generateReport(LocalDate from, LocalDate to) {
        return String.format(
            "SALES & PROFIT REPORT (%s to %s)%n" +
            "-----------------------------------------%n" +
            "%-22s %14.2f%n%-22s %14.2f%n%-22s %14.2f%n%-22s %14.2f%n%-22s %14.2f%n",
            from, to,
            "Total sales:", totalSales(from, to),
            "Cost of goods:", totalCostOfGoods(from, to),
            "Gross profit:", grossProfit(from, to),
            "Expenses:", totalExpenses(from, to),
            "Net profit:", netProfit(from, to));
    }

    /** Compares two periods (e.g., this month vs last month). */
    public String comparePeriods(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("COMPARISON%n  Period A: %s to %s%n  Period B: %s to %s%n", fromA, toA, fromB, toB));
        sb.append(String.format("%-14s %12s %12s %12s %9s%n", "Metric", "Period A", "Period B", "Difference", "Change"));
        row(sb, "Sales", totalSales(fromA, toA), totalSales(fromB, toB));
        row(sb, "Gross profit", grossProfit(fromA, toA), grossProfit(fromB, toB));
        row(sb, "Expenses", totalExpenses(fromA, toA), totalExpenses(fromB, toB));
        row(sb, "Net profit", netProfit(fromA, toA), netProfit(fromB, toB));
        return sb.toString();
    }

    private void row(StringBuilder sb, String label, BigDecimal a, BigDecimal b) {
        BigDecimal diff = a.subtract(b);
        String pct = b.signum() == 0 ? "n/a"
                : diff.multiply(BigDecimal.valueOf(100)).divide(b.abs(), 1, RoundingMode.HALF_UP) + "%";
        sb.append(String.format("%-14s %12.2f %12.2f %12.2f %9s%n", label, a, b, diff, pct));
    }

    private boolean inRange(LocalDate d, LocalDate from, LocalDate to) {
        return !d.isBefore(from) && !d.isAfter(to);
    }
}
