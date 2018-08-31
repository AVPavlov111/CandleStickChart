package producer;

import core.IStockTransactionService;
import core.StockTransaction;

import java.util.Random;

public class RandomStockTransactionGenerator implements IStockTransactionService {
    private static int maxTransactionAmount = 10;
    private static int maxQuantityOfGoods = 10;
    public StockTransaction GetStockTransaction() {
        Random rnd = new Random(System.currentTimeMillis());
        double transactionAmount = rnd.nextDouble() + rnd.nextInt(maxTransactionAmount);
        int quantityOfGoods = 1 + rnd.nextInt(maxQuantityOfGoods - 1);
        return new StockTransaction(transactionAmount, quantityOfGoods, System.currentTimeMillis());
    }
}
