package core.Serializers;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class JsonPOJOSerdesFactory {
    public static <T> Serde<T> getJsonPOJOSerdes(Class<T> type) {
        Map<String, Object> serdeProps = new HashMap<>();

        final Serializer<T> serializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", type);
        serializer.configure(serdeProps, false);

        final Deserializer<T> deserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", type);
        deserializer.configure(serdeProps, false);

        return Serdes.serdeFrom(serializer, deserializer);
    }
}
