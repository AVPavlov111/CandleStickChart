package producer;

import java.util.Properties;

public interface IProducerSettingsService {
    Properties getProducerProperties();
    String getTopic();
}
