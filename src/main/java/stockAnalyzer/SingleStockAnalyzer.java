package stockAnalyzer;

import data.Quote;
import data.SingleStockAnalysisMetadata;
import streamHandler.StreamHandler;
import streamHandler.StreamHandlerFactory;
import streamHandler.StreamingException;
import util.DateTimeUtil;
import util.PropertyKeys;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleStockAnalyzer implements StockAnalyzer{

    AtomicBoolean stopAnalysis = new AtomicBoolean(false);

    SingleStockAnalysisMetadata stockAnalysisMetadata = new SingleStockAnalysisMetadata();

    Properties stockProperties = null;
    StreamHandler buyStreamHandler = null;
    StreamHandler sellStreamHandler = null;

    StockMetadataProcessor metadataProcessor = new StockMetadataProcessor();

    @Override
    public void initialize(String configFile) throws StockAnalyzerException {
        stockProperties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(configFile);
        try{stockProperties.load(stream);}catch(Exception e){throw new StockAnalyzerException(e);}

        if(!stockProperties.contains(PropertyKeys.BUY_STREAM)) throw new StockAnalyzerException(new Exception("No Property for " + PropertyKeys.BUY_STREAM));
        if(!stockProperties.contains(PropertyKeys.SELL_STREAM)) throw new StockAnalyzerException(new Exception("No Property for " + PropertyKeys.SELL_STREAM));

        Optional<StreamHandler> sHandler = StreamHandlerFactory.getStreamHandler(stockProperties.getProperty(PropertyKeys.BUY_STREAM));
        if (sHandler.isEmpty()) throw new StockAnalyzerException(new Exception("No Stream Handler present for " + stockProperties.getProperty(PropertyKeys.BUY_STREAM)));
        buyStreamHandler = sHandler.get();

        sHandler = StreamHandlerFactory.getStreamHandler(stockProperties.getProperty(PropertyKeys.SELL_STREAM));
        if (sHandler.isEmpty()) throw new StockAnalyzerException(new Exception("No Stream Handler present for " + stockProperties.getProperty(PropertyKeys.SELL_STREAM)));
        sellStreamHandler = sHandler.get();

        if(!stockProperties.contains(PropertyKeys.STREAM_CONFIG_FILE)) throw new StockAnalyzerException(new Exception("No Property for " + PropertyKeys.STREAM_CONFIG_FILE));
    }

    @Override
    public void analyze() {
        analyzeInternal();
        while(true) {
            try {Thread.sleep(1000 * 60 * 60);} catch (InterruptedException e) {throw new RuntimeException(e);}
        }
    }

    private void analyzeInternal() {
        LocalDate today = LocalDate.now();
        if(!DateTimeUtil.isWeekEnd(today)){
            if(DateTimeUtil.isMarketUp()){
                resetMetadata();
                Thread t = new Thread(() -> {
                        try {
                            buyStreamHandler.initialize(stockProperties.getProperty(PropertyKeys.STREAM_CONFIG_FILE), stockProperties.getProperty(PropertyKeys.STOCK_ID));
                            sellStreamHandler.initialize(stockProperties.getProperty(PropertyKeys.STREAM_CONFIG_FILE), stockProperties.getProperty(PropertyKeys.STOCK_ID));
                            processBuyAndSell();
                            buyStreamHandler.cleanUp();
                            sellStreamHandler.cleanUp();
                        } catch (StreamingException e) {
                            System.err.println("Error while processing " + new StockAnalyzerException(e));
                        }
                    });
                t.start();
            }

            DateTimeUtil.callAtMarketEnd(new TimerTask() {
                public void run() {
                stopAnalysis.compareAndSet(false, true);
                DateTimeUtil.callAtNextMarketStart(new TimerTask() {
                    @Override
                    public void run() {
                        analyzeInternal();
                    }
                });
            }});
        }
    }

    private void resetMetadata() {
        stockAnalysisMetadata.currentBestBuyValue = 0.0;
        stockAnalysisMetadata.currentBestSellValue = 0.0;
        stockAnalysisMetadata.maxProfit = 0.0;
        stockAnalysisMetadata.currentProfit = 0.0;

        stopAnalysis.compareAndSet(true, false);
    }

    private void SendTodaysMaxProfit(Double maxProfit){
        LocalDate today = LocalDate.now();
        System.out.println("{\"Date\":\"" + today + "\", \"max_profit\" : \"" + maxProfit + "\"}");
    }

    private void processBuyAndSell(){
        while(!stopAnalysis.get()) {
            try {
                Optional<Quote> sellQuote = buyStreamHandler.readQuote();
                Optional<Quote> buyQuote = buyStreamHandler.readQuote();

                //process the buy and sell in order of their execution timestamp
                if (!sellQuote.isEmpty() && !buyQuote.isEmpty()) {
                    if(sellQuote.get().timestamp < buyQuote.get().timestamp){
                        metadataProcessor.processSell(stockAnalysisMetadata, sellQuote.get());
                        metadataProcessor.processBuy(stockAnalysisMetadata, buyQuote.get());
                    }else{
                        metadataProcessor.processBuy(stockAnalysisMetadata, buyQuote.get());
                        metadataProcessor.processSell(stockAnalysisMetadata, sellQuote.get());
                    }
                }else if(!sellQuote.isEmpty()){
                    metadataProcessor.processSell(stockAnalysisMetadata, sellQuote.get());
                }else if(!buyQuote.isEmpty()) {
                    metadataProcessor.processBuy(stockAnalysisMetadata, buyQuote.get());
                }
            } catch (StreamingException e) {
                throw new RuntimeException(e);
            }
        }
        SendTodaysMaxProfit(stockAnalysisMetadata.maxProfit);
    }
}

















