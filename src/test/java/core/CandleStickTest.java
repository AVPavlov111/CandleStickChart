package core;

import org.junit.Assert;
import org.junit.Test;

public class CandleStickTest {
    private static final double DELTA = 1e-15;
    @Test
    public void shouldAggregateFirstTransaction() {
        CandleStick candleStick = new CandleStick();

        StockTransaction stockTransaction = new StockTransaction(1.0, 1, 1 );

        candleStick.aggregateTransaction(stockTransaction);

        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getStartPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getEndPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getHighPrice(), DELTA);
        Assert.assertEquals(stockTransaction.getUnitPrice(), candleStick.getLowPrice(), DELTA);
    }

    @Test
    public void shouldAggregateMultipleTransactions() {
        CandleStick candleStick = new CandleStick();

        StockTransaction firstStockTransaction = new StockTransaction(2.0, 1, 1 );
        StockTransaction minStockTransaction = new StockTransaction(1.0, 1, 2 );
        StockTransaction maxStockTransaction = new StockTransaction(3.0, 1, 3 );
        StockTransaction endStockTransaction = new StockTransaction(3.0, 1, 4 );


        candleStick.aggregateTransaction(firstStockTransaction);
        candleStick.aggregateTransaction(minStockTransaction);
        candleStick.aggregateTransaction(maxStockTransaction);

        Assert.assertEquals(firstStockTransaction.getUnitPrice(), candleStick.getStartPrice(), DELTA);
        Assert.assertEquals(endStockTransaction.getUnitPrice(), candleStick.getEndPrice(), DELTA);
        Assert.assertEquals(maxStockTransaction.getUnitPrice(), candleStick.getHighPrice(), DELTA);
        Assert.assertEquals(minStockTransaction.getUnitPrice(), candleStick.getLowPrice(), DELTA);
    }
}