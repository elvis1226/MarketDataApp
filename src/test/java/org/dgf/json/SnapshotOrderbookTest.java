package org.dgf.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SnapshotOrderbookTest {

    @Test
    public void ParseNormalSnapshotJson_ReturnExpectedResult() {
        final String json = "{\"channel\":\"book\",\"type\":\"snapshot\"," +
                "\"data\":[{\"symbol\":\"BTC/USD\"," +
                            "\"bids\":[{\"price\":64359.9,\"qty\":10.32729064}," +
                            "{\"price\":64357.1,\"qty\":0.46617082},{\"price\":64356.0,\"qty\":0.10339100}," +
                            "{\"price\":64355.8,\"qty\":0.46607082},{\"price\":64351.2,\"qty\":0.96641677}," +
                            "{\"price\":64348.8,\"qty\":0.00794151},{\"price\":64348.1,\"qty\":0.15576086}," +
                            "{\"price\":64347.1,\"qty\":0.73740000},{\"price\":64346.4,\"qty\":0.15576086}," +
                            "{\"price\":64346.2,\"qty\":0.04454749}]," +
                            "\"asks\":[{\"price\":64360.0,\"qty\":0.85796294}," +
                            "{\"price\":64363.3,\"qty\":0.00010000},{\"price\":64364.4,\"qty\":0.00439003}," +
                            "{\"price\":64364.8,\"qty\":0.00225996},{\"price\":64365.2,\"qty\":0.02003333}," +
                            "{\"price\":64365.3,\"qty\":0.00177828},{\"price\":64365.8,\"qty\":0.00010000}," +
                            "{\"price\":64366.0,\"qty\":0.00558943},{\"price\":64366.8,\"qty\":0.00170067}," +
                            "{\"price\":64369.7,\"qty\":0.00095433}]," +
                            "\"checksum\":2124886230}]" +
                "}";
        final JsonUtility utility = new JsonUtility();
        Optional<SnapshotOrderbook> snapshot = utility.parseSnapshot(json);
        assertTrue(snapshot.isPresent());

        assertEquals("book", snapshot.get().getChannel());
        assertEquals("snapshot", snapshot.get().getType());
        assertTrue(snapshot.get().getData().size() == 1);

        SnapshotOrderbook.Data data = snapshot.get().getData().get(0);
        assertEquals("BTC/USD", data.getSymbol());
        assertEquals("2124886230", data.getChecksum());

        List<Quotation> bids = data.getBids();
        assertTrue(bids.size() == 10);
        bids.stream().forEach(bid -> System.out.println(bid));

        List<Quotation> asks = data.getAsks();
        assertTrue(asks.size() == 10);
        asks.stream().forEach(ask -> System.out.println(ask));
    }
}
