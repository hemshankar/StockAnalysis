package stockAnalyzer;

public interface StockAnalyzer {

    public void initialize(String configFile) throws StockAnalyzerException;
    public void analyze() throws StockAnalyzerException;

}
