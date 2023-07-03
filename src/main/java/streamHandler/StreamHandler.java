package streamHandler;

import data.Quote;

import java.util.Optional;

public interface StreamHandler {

    void initialize(String configFilename, String streamID) throws StreamingException;
    Optional<Quote> readQuote() throws StreamingException;

    void cleanUp();

}
