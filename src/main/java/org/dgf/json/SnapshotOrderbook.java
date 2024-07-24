package org.dgf.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SnapshotOrderbook {
    private String channel;
    private String type;
    private List<Data> data;

    @JsonCreator
    public SnapshotOrderbook(@JsonProperty("channel") String channel,
                             @JsonProperty("type") String type,
                             @JsonProperty("data") List<Data> data)
    {
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
        return "SnapshotOrderbook{" +
                "channel='" + channel + '\'' +
                ", type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    public static class Data
    {
        private String symbol;
        private List<Quotation> bids;
        private List<Quotation> asks;
        private String checksum;

        @JsonCreator
        public Data (@JsonProperty("symbol") String symbol,
                     @JsonProperty("bids") List<Quotation> bids,
                     @JsonProperty("asks") List<Quotation> asks,
                     @JsonProperty("checksum") String checksum)
        {
            this.symbol = symbol;
            this.bids = bids;
            this.asks = asks;
            this.checksum = checksum;
        }

        public String getSymbol() {
            return symbol;
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

        @Override
        public String toString() {
            return "Data{" +
                    "symbol='" + symbol + '\'' +
                    ", bids=" + bids +
                    ", asks=" + asks +
                    ", checksum='" + checksum + '\'' +
                    '}';
        }
    }
}
