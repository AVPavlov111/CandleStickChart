package consumer;

import core.IConsumerSettingsService;
import core.KafkaProperties;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class ConsumerSettingsService implements IConsumerSettingsService {
    private static final String clientId =  "StockTransactionConsumer_v2";
    private static final String windowSorName =  "windowed-aggregated-stream-store";
    @Override
    public Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, clientId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.KAFKA_SERVER_URL + ":" + KafkaProperties.KAFKA_SERVER_PORT);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        return props;
    }

    @Override
    public String getInputTopic() {
        return KafkaProperties.STOCK_TRANSACTIONS_TOPIC;
    }

    @Override
    public String getOutputTopic() {
        return KafkaProperties.CANDLE_STICKS_TOPIC;
    }

    @Override
    public int aggregationPeriodInMinutes() {
        return 15;
    }

    @Override
    public String getWindowStoreName() {
        return windowSorName;
    }
}
