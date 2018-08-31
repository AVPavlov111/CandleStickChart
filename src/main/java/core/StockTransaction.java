package core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class StockTransaction {

    private UUID id;
    private Double transactionAmount;
    private int quantityOfGoods;
    private long timestamp;

    /**
     * Default constructor needed by Kafka
     */
    public StockTransaction() {

    }

    public StockTransaction(Double transactionAmount, int quantityOfGoods, long timestamp) {
        this.transactionAmount = transactionAmount;
        this.quantityOfGoods = quantityOfGoods;
        this.timestamp = timestamp;
        id = UUID.randomUUID();
    }

    @JsonIgnore
    public double getUnitPrice() {
        return transactionAmount / quantityOfGoods;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public int getQuantityOfGoods() {
        return quantityOfGoods;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public void setQuantityOfGoods(int quantityOfGoods) {
        this.quantityOfGoods = quantityOfGoods;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
