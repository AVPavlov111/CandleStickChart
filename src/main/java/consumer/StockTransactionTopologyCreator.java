package consumer;

import core.CandleStick;
import core.IConsumerSettingsService;
import core.JsonPOJOSerdesFactory;
import core.StockTransaction;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.apache.kafka.streams.state.WindowStore;

import java.util.concurrent.TimeUnit;

public class StockTransactionTopologyCreator {
    public static Topology Create(final IConsumerSettingsService settingsService) {
        final StreamsBuilder builder = new StreamsBuilder();

        KGroupedStream<String, StockTransaction> gr = builder.stream(settingsService.getInputTopic(),
                Consumed.with(Serdes.String(), JsonPOJOSerdesFactory.getJsonPOJOSerdes(StockTransaction.class)))
                .selectKey((s, stockTransaction) -> "any_key")
                .groupByKey( Serialized.with(
                        Serdes.String(),
                        JsonPOJOSerdesFactory.getJsonPOJOSerdes(StockTransaction.class)));

        StringSerializer stringSerializer = new StringSerializer();
        StringDeserializer stringDeserializer = new StringDeserializer();
        WindowedSerializer<String> windowedSerializer = new TimeWindowedSerializer<>(stringSerializer);
        TimeWindowedDeserializer<String> windowedDeserializer = new TimeWindowedDeserializer<>(stringDeserializer);
        Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(windowedSerializer,windowedDeserializer);

        gr.windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(settingsService.aggregationPeriodInMinutes())))
                .aggregate( CandleStick::new,
                        (s, stockTransaction, candleStick) -> {
                            candleStick.aggregateTransaction(stockTransaction);
                            return candleStick;
                        },
                        Materialized.<String, CandleStick, WindowStore<Bytes, byte[]>>as(settingsService.getWindowStoreName())
                                .withValueSerde(JsonPOJOSerdesFactory.getJsonPOJOSerdes(CandleStick.class))
                ).toStream()
                .to(settingsService.getOutputTopic(), Produced.with(windowedSerde, JsonPOJOSerdesFactory.getJsonPOJOSerdes(CandleStick.class)));

        return builder.build();
    }
}
