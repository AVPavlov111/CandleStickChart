package producer;

import com.google.inject.AbstractModule;
import core.IStockTransactionService;

public class ProducerModules extends AbstractModule {
    @Override
    protected void configure() {
        bind(IStockTransactionService.class).to(RandomStockTransactionGenerator.class);
        bind(IProducerSettingsService.class).to(ProducerSettingsService.class);

        bind(StockTransactionsProducer.class);
    }
}
