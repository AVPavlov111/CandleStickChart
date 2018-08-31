package consumer;

import com.google.inject.AbstractModule;
import core.IConsumerSettingsService;

public class ConsumerModules extends AbstractModule {
    @Override
    protected void configure() {
        bind(IConsumerSettingsService.class).to(ConsumerSettingsService.class);

        bind(StockTransactionConsumer.class);
    }
}
