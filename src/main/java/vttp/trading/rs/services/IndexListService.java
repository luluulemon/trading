package vttp.trading.rs.services;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class IndexListService {
    
    // java.lang.NoClassDefFoundError: org/jsoup/Jsoup -> Can't get this to work

    public List<String> getNQ(){
        
        List<String> tickers = new LinkedList<>();

        try{
            Document document = Jsoup.connect("https://en.wikipedia.org/wiki/Nasdaq-100")
                                            .timeout(5000)
                                            .get();

            Element table = document.getElementsByClass("wikitable sortable jquery-tablesorter")
                                    .get(0);

            Elements rows = table.select("tr");

            for(int i=1; i<rows.size(); i++) // skip 1st row: headers
            {   Element row = rows.get(i);
                Elements cols = row.select("td");
                tickers.add( cols.get(1).text() );      // add column2 to ticker list
            }

            return tickers;

        } catch(IOException IOEx){
            IOEx.printStackTrace();
        }

        return tickers;
    }
}
