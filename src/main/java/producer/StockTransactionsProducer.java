package producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import core.IStockTransactionService;
import core.StockTransaction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class StockTransactionsProducer {
    private final KafkaProducer<String, JsonNode> producer;
    private final String topic;
    private final IStockTransactionService stockTransactionService;
    private final ObjectMapper jsonObjectMapper;

    @Inject
    public StockTransactionsProducer(IStockTransactionService stockTransactionService, IProducerSettingsService settingsService) {
        this.producer = new KafkaProducer<>(settingsService.getProducerProperties());
        this.topic = settingsService.getTopic();
        this.stockTransactionService = stockTransactionService;
        jsonObjectMapper = new ObjectMapper();
    }

    public void SendNextTransaction() {
        StockTransaction stockTransaction = stockTransactionService.GetStockTransaction();
        JsonNode stockTransactionJson = jsonObjectMapper.valueToTree(stockTransaction);
        ProducerRecord<String, JsonNode> rec = new ProducerRecord<>(topic, null, stockTransaction.getTimestamp(), null, stockTransactionJson);
        producer.send(rec);
    }

    public static void main(String[] args){
        Injector injector = Guice.createInjector(new ProducerModules());
        StockTransactionsProducer producer = injector.getInstance(StockTransactionsProducer.class);

        for (int i = 0; i < 100; i++) {
            producer.SendNextTransaction();
        }
    }
}
