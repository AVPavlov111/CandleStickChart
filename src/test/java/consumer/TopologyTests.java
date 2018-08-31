package consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import core.CandleStick;
import core.IConsumerSettingsService;
import core.Serializers.JsonPOJODeserializer;
import core.StockTransaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.*;

import java.util.*;

public class TopologyTests {
    private static final double DELTA = 1e-15;
    private TopologyTestDriver testDriver;
    private ObjectMapper jsonObjectMapper;
    private IConsumerSettingsService settingsService;
    private Deserializer<CandleStick> candleStickDeserializer;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestsModules());
        settingsService = injector.getInstance(IConsumerSettingsService.class);
        jsonObjectMapper = new ObjectMapper();

        Map<String, Object> serdeProps = new HashMap<>();

        candleStickDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", CandleStick.class);
        candleStickDeserializer.configure(serdeProps, false);

        final Topology topology = StockTransactionTopologyCreator.Create(settingsService);
        Properties props = settingsService.getConsumerProperties();
        testDriver = new TopologyTestDriver(topology, props);
    }

    @After
    public void tearDown() {
        try {
            //throws DirectoryNotEmptyException
            testDriver.close();
        }
        catch (Exception ignored) {

        }
    }

    @Test
    public void shouldAggregateOneTransaction()  {
        StockTransaction stockTransaction = new StockTransaction(1.0, 1, 1);

        sendTransaction(stockTransaction);

        CandleStick candleStick = readOutput();

        Assert.assertEquals(stockTransaction.getTimestamp(), candleStick.getStartAggregationPeriodTimestamp());
        Assert.assertEquals(stockTransaction.getTimestamp(), candleStick.getEndAggregationPeriodTimestamp());
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getStartPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getEndPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getHighPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getLowPrice(), DELTA);
        Assert.assertEquals(1, candleStick.getAggregationCount(), DELTA);
    }

    @Test
    public void shouldAggregateMultipleTransactions()  {
        StockTransaction firstTransaction = new StockTransaction(2.0, 1, 1);
        StockTransaction lastTransaction = new StockTransaction(3.0, 1, 4);
        StockTransaction minPriceTransaction = new StockTransaction(1.0, 1, 2);
        StockTransaction maxPriceTransaction = new StockTransaction(4.0, 1, 3);

        List<StockTransaction> transactions = new LinkedList<>();
        transactions.add(firstTransaction);
        transactions.add(lastTransaction);
        transactions.add(minPriceTransaction);
        transactions.add(maxPriceTransaction);

        CandleStick candleStick = null;
        for (StockTransaction transaction : transactions) {
            sendTransaction(transaction);

            candleStick =  readOutput();
        }

        Assert.assertNotNull(candleStick);
        Assert.assertEquals(firstTransaction.getTimestamp(), candleStick.getStartAggregationPeriodTimestamp());
        Assert.assertEquals(lastTransaction.getTimestamp(), candleStick.getEndAggregationPeriodTimestamp());
        Assert.assertEquals(firstTransaction.getUnitPrice(), candleStick.getStartPrice(), DELTA);
        Assert.assertEquals(lastTransaction.getUnitPrice(), candleStick.getEndPrice(), DELTA);
        Assert.assertEquals(maxPriceTransaction.getUnitPrice(), candleStick.getHighPrice(), DELTA);
        Assert.assertEquals(minPriceTransaction.getUnitPrice(), candleStick.getLowPrice(), DELTA);
        Assert.assertEquals(4, candleStick.getAggregationCount(), DELTA);
    }

    @Test
    public void shouldAggregateToDifferentCandleSticksIfTimestampsDifferMoreThanWindow()  {
        sendTransaction(new StockTransaction(1.0, 1, 1));
        sendTransaction(new StockTransaction(1.0, 1,
                settingsService.aggregationPeriodInMinutes() * 60 * 1000));

        CandleStick firstCandleStick =  readOutput();
        CandleStick lastCandleStick =  readOutput();

        Assert.assertNotEquals(firstCandleStick.getId(), lastCandleStick.getId());
    }

    @Test
    public void shouldAggregateToOneCandleStickIfTimestampsDifferLessThanWindow()  {
        sendTransaction(new StockTransaction(1.0, 1, 1));
        sendTransaction(new StockTransaction(1.0, 1,
                 settingsService.aggregationPeriodInMinutes() * 60 * 1000 - 1));

        CandleStick firstCandleStick =  readOutput();
        CandleStick lastCandleStick =  readOutput();

        Assert.assertEquals(firstCandleStick.getId(), lastCandleStick.getId());
    }

    @Test
    public void shouldAggregateInCorrectOrderByTimeStamp()  {
        sendTransaction(new StockTransaction(1.0, 1, 1));
        sendTransaction(new StockTransaction(1.0, 1,
                settingsService.aggregationPeriodInMinutes() * 60 * 1000 + 10));
        sendTransaction(new StockTransaction(1.0, 1, 2));

        CandleStick candleStick1 =  readOutput();
        CandleStick candleStick2 =  readOutput();
        CandleStick candleStick3 =  readOutput();

        Assert.assertEquals(candleStick1.getId(), candleStick3.getId());
        Assert.assertNotEquals(candleStick1.getId(), candleStick2.getId());
    }

    private CandleStick readOutput() {
        return testDriver
                .readOutput(settingsService.getOutputTopic(), new StringDeserializer(),
                        candleStickDeserializer).value();
    }

    private void sendTransaction(StockTransaction transaction) {
        JsonNode stockTransactionJson = jsonObjectMapper.valueToTree(transaction);
        ConsumerRecordFactory<String, JsonNode> recordFactory = new ConsumerRecordFactory<>(new StringSerializer(), new JsonSerializer());
        ConsumerRecord<byte[], byte[]> rec = recordFactory.create(settingsService.getInputTopic(), null, stockTransactionJson, transaction.getTimestamp());
        testDriver.pipeInput(rec);
    }
}
