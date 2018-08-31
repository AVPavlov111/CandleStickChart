package core;

import java.util.Properties;

public interface IConsumerSettingsService {
    Properties getConsumerProperties();
    String getInputTopic();
    String getOutputTopic();
    int aggregationPeriodInMinutes();
    String getWindowStoreName();
}
