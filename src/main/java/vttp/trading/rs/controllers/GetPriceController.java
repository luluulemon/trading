package vttp.trading.rs.controllers;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.trading.rs.models.RS;
import vttp.trading.rs.repositories.PriceRepository;
import vttp.trading.rs.services.GetIndexService;
import vttp.trading.rs.services.GetIndustryService;
import vttp.trading.rs.services.HistoricalPriceService;
import vttp.trading.rs.services.IndexListService;
import vttp.trading.rs.services.LogService;

@Controller
public class GetPriceController {
    
    @Autowired
    HistoricalPriceService priceSvc;

    @Autowired
    IndexListService indexListSvc;

    @Autowired
    GetIndexService getIndexSvc;

    @Autowired
    GetIndustryService getIndustrySvc;

    @Autowired
    LogService logSvc;

    @Autowired
    PriceRepository priceRepo;
    
    @GetMapping("/price/RS/{indexCollectionName}")
    public String getRS(Model model, @PathVariable String indexCollectionName){
          // ****Select collectionName with Path Variable****
        List<List<RS>> rsObj = priceSvc.getRS(indexCollectionName);
        model.addAttribute("RSLists", rsObj);
        model.addAttribute("IndexName", indexCollectionName);

        return "rsTemplate";
    }


    @GetMapping("/updatePrice/{collectionName}")
    public String updatePrice(@PathVariable String collectionName){
        
        List<String> tickers = new LinkedList<>();

        long startTime = System.nanoTime();
        // 1. Get last time
        //  Take currentTimeStamp minus (lastTime/1000) / 86400 - 1 (minus one is for time difference)
        //  divide by 30 to get no. of months
        Long lastTime = logSvc.getLogDate(collectionName);
        Long currentTime = System.currentTimeMillis();

        int numDays = (int)(currentTime - lastTime)/1000/86400 - 1;
        int numMonths = numDays/30 + 1;
        System.out.println(">>>>>>>>>>>>>>>>>>" + numMonths);

        // 2. Cycle through the Json, get the last time index
        if(collectionName.equals("SnP"))
        {   tickers = getIndexSvc.getSnPList();   }     // List of tickers to work on
        else    {   tickers = getIndexSvc.getNQList();  }
        
        Long updateCount = 0l;
        Long lastUpdateTime = 0l;
        for(String ticker: tickers){
            JsonObject updateResults = priceSvc.getPrices(ticker, collectionName, "month", Integer.toString(numMonths));
            try{    Thread.sleep(280); } catch(InterruptedException e)
            {    e.printStackTrace();    }
            JsonArray candles = updateResults.getJsonArray("candles");
            int range = candles.size();
            lastUpdateTime = Long.parseLong( candles.get(range-1).asJsonObject().get("datetime").toString() );

            Document dbResult = priceRepo.getPriceByTicker(ticker, collectionName);
            List<Long> dateTime = dbResult.getList("dates", Long.class);
            Long lastDBTime = dateTime.get(dateTime.size()-1);

            if(lastDBTime.equals(lastUpdateTime))
            {   System.out.println("Nothing to update >>>>>>>>>>>>> for " + ticker);    }

            else{
                int indexToStart = 0;
                for(int i=0; i< range; i++){
                    JsonObject v = (JsonObject) candles.get(i);
                    Long time = Long.parseLong( v.get("datetime").toString() ) ;
                    if(time > lastDBTime){    
                        indexToStart = i;
                        System.out.println("CHeck index to start: " + indexToStart);
                        break;   }
                }
            // 3. Get the previous object by ticker, update the obj -> Open, close, datetime, and various RS
            // 4. Save back to the database
            
            updateCount += priceSvc.updatePrices(ticker, collectionName, updateResults, dbResult ,indexToStart);
            }
                
        }
        // last -> create Log
        logSvc.logPriceUpdate(updateCount, lastUpdateTime, collectionName);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time in millis: " + elapsedTime/1000000);

        return "test";
    }


    // below endPoints are just for setups!

    @GetMapping("/price/loadAll/{collectionName}")
    public String loadPrices(@PathVariable String collectionName){

        long startTime = System.nanoTime();
        // collectionName: ***save to which collection***

        Long updateCount = 0l;
        Long lastUpdateTime = 0l;
        List<String> tickers = new LinkedList<>();     // List of tickers to work on
        
        switch (collectionName){
            case "SnP":
                tickers = getIndexSvc.getSnPList(); 
                break;
            default:
                tickers = getIndexSvc.getNQList();
                break;
        }

        for(String ticker: tickers){
              // delay API call 0.21s -> endPoint has limit of 120/min
             updateCount += priceSvc.savePrices(priceSvc.getPrices(ticker, collectionName), collectionName);
            // try{    Thread.sleep(200); } catch(InterruptedException e)
            // {    e.printStackTrace();    }
        }
        // Get updateTime, add to Log
        List<Long> datesList = priceSvc.getPriceByTicker(tickers.get(0), collectionName).getList("dates", Long.class);
        lastUpdateTime = datesList.get(datesList.size()-1);
        priceSvc.saveLog(updateCount, lastUpdateTime, collectionName);

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time in millis: " + elapsedTime/1000000);
        // ***Load industries***  takes about 0.24s per ticker update

        switch (collectionName){
            case "SnP":
                getIndustrySvc.saveSnPIndustry(tickers);
                break;
            default:
                getIndustrySvc.saveNQIndustry(tickers, collectionName);
                break;
        }

        return "test";
    }






}
