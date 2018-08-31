package core;

import java.util.UUID;

public class CandleStick {

    private UUID id;
    private double startPrice;
    private double endPrice;
    private double highPrice;
    private double lowPrice;
    private long startAggregationPeriodTimestamp;
    private long endAggregationPeriodTimestamp;
    private long aggregationCount;


    public CandleStick() {
        id = UUID.randomUUID();
    }

    public void aggregateTransaction(StockTransaction stockTransaction) {

        double unitPrice = stockTransaction.getUnitPrice();
        if (aggregationCount == 0) {
            startAggregationPeriodTimestamp = stockTransaction.getTimestamp();
            endAggregationPeriodTimestamp = stockTransaction.getTimestamp();
            startPrice = unitPrice;
            lowPrice = unitPrice;
            endPrice = unitPrice;
        }

        if (startAggregationPeriodTimestamp > stockTransaction.getTimestamp()) {
            startAggregationPeriodTimestamp = stockTransaction.getTimestamp();
            startPrice = unitPrice;
        }

        if (endAggregationPeriodTimestamp < stockTransaction.getTimestamp()) {
            endAggregationPeriodTimestamp = stockTransaction.getTimestamp();
            endPrice = unitPrice;
        }

        highPrice = Math.max(highPrice, unitPrice);
        lowPrice = Math.min(lowPrice, unitPrice);
        ++aggregationCount;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getEndPrice() {
        return endPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public long getStartAggregationPeriodTimestamp() {
        return startAggregationPeriodTimestamp;
    }

    public long getEndAggregationPeriodTimestamp() {
        return endAggregationPeriodTimestamp;
    }

    public long getAggregationCount() {
        return aggregationCount;
    }

    public UUID getId() {
        return id;
    }
}
