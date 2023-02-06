package vttp.trading.rs.services;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

// Service grabs SnP 500 and NQ100 List from Wikipedia

@Service
public class GetIndexService {
    

    public List<String> getNQList(){

        List<String> tickers = new LinkedList<>();

        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        try {
            HtmlPage page = webClient.getPage("https://en.wikipedia.org/wiki/Nasdaq-100");

            DomElement firstHeading = page.getElementById("firstHeading");
            HtmlTable table = page.getHtmlElementById("constituents");
            System.out.print("Should print Nasdaq " + firstHeading.asNormalizedText()); 

            for(HtmlTableRow row: table.getRows()){
                tickers.add( row.getCell(1).asNormalizedText() );
            }
            webClient.close();
            tickers.remove(0);
            return tickers;

        } catch (IOException e) {   System.out.println("An error occurred: " + e);  }
        webClient.close();
        return tickers;
    }


    public List<String> getSnPList(){

        List<String> tickers = new LinkedList<>();

        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        try {
            HtmlPage page = webClient.getPage("https://en.wikipedia.org/wiki/List_of_S%26P_500_companies");
            HtmlTable table = page.getHtmlElementById("constituents");

            for(HtmlTableRow row: table.getRows()){
                tickers.add( row.getCell(0).asNormalizedText() );
            }
            webClient.close();
            tickers.remove(0);
            return tickers;

        } catch (IOException e) {   System.out.println("An error occurred: " + e);  }
        webClient.close();
        return tickers;
    }
}
