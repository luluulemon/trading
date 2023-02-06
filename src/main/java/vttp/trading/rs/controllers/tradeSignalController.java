package vttp.trading.rs.controllers;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vttp.trading.rs.services.HistoricalPriceService;

@Controller
public class tradeSignalController {
    
    @Autowired
    private HistoricalPriceService priceSvc;

    @GetMapping("r2g")
    public String redToGreen(Model model){

        // 1. Previous week is red
            // Check for previous Fri -> Thur -> Wed etc., get the close, and low
            // Move towards Monday, get the open
        // 2. current week is green
        // 3. green is higher than previous week close
        List<Document> prices = priceSvc.getIndexPrices("SnP");

        // 1641103200 Benchmark time: 30 Jan 2022 Sunday
        long benchMarkTime = 1643522400000l; 

        for(Document price: prices){
            
            if(!price.getString("ticker").equals("log") ){

                double open = 0;
                double close = 0;
                double high = 0;
                double low = 99999;

                String ticker = price.getString("ticker");
                List<Long> dates = price.getList("dates", Long.class);
                int size = dates.size();
                List<Double> opens = price.getList("opens", Double.class);
                List<Double> highs = price.getList("highs", Double.class);
                List<Double> lows = price.getList("lows", Double.class);
                
                int dayCount = 1;
                long dayOfWeek = ((dates.get(dates.size()-1) - benchMarkTime  )/86400000)%7 ;

                close = price.getList("closes", Double.class).get(size-1);

                long prevDay = 0l;

                while(prevDay < dayOfWeek){
                    
                    open = opens.get(size - dayCount);
                    high = Math.max(high, highs.get(size - dayCount));
                    low = Math.min(low, lows.get(size - dayCount));

                    dayCount++;
                    prevDay = ((dates.get(dates.size()-dayCount) - benchMarkTime  )/86400000)%7 ;
                    //System.out.println("Check prevDay " + prevDay);
                }

                double prevWkOpen = 0;
                double prevWkClose = price.getList("closes", Double.class).get(size-dayCount);
                double prevWkHigh = 0;
                double prevWkLow = 99999;

                // reset days
                dayOfWeek = ((dates.get(dates.size()-dayCount) - benchMarkTime  )/86400000)%7 ;
                prevDay = 0l;
                while(prevDay < dayOfWeek){
                    prevWkOpen = opens.get(size - dayCount);
                    prevWkHigh = Math.max(prevWkHigh, highs.get(size - dayCount));
                    prevWkLow = Math.min(prevWkLow, lows.get(size - dayCount));

                    dayCount++;
                    prevDay = ((dates.get(dates.size()-dayCount) - benchMarkTime  )/86400000)%7 ;
                }

                String colors = "  ";
                if(low<prevWkLow && close>open){
                    System.out.println(ticker + " has a r2g signal");
                }
                if(prevWkClose>prevWkOpen){ colors += "G";  }
                else{   colors += "R";  }

                if(close>open){ colors += "G";}
                else{ colors += "R"; }

                System.out.println(ticker + " :" + colors);
            }
        }



        //model.addAttribute("test", dayOfWeek);

        return "test";
    }
}
