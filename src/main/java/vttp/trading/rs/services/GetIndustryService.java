package vttp.trading.rs.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import vttp.trading.rs.repositories.PriceRepository;

@Service
public class GetIndustryService {
    
    @Autowired
    PriceRepository priceRepo;


    public HashMap<String, String[]> getNQIndustry(){

        HashMap<String, String[]> industries = new HashMap<>();

        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        try {
            HtmlPage page = webClient.getPage("https://en.wikipedia.org/wiki/Nasdaq-100");
            HtmlTable table = page.getHtmlElementById("constituents"); 

            for(HtmlTableRow row: table.getRows()){
                String[] sectors = {row.getCell(2).asNormalizedText(), row.getCell(3).asNormalizedText() };
                industries.put(row.getCell(1).asNormalizedText(), sectors);
            }
            webClient.close();
            return industries;

        } catch (IOException e) {   System.out.println("An error occurred: " + e);  }
        webClient.close();
        return industries;
    }


    public void saveNQIndustry(List<String> tickers, String collectionName){

        for(String ticker: tickers)
        {   // System.out.println(ticker);    **for checking speed, troubleshoot
            priceRepo.saveIndustries(ticker, getNQIndustry(), collectionName);    }
    }

    public void saveSnPIndustry(List<String> tickers){

        for(String ticker: tickers)
        {   System.out.println(ticker);
            priceRepo.saveIndustries(ticker, getSnPIndustry(), "SnP");    }
    }


    public HashMap<String, String[]> getSnPIndustry(){

        HashMap<String, String[]> industries = new HashMap<>();

        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);

        try {
            HtmlPage page = webClient.getPage("https://en.wikipedia.org/wiki/List_of_S%26P_500_companies");
            HtmlTable table = page.getHtmlElementById("constituents");

            for(HtmlTableRow row: table.getRows()){
                String[] sectors = {row.getCell(3).asNormalizedText(), row.getCell(4).asNormalizedText() };
                industries.put(row.getCell(0).asNormalizedText(), sectors);
            }
            webClient.close();
            return industries;

        } catch (IOException e) {   System.out.println("An error occurred: " + e);  }
        webClient.close();
        return industries;
    }
}
