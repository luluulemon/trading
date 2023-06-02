package vttp.trading.rs.services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.trading.rs.models.Price;
import vttp.trading.rs.models.RS;
import vttp.trading.rs.repositories.PriceRepository;

@Service
public class HistoricalPriceService {
    
    @Autowired
    PriceRepository priceRepo;

    String apiKey = System.getenv("TD_API_KEY");

    public JsonObject getPrices(String ticker, String collectionName){    // default for getPrice -> 5 years
        return getPrices(ticker, collectionName, "year", "5");     }

    public JsonObject getPrices(String ticker, String collectionName, String periodType, String period){

        String endPointUrl = "https://api.tdameritrade.com/v1/marketdata/%s/pricehistory";
        String url = UriComponentsBuilder.fromUriString(endPointUrl.formatted(ticker.toUpperCase()))
                    .queryParam("apikey", apiKey)
                    .queryParam("periodType", periodType)
                    .queryParam("Period", period)
                    .queryParam("frequencyType", "daily")
                    .queryParam("frequency", "1")
                    .toUriString(); 

        System.out.println("CHeck >>>>>>>>>>>>>>>>>>>>>>>" + url);

        RequestEntity req = RequestEntity.get(url).build();  
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.exchange(req, String.class);

        JsonReader reader = Json.createReader(new StringReader(resp.getBody()));
        JsonObject results = reader.readObject(); 

        return results;
    }

    // getPrice with Start Date
    public JsonObject getPrices(String ticker, String collectionName, String periodType, long startDate){

        String endPointUrl = "https://api.tdameritrade.com/v1/marketdata/%s/pricehistory";
        String url = UriComponentsBuilder.fromUriString(endPointUrl.formatted(ticker.toUpperCase()))
                    .queryParam("apikey", apiKey)
                    .queryParam("periodType", periodType)
                    .queryParam("startDate", startDate)
                    .queryParam("frequencyType", "daily")
                    .queryParam("frequency", "1")
                    .toUriString(); 

        RequestEntity req = RequestEntity.get(url).build();  
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.exchange(req, String.class);

        JsonReader reader = Json.createReader(new StringReader(resp.getBody()));
        JsonObject results = reader.readObject(); 

        return results;
    }




    public List<Document> getPriceByTicker(String ticker, String collectionName){
        return priceRepo.getPriceByTicker(ticker, collectionName);
    }

    public List<Document> getIndexPrices(String collectionName){
        return priceRepo.getIndexPrices(collectionName);
    }

    public Long savePrices(JsonObject resultBody, String collectionName){
        JsonObject price = Price.toPriceJson(resultBody);
        Document priceDoc = Document.parse(price.toString());
        return priceRepo.savePrice(priceDoc, collectionName);
    }


    public Long updatePrices(String ticker, String collectionName, JsonObject updateResults, 
                            Document dbResult, int updateIndex){
        // retrieve DB prices --> prices come out in **Double type**

        List<Double> opens =  dbResult.getList("opens", Double.class);
        List<Double> closes =  dbResult.getList("closes", Double.class);
        List<Long> dateTime = dbResult.getList("dates", Long.class);
        List<Double> highs = dbResult.getList("highs", Double.class);
        List<Double> lows = dbResult.getList("lows", Double.class);

        // combine with the new open close, datetime array
        JsonArray candles = updateResults.getJsonArray("candles");
        for(int i=updateIndex; i<candles.size(); i++){
            opens.add( Double.parseDouble( candles.get(i).asJsonObject().get("open").toString() ) );
            closes.add( Double.parseDouble( candles.get(i).asJsonObject().get("close").toString() ) );
            dateTime.add( Long.parseLong( candles.get(i).asJsonObject().get("datetime").toString() ) );
            highs.add( Double.parseDouble( candles.get(i).asJsonObject().get("high").toString() ) );
            lows.add( Double.parseDouble( candles.get(i).asJsonObject().get("low").toString() ) );
        }
        System.out.println("Resulting length of the closes: " + closes.size());

        Double[] opensArray = new Double[opens.size()];
        Double[] closesArray = new Double[closes.size()];
        Long[] datetime = new Long[dateTime.size()];
        // calculate new RS
        Price price = updateRS(closes);
        price.setClosePrices(closes.toArray(closesArray));
        price.setOpenPrices(opens.toArray(opensArray));
        price.setDatetimes(dateTime.toArray(datetime));
        price.setHighs(highs);
        price.setLows(lows);
        // send back for update
        return priceRepo.updatePrice(ticker, collectionName, price);
    }

    public void saveLog(Long updateCount, Long datetime, String collectionName){
        JsonObject logJson = Json.createObjectBuilder().add("ticker", "log")
                                        .add("dates", datetime)
                                        .add("updateCount", updateCount)
                                        .build();
        priceRepo.savePrice(Document.parse(logJson.toString()), collectionName);      
    }

    // public void logPriceUpdate(Long updateCount, Long datetime, String collectionName){
    //     priceRepo.updateLog(datetime, updateCount, collectionName);   
    // }
    

    // Helper function to calculate RS from closing prices
    public Price updateRS(List<Double> closes){
        Price price = new Price();
        Double year = 0d;
        Double halfYear = 0d;
        Double threeMths = 0d;
        Double oneMth = 0d;
        int n = closes.size();

        if(closes.size() > 250){
            year = closes.get(n-1) / closes.get(n-250);
            halfYear = closes.get(n-1) / closes.get(n-125);
            threeMths = closes.get(n-1) / closes.get(n-62);
            oneMth = closes.get(n-1) / closes.get(n-20);
        }
        else if(n > 125){
            halfYear = closes.get(n-1) / closes.get(n-125);
            threeMths = closes.get(n-1) / closes.get(n-62);
            oneMth = closes.get(n-1) / closes.get(n-20);
        }
        else if(n > 62){
            threeMths = closes.get(n-1) / closes.get(n-62);
            oneMth = closes.get(n-1) / closes.get(n-20);
        }
        else if(n > 20){ oneMth = closes.get(n-1) / closes.get(n-20);  }
                
        price.setRs1m(oneMth);
        price.setRs3m(threeMths);
        price.setRs6m(halfYear);
        price.setRs1y(year);
        return price;
    }

    
    // Get RS List from Repo, convert JSON to obj, pass back to controller
    public List<List<RS>> getRS(String collectionName){
        
        List<List<RS>> rsObj = new ArrayList<>();
        List<List<Document>> rsDocs = priceRepo.getRS(collectionName);

        List<RS> rs3 = rsDocs.get(0).stream()
                        .map(v -> RS.create(v))
                        .toList();
        List<RS> rs6 = rsDocs.get(1).stream()
                                    .map(v -> RS.create(v))
                                    .toList();
        List<RS> rs12 = rsDocs.get(2).stream()
                                    .map(v -> RS.create(v))
                                    .toList();

        rsObj.add(rs3);
        rsObj.add(rs6);
        rsObj.add(rs12);

        return rsObj;
    }
}

