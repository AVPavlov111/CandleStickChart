package consumer;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import core.IConsumerSettingsService;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import java.util.concurrent.CountDownLatch;


public class StockTransactionConsumer {
    private final KafkaStreams streams;

    @Inject
    public StockTransactionConsumer(final IConsumerSettingsService settingsService) {
        final Topology topology = StockTransactionTopologyCreator.Create(settingsService);
        streams = new KafkaStreams(topology, settingsService.getConsumerProperties());
    }

    public void start() {
        streams.start();
    }

    public void stop() {
        streams.close();
    }

    public static void main(String[] args){
        Injector injector = Guice.createInjector(new ConsumerModules());
        StockTransactionConsumer consumer = injector.getInstance(StockTransactionConsumer.class);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                consumer.stop();
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);

        try {
            consumer.start();
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
