package stockAnalyzer;

import data.Quote;
import data.SingleStockAnalysisMetadata;

/**
 * since in the question it is written that
 *  "A new stock quote (buy or sell) is generated every minute consistently."
 * We can assume that the data in both the stream comes in order.
 * If that is not the case then this logic need to handle the processing based on timestamp.
 */
public class StockMetadataProcessor {


    public synchronized void processSell(SingleStockAnalysisMetadata metadata, Quote newSellQuote){
        if(metadata.currentBestSellValue < newSellQuote.getPrice()){
            metadata.currentBestSellValue = newSellQuote.getPrice();
        }
        updateProfits(metadata);
    }
    public synchronized void processBuy(SingleStockAnalysisMetadata metadata, Quote newBuyQuote){
        if(metadata.currentBestBuyValue > newBuyQuote.getPrice()){
            metadata.currentBestBuyValue = newBuyQuote.getPrice();
        }else{
            return;
        }
        updateProfits(metadata);
    }

    private void updateProfits(SingleStockAnalysisMetadata metadata){
        metadata.currentProfit = metadata.currentBestSellValue - metadata.currentBestBuyValue;
        if(metadata.currentProfit > metadata.maxProfit){
            metadata.maxProfit = metadata.currentProfit;
        }
    }

}
