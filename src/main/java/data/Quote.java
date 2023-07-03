package data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Quote {

    public static Quote FromJson(String jsonStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Quote quote = mapper.readValue(jsonStr, Quote.class);
        return quote;
    }

    public Quote(double price, long timestamp) {
        this.price = price;
        this.timestamp = timestamp;
    }

    @JsonProperty("price")
    public double price;
    @JsonProperty("timestamp")
    public long timestamp;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
