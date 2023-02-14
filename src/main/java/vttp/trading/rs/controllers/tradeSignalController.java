package vttp.trading.rs.controllers;

import java.util.LinkedList;
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
           
        List<String> r2g = new LinkedList<>();
        List<String> g2r = new LinkedList<>();
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
                    // move backwards through the lists
                    open = opens.get(size - dayCount);
                    high = Math.max(high, highs.get(size - dayCount));
                    low = Math.min(low, lows.get(size - dayCount));

                    dayCount++;
                    prevDay = ((dates.get(size-dayCount) - benchMarkTime  )/86400000)%7 ;
                    //System.out.println("Check prevDay " + prevDay);
                }

                double prevWkOpen = 0;
                double prevWkClose = price.getList("closes", Double.class).get(size-dayCount);
                double prevWkHigh = 0;
                double prevWkLow = 99999;

                // Move backwards for previous week
                dayOfWeek = ((dates.get(size-dayCount) - benchMarkTime  )/86400000)%7 ;
                prevDay = 0l;
                while(prevDay < dayOfWeek){
                    prevWkOpen = opens.get(size - dayCount);
                    prevWkHigh = Math.max(prevWkHigh, highs.get(size - dayCount));
                    prevWkLow = Math.min(prevWkLow, lows.get(size - dayCount));

                    dayCount++;
                    prevDay = ((dates.get(dates.size()-dayCount) - benchMarkTime  )/86400000)%7 ;
                }


                // screen for 1. uppercut previous wk, 2. Previous week is red 3. Close in upper half
                if(low<prevWkLow && prevWkOpen>prevWkClose &&  close > (high+low)/2 ){
                    r2g.add(ticker);
                    // add further checks on signal strength
                }

                if(high>prevWkHigh && prevWkOpen<prevWkClose &&  close < (high+low)/2 ){
                    g2r.add(ticker);
                    // add further checks on signal strength
                }
            }

            model.addAttribute("R2g", r2g);
            model.addAttribute("G2r", g2r);

        }

        return "reversalTemplate";
    }
}
