package org.dgf.json;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TickUpdateTest {

    @Test
    public void NormalTickUpdateJson_ReturnExpectedResult() {
        final String json = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\"," +
                "\"bids\":[{\"price\":64359.4,\"qty\":1.55377409}]," +
                "\"asks\":[{\"price\":64359.4,\"qty\":1.55377409}]," +
                "\"checksum\":2450216282,\"timestamp\":\"2024-07-19T12:50:10.319287Z\"}]}";
        final JsonParser utility = new JsonParser();
        Optional<TickUpdate> update = utility.parseTickUpdate(json);
        assertTrue(update.isPresent());
        assertEquals("book", update.get().getChannel());
        assertEquals("update", update.get().getType());
        assertTrue(update.get().getData().size() > 0);

        TickUpdate.Data data = update.get().getData().get(0);
        assertEquals("2450216282", data.getChecksum());
        assertEquals("2024-07-19T12:50:10.319287Z", data.getTimestamp());

        assertTrue(data.getAsks().size() == 1);
        Quotation ask = data.getAsks().get(0);
        assertTrue(Double.compare(64359.4, ask.getPrice()) == 0);
        assertEquals(1.55377409, ask.getQuantity());
        assertTrue(data.getBids().size() == 1);
        Quotation bid = data.getBids().get(0);
        assertTrue(Double.compare(64359.4, bid.getPrice()) == 0);
        assertEquals(1.55377409, bid.getQuantity());
    }

    @Test
    public void EmptyAskInTickUpdateJson_ReturnEmptyAsks() {
        final String json = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\"," +
                "\"bids\":[{\"price\":64359.4,\"qty\":1.55377409}]," +
                "\"asks\":[]," +
                "\"checksum\":2450216282,\"timestamp\":\"2024-07-19T12:50:10.319287Z\"}]}";
        final JsonParser utility = new JsonParser();
        Optional<TickUpdate> update = utility.parseTickUpdate(json);
        assertTrue(update.isPresent());
        assertEquals("book", update.get().getChannel());
        assertEquals("update", update.get().getType());
        assertTrue(update.get().getData().size() > 0);

        TickUpdate.Data data = update.get().getData().get(0);
        assertEquals("2450216282", data.getChecksum());
        assertEquals("2024-07-19T12:50:10.319287Z", data.getTimestamp());

        assertTrue(data.getAsks().size() == 0);

        assertTrue(data.getBids().size() == 1);
        Quotation bid = data.getBids().get(0);
        assertTrue(Double.compare(64359.4, bid.getPrice()) == 0);
        assertEquals(1.55377409, bid.getQuantity());
    }

    @Test
    public void EmptyBidInTickUpdateJson_ReturnEmptyBids() {
        final String json = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\"," +
                "\"bids\":[]," +
                "\"asks\":[{\"price\":64359.4,\"qty\":1.55377409}]," +
                "\"checksum\":2450216282,\"timestamp\":\"2024-07-19T12:50:10.319287Z\"}]}";
        final JsonParser utility = new JsonParser();
        Optional<TickUpdate> update = utility.parseTickUpdate(json);
        assertTrue(update.isPresent());
        assertEquals("book", update.get().getChannel());
        assertEquals("update", update.get().getType());
        assertTrue(update.get().getData().size() > 0);

        TickUpdate.Data data = update.get().getData().get(0);
        assertEquals("2450216282", data.getChecksum());
        assertEquals("2024-07-19T12:50:10.319287Z", data.getTimestamp());

        assertTrue(data.getAsks().size() == 1);
        Quotation ask = data.getAsks().get(0);
        assertTrue(Double.compare(64359.4, ask.getPrice()) == 0);
        assertEquals(1.55377409, ask.getQuantity());

        assertTrue(data.getBids().size() == 0);
    }
}
