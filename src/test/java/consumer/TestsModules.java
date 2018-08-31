package consumer;

import Stubs.TestConsumerSettingsService;
import com.google.inject.AbstractModule;
import core.IConsumerSettingsService;

public class TestsModules extends AbstractModule {
    @Override
    protected void configure() {
        bind(IConsumerSettingsService.class).to(TestConsumerSettingsService.class);
    }
}
