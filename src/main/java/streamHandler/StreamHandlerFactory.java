package streamHandler;

import util.PropertyKeys;

import java.util.Optional;

public class StreamHandlerFactory {

    public static Optional<StreamHandler> getStreamHandler(String streamHandler) {
        if (PropertyKeys.KAFKA.equals(streamHandler)) {
            return Optional.of(new KafkaStreamHandler());
        }
        return Optional.empty();
    }
}
