package org.dgf.core;

import com.google.common.base.Preconditions;

import org.dgf.json.JsonParser;
import org.dgf.json.Quotation;
import org.dgf.json.Snapshot;
import org.dgf.json.TickUpdate;
import org.dgf.kafka.KafkaProducerClient;
import org.dgf.util.DateUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class DataProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessor.class);

    private final BlockingQueue<Message> queue;
    private volatile boolean isDone = false;
    private final JsonParser jsonParser;
    private List<OrderBook> orderBook;
    private boolean isSnapshotMessage = true;
    private int DEFAULT_ORDER_NUM = 10;
    private KafkaProducerClient kafkaProducerClient;

    public DataProcessor(BlockingQueue<Message> queue) {
        this.queue = queue;
        this.jsonParser = new JsonParser();
        this.orderBook = new ArrayList<>();
        this.kafkaProducerClient = new KafkaProducerClient();
    }

    @Override
    public void run()
    {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        List<TickMidPrice> tickMidPrices =  new ArrayList<>();
        final LocalDateTime startRun = LocalDateTime.now();
        while (!isDone) {
            try {
                if (this.queue.isEmpty()) {
                    continue;
                }

                Message message = this.queue.poll(100, TimeUnit.MILLISECONDS);
                if (!message.isEmpty()) {
                    logger.debug(message.getValue());
                    List<OrderBook> orderBook = parseMessage(message);
                    if (!orderBook.isEmpty()) {
                        if (this.isSnapshotMessage) {
                            List<OrderBook> updated = orderBook.stream().map(x-> new OrderBook(x.getSymbol(), x.getBids(), x.getAsks(), startRun)).toList();
                            this.orderBook.addAll(updated);
                            logger.debug("got snapshot size {}", this.orderBook.size());
                            continue;
                        }
                        LocalDateTime anyTime = orderBook.get(0).getTimestamp();
                        if(startTime == null) {
                            startTime = anyTime;
                            endTime = startTime.plus(60, ChronoUnit.SECONDS);
                            logger.debug("init Start {}, End {}", startTime, endTime);
                        }

                        if (anyTime.isAfter(endTime)) {
                            logger.debug("Stop current aggregate at Start {}, End {}, Now {}", startTime, endTime, anyTime);
                            startTime = anyTime;
                            endTime = startTime.plus(60, ChronoUnit.SECONDS);
                            logger.debug("Set next Start {}, End {}", startTime, endTime);
                            aggregate(tickMidPrices);
                            tickMidPrices.clear();
                        }
                        updateOrderBook(orderBook, tickMidPrices);
                        Thread.sleep(1);
                    }
                }
            }
            catch (Exception e) {
                this.isDone = true;
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        logger.info("DataProcessor exited");
    }

    public void complete() {
        this.isDone = true;
    }

    private void aggregate(List<TickMidPrice> midPrices) {
        if (midPrices.isEmpty()) {
            logger.warn("nothing to aggregate as no mid price list exit");
            return;
        }
        Map<String, List<TickMidPrice>> grouped = midPrices.stream().collect(Collectors.groupingBy(TickMidPrice::getSymbol));
        List<CandleStick> sticks = grouped.entrySet().stream().map(entry -> {
            var sortByTime = entry.getValue().stream().sorted( (x, y) -> x.getTimestamp().compareTo(y.getTimestamp())).toList();
            var sortByPrice = entry.getValue().stream().sorted( (x, y) -> Double.compare(x.getMid(), y.getMid())).toList();
            var size = entry.getValue().size();
            var stick = new CandleStick(entry.getKey(),
                                        sortByTime.get(0).getMid(),
                                        sortByPrice.get(sortByPrice.size()-1).getMid(),
                                        sortByPrice.get(0).getMid(),
                                        sortByTime.get(sortByTime.size()-1).getMid(),
                                        sortByTime.get(0).getTimestamp(),
                                        size);
           return stick;
        }).toList();
        sticks.forEach(x -> {
            var uuid = String.valueOf(UUID.randomUUID());
            logger.info("{} - {}", uuid, x.toString());
            kafkaProducerClient.send(uuid, x.toString() );
        });
    }

    private List<Quotation> processQuote(List<Quotation> current, List<Quotation> newQuote)
    {
        if (newQuote.isEmpty()) {
            return current;
        }
        List<Quotation> result = new ArrayList<>();
        var newPriceMap = newQuote.stream().collect(Collectors.toMap(Quotation::getPrice, Quotation::getQuantity, (x, y) -> x+y));
        var currentPriceMap = current.stream().collect(Collectors.toMap(Quotation::getPrice, Quotation::getQuantity, (x, y) -> x+y));

        var updated = current.stream().map(c -> {
                                            if (newPriceMap.containsKey(c.getPrice())) {
                                                return new Quotation(c.getPrice(), newPriceMap.get(c.getPrice()));
                                            }
                                            else {
                                                return c;
                                            }
                                         }).toList();

        var added = newQuote.stream().filter(n -> !currentPriceMap.containsKey(n.getPrice())).toList();
        result.addAll(updated);
        result.addAll(added);

        return result;
    }

    private OrderBook processNewBook(List<OrderBook> current,
                                     OrderBook newBook,
                                     List<Double> newAsksZeroQnt,
                                     List<Double> newBidsZeroQnt) {
        if (newBook.getAsks().isEmpty() && newBook.getBids().isEmpty()) {
            logger.warn ("both asks and bids is empty in new book");
            return null;
        }
        logger.debug("origin book size {}, new ask zero {}, new bid zero {}", current.size(), newAsksZeroQnt.size(), newBidsZeroQnt.size());
        logger.debug("zero ask {}", newAsksZeroQnt);
        logger.debug("zero bid {}", newBidsZeroQnt);
        List<Quotation> nonZeroQntAsks = newBook.getAsks().stream().filter(x -> !newAsksZeroQnt.contains(x.getPrice())).toList();
        List<Quotation> nonZeroQntBids = newBook.getBids().stream().filter(x -> !newBidsZeroQnt.contains(x.getPrice())).toList();
        logger.debug("nonZeroQntAsks {}, nonZeroQntBids {}", nonZeroQntAsks, nonZeroQntBids);

        List<OrderBook> result = current.stream().map(x->  {
                                  var asks = processQuote(x.getAsks().stream().filter(a -> !newAsksZeroQnt.contains(a.getPrice())).toList(), nonZeroQntAsks);
                                  var bids = processQuote(x.getBids().stream().filter(a -> !newBidsZeroQnt.contains(a.getPrice())).toList(), nonZeroQntBids);
                                  return buildFinalOrderBook(newBook.getSymbol(), asks, bids, newBook.getTimestamp());
                                }
                        ).toList();
        Preconditions.checkArgument(result.size() == 1, "Wrong size of the orderbook");
        return result.get(0);
    }

    private TickMidPrice updateMidPrice(OrderBook orderBook) {
        var asks = orderBook.getAsks().stream().map(Quotation::getPrice).sorted().toList();
        var bids = orderBook.getBids().stream().map(Quotation::getPrice).sorted(Comparator.reverseOrder()).toList();
        var highestBid = bids.get(0);
        var lowestAsk = asks.get(0);
        var symbol = orderBook.getSymbol();
        var timestamp = orderBook.getTimestamp();
        if (lowestAsk < highestBid) {
            //logger.warn ("observed violation lowest ask less than highest bid :  ask {} < bid {}", lowestAsk, highestBid);
            //return null;
        }
        var mid = (highestBid+lowestAsk)/2;
        return new TickMidPrice(symbol, mid, timestamp);
    }

    private OrderBook buildFinalOrderBook(String symbol, List<Quotation> asks, List<Quotation> bids, LocalDateTime timestamp) {
        var sortedAsks = asks.stream().sorted((x, y)-> Double.compare(x.getPrice(), y.getPrice())).toList();
        var sortedBids = bids.stream().sorted((x, y)-> Double.compare(y.getPrice(), x.getPrice())).toList();

        /*
        int i = 0, j = 0;
        while(sortedAsks.size() > DEFAULT_ORDER_NUM && sortedBids.size() > DEFAULT_ORDER_NUM) {
            var highestBid = sortedBids.get(0).getPrice();
            var lowestAsk = sortedAsks.get(0).getPrice();
            if (lowestAsk < highestBid) {
                if (sortedAsks.size() > DEFAULT_ORDER_NUM) {
                    sortedAsks.remove(0);
                }
                else if (sortedBids.size() > DEFAULT_ORDER_NUM) {
                    sortedBids.remove(0);
                }
                else {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        */
        var orderedAsks = sortedAsks.stream().limit(DEFAULT_ORDER_NUM).toList();
        var orderedBids = sortedBids.stream().limit(DEFAULT_ORDER_NUM).toList();

        logger.debug("orderedAsks {}", orderedAsks);
        logger.debug("orderedBids {}", orderedBids);

        return new OrderBook(symbol, orderedAsks, orderedBids, timestamp);
    }

    private void updateOrderBook(List<OrderBook> newOrderBook, List<TickMidPrice> midPrices)
    {
        if(newOrderBook.isEmpty()) {
            return ;
        }
        logger.debug("new {}", newOrderBook);
        logger.debug("current {}", this.orderBook);
        var current = this.orderBook;
        var adjusted = newOrderBook.stream()
                                  .map(x -> processNewBook(current.stream().filter(y -> y.getSymbol().equals(x.getSymbol())).toList(),
                                                          x,
                                                          x.getAsks().stream().filter(ask-> Double.compare(ask.getQuantity(), 0) == 0).map(y->y.getPrice()).toList(),
                                                          x.getBids().stream().filter(bid-> Double.compare(bid.getQuantity(), 0) == 0).map(y->y.getPrice()).toList()
                                                         )
                                      )
                                  .filter( x-> x != null)
                                  .toList();
        var updatedSymbols = adjusted.stream().map(OrderBook::getSymbol).toList();
        var existed = this.orderBook.stream().filter(x -> ! updatedSymbols.contains(x.getSymbol())).toList();
        this.orderBook.clear();
        if (!existed.isEmpty()) {
            this.orderBook.addAll(existed);
        }
        this.orderBook.addAll(adjusted);

        var newMidprice = this.orderBook.stream().map(x -> updateMidPrice((x))).filter( x-> x!= null).toList();
        if (newMidprice.size() > 0) {
            midPrices.addAll(newMidprice);
        }
    }

    private List<OrderBook> parseMessage(Message message)
    {
        Optional<Snapshot> snapshot = Optional.empty();
        Optional<TickUpdate> tickUpdate = Optional.empty();

        if (message.getType().equals("snapshot")) {
            snapshot = this.jsonParser.parseSnapshot(message.getValue());
            this.isSnapshotMessage = true;
            return snapshot.map(x-> buildOrderBookFromSnapshot(x)).orElse(List.of());
        }
        else if (message.getType().equals("update")) {
            tickUpdate = this.jsonParser.parseTickUpdate(message.getValue());
            this.isSnapshotMessage = false;
            return tickUpdate.map(x -> buildOrderBookFromUpdate(x)).orElse(List.of());
        }

        return List.of();
    }

    private List<OrderBook> buildOrderBookFromSnapshot(Snapshot snapshot)
    {
        LocalDateTime now = LocalDateTime.now();
        List<Snapshot.Data> data = snapshot.getData();
        return  data.stream()
                .map(x -> new OrderBook(x.getSymbol(), x.getBids(), x.getAsks(), null))
                .collect(Collectors.toList());
    }

    private List<OrderBook> buildOrderBookFromUpdate(TickUpdate tickUpdate)
    {
        List<TickUpdate.Data> data = tickUpdate.getData();
        return  data.stream()
                .map(x -> new OrderBook(x.getSymbol(), x.getBids(), x.getAsks(), DateUtility.parse(x.getTimestamp())))
                .collect(Collectors.toList());
    }


    public static class TickMidPrice
    {
        private String symbol;
        private double mid;
        private LocalDateTime timestamp;

        public TickMidPrice(String symbol, double mid, LocalDateTime timestamp) {
            this.symbol = symbol;
            this.mid = mid;
            this.timestamp = timestamp;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getMid() {
            return mid;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }


}
