package consumer;

import core.CandleStick;
import core.IConsumerSettingsService;
import core.Serializers.JsonPOJOSerdesFactory;
import core.StockTransaction;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;

import java.util.concurrent.TimeUnit;

public class StockTransactionTopologyCreator {
    public static Topology Create(final IConsumerSettingsService settingsService) {
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(settingsService.getInputTopic(),
                Consumed.with(Serdes.String(), JsonPOJOSerdesFactory.getJsonPOJOSerdes(StockTransaction.class)))
                .selectKey((s, stockTransaction) -> "any_key")
                .groupByKey( Serialized.with(Serdes.String(), JsonPOJOSerdesFactory.getJsonPOJOSerdes(StockTransaction.class)))
                .windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(settingsService.aggregationPeriodInMinutes())))
                .aggregate( CandleStick::new,
                        (s, stockTransaction, candleStick) -> {
                            candleStick.aggregateTransaction(stockTransaction);
                            return candleStick;
                        },
                        Materialized.with(Serdes.String(), JsonPOJOSerdesFactory.getJsonPOJOSerdes(CandleStick.class))
                ).toStream()
                .to(settingsService.getOutputTopic(), Produced.with(getStringWindowedSerde(), JsonPOJOSerdesFactory.getJsonPOJOSerdes(CandleStick.class)));

        return builder.build();
    }

    private static Serde<Windowed<String>> getStringWindowedSerde() {
        StringSerializer stringSerializer = new StringSerializer();
        StringDeserializer stringDeserializer = new StringDeserializer();
        WindowedSerializer<String> windowedSerializer = new TimeWindowedSerializer<>(stringSerializer);
        TimeWindowedDeserializer<String> windowedDeserializer = new TimeWindowedDeserializer<>(stringDeserializer);
        return Serdes.serdeFrom(windowedSerializer,windowedDeserializer);
    }
}
