package org.izolotov.kafka;

import com.google.common.primitives.Longs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class App {

    private static final Logger LOG = Logger.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        Clock utcClock = Clock.systemUTC();
        Map<String, Long> keyCounts = new HashMap<>();
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getenv("BOOTSTRAP_SERVERS"));
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.location", "kafka.client.truststore.jks");
        props.put("group.id", System.getenv("CONSUMER_GROUP"));
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(System.getenv("TOPIC_NAME")));
        while (true) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Integer.parseInt(System.getenv("POLL_WINDOW_MILLISECONDS")));
            int sum = 0;
            int received = 0;
            for (ConsumerRecord<String, byte[]> record : records) {
                try {
                    Long expectedCounterValue = keyCounts.getOrDefault(record.key(), 0L);
                    Long actualCounterValue = Longs.fromByteArray(Arrays.copyOfRange(record.value(), 0, 8));
                    Long produceEpoch = Longs.fromByteArray(Arrays.copyOfRange(record.value(), 8, 16));
                    Long consumeEpoch = utcClock.millis();
                    Long delay = consumeEpoch - produceEpoch;
                    sum+=delay;
//                    LOG.info(String.format("New record consumed. Key = %s, counter value = %d, delay = %d", record.key(), actualCounterValue, delay));
                    if (actualCounterValue > expectedCounterValue) {
                        LOG.warn(String.format(
                                "Missing value spotted. Expected counter value = %d, actual counter value = %d", expectedCounterValue, actualCounterValue));
                    } else if (actualCounterValue < expectedCounterValue) {
                        LOG.warn(String.format(
                                "Duplicate value spotted. Expected counter value = %d, actual counter value = %d", expectedCounterValue, actualCounterValue));
                    }
                    keyCounts.put(record.key(), actualCounterValue + 1);
                    received++;
                } catch (Exception exc) {
                    LOG.warn("Can't handle message: " + exc.toString());
                }
            }
            if (received != 0) {
                double meanDelay = sum / received;
                LOG.info(System.getenv("TOPIC_NAME") + " MEAN_DELAY_MILLISECONDS " + meanDelay);
            }

        }
    }


}
