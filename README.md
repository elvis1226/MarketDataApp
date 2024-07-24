# MarketDataApp

# Summary
This program is to extract the tick data from kraken via websocket API

## High Level Flow
1. DataExtractor thread will subcribe the snapshot book from kraken and save to in-memory queue
2. DataProcessor thread will take the message from in-memory queue and aggregate to generate 1min candlestick
3. 1min candlestick will be output in log file


# Environment
1. JDK22
2. Maven 3.x or 3.8.4

# Build
The maven build will generate a fat jar with all dependence via shade plugin
> mvn clean install

# Run
1. Run as below command for multi instrument and one period only.
> java -cp Application-1.0-SNAPSHOT.jar org.dgf.App "BTC/USD,XBT/USD" 30

**NB**
Sample log output pls refer to App.log

# Outstanding Issue
1. No clear logic to handle the case when the highest bid price bigger than lowest ask price
2. currently only subscribe 10 best order books as default



