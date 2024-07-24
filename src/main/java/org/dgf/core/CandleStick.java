package org.dgf.core;

import java.time.LocalDateTime;

public class CandleStick {
    private double open;
    private double high;
    private double low;
    private double close;
    private LocalDateTime start;
    private int tickCount;
    private String symbol;

    public CandleStick(String symbol, double open, double high, double low, double close, LocalDateTime start, int tickCount)
    {
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.start = start;
        this.tickCount = tickCount;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    @Override
    public String toString() {
        return "1M CandleStick{" +
                "symbol=" + symbol +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", start=" + start +
                ", tickCount=" + tickCount +
                '}';
    }
}
