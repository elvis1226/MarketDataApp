package org.dgf.core;

import org.dgf.json.Quotation;

import java.time.LocalDateTime;
import java.util.List;

public class OrderBook {
    private List<Quotation> bids;
    private List<Quotation> asks;
    private LocalDateTime timestamp;
    private String symbol;

    public OrderBook(String symbol, List<Quotation> bids, List<Quotation> asks, LocalDateTime timestamp) {
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<Quotation> getBids() {
        return bids;
    }

    public void setBids(List<Quotation> bids) {
        this.bids = bids;
    }

    public List<Quotation> getAsks() {
        return asks;
    }

    public void setAsks(List<Quotation> asks) {
        this.asks = asks;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderBook{" +
                "bids=" + bids +
                ", asks=" + asks +
                ", timestamp=" + timestamp +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
