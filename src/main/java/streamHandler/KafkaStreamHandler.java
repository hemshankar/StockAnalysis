package streamHandler;

import data.Quote;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import util.PropertyKeys;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class KafkaStreamHandler implements StreamHandler{

    //Configurable at stockAnalyzer.properties
    public static long POLL_TIME_OUT_MILLISECONDS = 1000 * 60 * 60;
    public static String TYPE = PropertyKeys.KAFKA;
    KafkaConsumer<String, String> kafkaConsumer = null;

    public static StreamHandler getKafkaStreamHandler(){
        return new KafkaStreamHandler();
    }

    @Override
    public void initialize(String configFilename, String streamID) throws StreamingException {

        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(configFilename);
        try {
            prop.load(stream);
            kafkaConsumer = new KafkaConsumer(prop);
            kafkaConsumer.subscribe(Arrays.asList(streamID));
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public Optional<Quote> readQuote() throws StreamingException {

        ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(POLL_TIME_OUT_MILLISECONDS));
        if (records.isEmpty()) {
            System.out.println("Received Empty record");
            return Optional.empty(); //this can be handled in a better way, have a nul object or use optional
        }

        //we are expecting only 1 message in the topic.
        // If multiple of them comes, then this needs to be handled in a bit different way
        // like having local buffer and store and send values on every call to read.
        for(ConsumerRecord<String, String>record : records){
            System.out.println("Read key: " + record.key() + ", value: " + record.value());
            try{
                return Optional.of(Quote.FromJson(record.value()));
            }catch (Exception e){
                throw new StreamingException(e);
            }
        }

        return Optional.empty();
    }

    public void cleanUp(){
        kafkaConsumer.close();
    }
}
