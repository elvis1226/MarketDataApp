# MarketDataApp

# Summary
This program is to extract the tick data from kraken via websocket API

## High Level Flow
1. DataExtractor thread will subcribe the snapshot book from kraken and save to in-memory queue
2. DataProcessor thread will take the message from in-memory queue and aggregate to generate 1min candlestick
3. KafkaProducerClient will publish the 1min tick message to kafka via topic "market"
4. KafkaConsumerClient thread will consume the message from kafka via topic "market"
5. 1min candlestick will be output in log file
6. 1min candlestick will be published to kafka and consumed once available

# Environment
1. JDK22
2. Maven 3.x or 3.8.4

# Build
The maven build will generate a fat jar with all dependence via shade plugin
> mvn clean install

# Run
1. Run as below command for multi instrument and one period only.
> java -cp Application-1.0-SNAPSHOT-shaded.jar org.dgf.App "BTC/USD,ETH/USD" 30

**NB**
Sample log output pls refer to App.log

# Outstanding
1. Need to handle the case perfectly when the highest bid price is bigger than lowest ask price
2. currently subscribe 10 best order books as default but could be configured if required
3. Coding style and formatting 
4. Unit test



