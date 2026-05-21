package com.sifap.paymentprocessing.domain;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/** A payment competence (year-month). Pattern {@code YYYY-MM}. */
public final class Competence {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final YearMonth value;

    private Competence(YearMonth value) { this.value = value; }

    public static Competence parse(String text) {
        Objects.requireNonNull(text, "text");
        try {
            return new Competence(YearMonth.parse(text, FORMAT));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                "Invalid competence '%s'. Expected YYYY-MM.".formatted(text), ex);
        }
    }

    public static Competence of(int year, int month) { return new Competence(YearMonth.of(year, month)); }

    public boolean isDecember() { return value.getMonthValue() == 12; }

    public boolean isFuture(LocalDate today) { return value.isAfter(YearMonth.from(today).plusMonths(1)); }

    public boolean isOlderThanMonths(int months, LocalDate today) {
        return value.isBefore(YearMonth.from(today).minusMonths(months));
    }

    public YearMonth asYearMonth() { return value; }

    public String asText() { return value.format(FORMAT); }

    @Override public boolean equals(Object o) {
        return o instanceof Competence c && c.value.equals(this.value);
    }

    @Override public int hashCode() { return value.hashCode(); }

    @Override public String toString() { return asText(); }
}
