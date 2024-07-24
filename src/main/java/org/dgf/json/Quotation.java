package org.dgf.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Quotation  {
    private final double price;
    private final double quantity;

    @JsonCreator
    public Quotation(@JsonProperty("price") double price,
                     @JsonProperty("qty")   double quantity)
    {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quotation quotation = (Quotation) o;
        return Double.compare(price, quotation.price) == 0 && Double.compare(quantity, quotation.quantity) == 0;
    }

    public boolean samePrice(Quotation next) {
        return Double.compare(this.price, next.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, quantity);
    }

    @Override
    public String toString() {
        return "Quotation{" +
                "price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
