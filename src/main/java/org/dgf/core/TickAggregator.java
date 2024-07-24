package org.dgf.core;

import java.time.LocalDateTime;

public class TickAggregator {

    private CandleStick aggregatedCandleStick;
    private LocalDateTime startTime;
    private int tickCounts;

    private static final int INTERVAL = 60;

    public TickAggregator(LocalDateTime startTime, CandleStick candleStick)
    {
        this.aggregatedCandleStick = candleStick;
        this.startTime = startTime;
    }

    public void aggregate(CandleStick next)
    {

    }
}
