package org.dgf;

import com.google.common.collect.ImmutableList;
import org.dgf.core.DataExtractor;
import org.dgf.core.DataProcessor;
import org.dgf.core.DataResult;
import org.dgf.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * Application
 *
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String USAGE =
                    "1st argv : list of currency pairs with ',' separator \n" +
                    "2nd argv : execution duration in minutes" +
                    "e.g \n" +
                    "java org.dgf.App <BTC/USD,XBT/USD...> 30 \n";
    private static final String wssHost = "wss://ws.kraken.com/v2";
    private static final int MAX_QUEUE_NUM = 10000;

    public static void main( String[] args ) throws InterruptedException {
       // System.setProperty("javax.net.debug", "all");

        if (args.length < 2) {
            throw new IllegalArgumentException("Missing param, " + USAGE);
        }
        DataProcessor processor = null;
        DataExtractor extractor = null;

        try {
            logger.info("start ...");
            int duration = Integer.parseInt(args[1]);
            String[] arguments = args[0].split(",");
            List<String> pairs = ImmutableList.copyOf(arguments);

            logger.info("execution duration {} mins", duration);
            logger.info("currency pairs : {}", pairs);

            final BlockingQueue<Message> queue = new LinkedBlockingQueue(MAX_QUEUE_NUM);

            extractor = new DataExtractor(wssHost, pairs, queue);
            new Thread(extractor).start();

            processor = new DataProcessor(queue);
            new Thread(processor).start();

            //sleep until reach the duration
            Thread.sleep(duration * 60 * 1000);
            System.exit(0);
        }
        catch(Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (processor != null) {
                processor.complete();
            }
            if (extractor != null) {
                extractor.complete();
            }
        }
    }
}
