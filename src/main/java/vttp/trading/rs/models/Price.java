package vttp.trading.rs.models;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Price {
    
    private String symbol;
    private Double[] openPrices;
    private Double[] closePrices;
    private Long[] datetimes;
    private Double rs1y;
    private Double rs6m;
    private Double rs3m;
    private Double rs1m;


    public Double getRs1y() {        return rs1y;    }
    public void setRs1y(Double rs1y) {        this.rs1y = rs1y;    }

    public Double getRs6m() {        return rs6m;    }
    public void setRs6m(Double rs6m) {        this.rs6m = rs6m;    }
    
    public Double getRs3m() {        return rs3m;    }
    public void setRs3m(Double rs3m) {        this.rs3m = rs3m;    }

    public Double getRs1m() {        return rs1m;    }
    public void setRs1m(Double rs1m) {        this.rs1m = rs1m;    }

    public Double[] getClosePrices() {        return closePrices;    }
    public void setClosePrices(Double[] closePrices) {        this.closePrices = closePrices;    }

    public String getSymbol() {        return symbol;   }
    public void setSymbol(String symbol) {        this.symbol = symbol;    }

    public Double[] getOpenPrices() {        return openPrices;    }
    public void setOpenPrices(Double[] prices) {        this.openPrices = prices;    }

    public Long[] getDatetimes() {        return datetimes;    }
    public void setDatetimes(Long[] datetimes) {        this.datetimes = datetimes;    }


    public static Price loadPrice(JsonObject prices){
        JsonArray candles = prices.getJsonArray("candles");
        int n = candles.size();
        Price price = new Price();
        Double[] opens = new Double[n];
        Double[] closes = new Double[n];
        Long[] dates = new Long[n];
        
        for(int i=0; i<n; i++){
            JsonObject obj =  (JsonObject) candles.get(i);
            opens[i] = Double.parseDouble(obj.get("open").toString());
            closes[i] = Double.parseDouble(obj.get("close").toString());
            dates[i] = Long.parseLong(obj.get("datetime").toString());
        }

        price.setOpenPrices(opens);
        price.setClosePrices(closes);
        price.setDatetimes(dates);

        return price;
    }

    // Builds Doc structure for storing in Mongo

    public static JsonObject toPriceJson(JsonObject prices){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArray candles = prices.getJsonArray("candles");
        int n = candles.size();

        JsonArrayBuilder opens = Json.createArrayBuilder();
        JsonArrayBuilder closes = Json.createArrayBuilder();
        JsonArrayBuilder dates = Json.createArrayBuilder();
        JsonArrayBuilder highs = Json.createArrayBuilder();
        JsonArrayBuilder lows = Json.createArrayBuilder();

        for(int i=0; i<n; i++){
            JsonObject obj =  (JsonObject) candles.get(i);
            opens.add( obj.get("open") );
            closes.add( obj.get("close") );
            dates.add( obj.get("datetime") );
            highs.add( obj.get("high") );
            lows.add( obj.get("low") );
        }

        JsonArray closeArray = closes.build();
        Float year = 0f;        // calculating RS for the various periods
        Float halfYear = 0f;
        Float threeMths = 0f;
        Float oneMth = 0f;

        if(n > 21){
            oneMth = ( Float.parseFloat(closeArray.get(n-1).toString())
            / Float.parseFloat( closeArray.get(n-21).toString() ) );

            if(closeArray.size() > 62){

                threeMths = ( Float.parseFloat(closeArray.get(n-1).toString())
                / Float.parseFloat( closeArray.get(n-62).toString() ) );

                if(closeArray.size() > 125){

                    halfYear = ( Float.parseFloat(closeArray.get(n-1).toString())
                    / Float.parseFloat( closeArray.get(n-125).toString() ) );

                    if(closeArray.size() > 250){
                        year = ( Float.parseFloat(closeArray.get(n-1).toString())
                        / Float.parseFloat( closeArray.get(n-250).toString() ) );
                    }
                }
            }
        }
        
        builder.add("ticker", prices.getString("symbol"))
                .add("opens", opens.build())
                .add("closes", closeArray)
                .add("dates", dates.build())
                .add("highs", highs.build())
                .add("lows", lows.build())
                .add("rs1y", year)
                .add("rs 6 mth", halfYear)
                .add("rs 3 mths", threeMths)
                .add("rs 1 mth", oneMth);

        return builder.build();
    }


    // {"candles":[{"open":160.5855,"high":161.6615,"low":156.3045,"close":161.486,"volume":87798300,"datetime":1641794400000},{"open":161.5,"high":166.35,"low":160.7015,"close":165.362,"volume":62805580,"datetime":1641880800000},{"open":166.575,"high":166.878,"low":164.417,"close":165.207,"volume":50029120,"datetime":1641967200000},{"open":165.2505,"high":166.221495,"low":161.091,"close":161.214,"volume":52188300,"datetime":1642053600000},{"open":160.15,"high":162.25,"low":159.8005,"close":162.138,"volume":45974860,"datetime":1642140000000},{"open":159.105,"high":159.734495,"low":157.6645,"close":158.9175,"volume":67441440,"datetime":1642485600000},{"open":158.762,"high":159.25,"low":156.25,"close":156.299,"volume":53241920,"datetime":1642572000000},{"open":156.766,"high":158.0,"low":151.351,"close":151.6675,"volume":71973320,"datetime":1642658400000},{"open":149.95,"high":150.9,"low":142.0704,"close":142.643,"volume":163971440,"datetime":1642744800000},{"open":139.0,"high":144.945,"low":135.352,"close":144.544,"volume":156044740,"datetime":1643004000000}

}
