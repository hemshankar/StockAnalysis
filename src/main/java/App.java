import stockAnalyzer.SingleStockAnalyzer;
import stockAnalyzer.StockAnalyzer;
import stockAnalyzer.StockAnalyzerException;

public class App {

    public static void main(String[] args) {
        StockAnalyzer stockAnalyzer = new SingleStockAnalyzer();

        try {
            stockAnalyzer.initialize("configFile.properties");
            stockAnalyzer.analyze();
        } catch (StockAnalyzerException e) {
            throw new RuntimeException(e);
        }
    }

}
