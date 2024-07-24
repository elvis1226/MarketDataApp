package org.dgf.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TickUpdate {
    private final String channel;
    private final String type;
    private final List<Data> data;

    @JsonCreator
    public TickUpdate(@JsonProperty("channel")String channel,
                      @JsonProperty("type") String type,
                      @JsonProperty("data") List<Data> data) {
        this.channel = channel;
        this.type = type;
        this.data = data;
    }

    public String getChannel() {
        return channel;
    }

    public String getType() {
        return type;
    }

    public List<Data> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "TickUpdate{" +
                "channel='" + channel + '\'' +
                ", type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    public static class Data {
        private final String symbol;
        private final List<Quotation> bids;
        private final List<Quotation> asks;
        private final String checksum;
        private final String timestamp;

        @JsonCreator
        public Data (@JsonProperty("symbol") String symbol,
                     @JsonProperty("bids") List<Quotation> bids,
                     @JsonProperty("asks") List<Quotation> asks,
                     @JsonProperty("checksum") String checksum,
                     @JsonProperty("timestamp") String timestamp) {
            this.symbol = symbol;
            this.bids = bids;
            this.asks = asks;
            this.checksum = checksum;
            this.timestamp = timestamp;
        }

        public List<Quotation> getBids() {
            return List.copyOf(bids);
        }

        public List<Quotation> getAsks() {
            return List.copyOf(asks);
        }

        public String getChecksum() {
            return checksum;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "symbol='" + symbol + '\'' +
                    ", bids=" + bids +
                    ", asks=" + asks +
                    ", checksum='" + checksum + '\'' +
                    ", timestamp='" + timestamp + '\'' +
                    '}';
        }
    }
}
