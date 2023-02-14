package vttp.trading.rs.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import vttp.trading.rs.models.Price;

@Repository
public class PriceRepository {
    
    @Autowired
    MongoTemplate mongoTemplate;

    public Long savePrice(Document doc, String collectionName){
        if(mongoTemplate.insert(doc, collectionName) != null){ return 1l;    }
        return 0l;
    }

    public Long getLogDate(String collectionName){
        List<Document> results = mongoTemplate.find(Query.query(Criteria.where("ticker").is("log")),
                                         Document.class, collectionName);
        return results.get(0).getLong("dates");
    }

    public Long updatePrice(String ticker, String collectionName, Price price){
        // updates new opens, closes, datetime, RS
        Query query = Query.query(Criteria.where("ticker" ).is(ticker));

        Update updateOps = new Update().set("opens", price.getOpenPrices())
                                        .set("closes", price.getClosePrices())
                                        .set("dates", price.getDatetimes())
                                        .set("highs", price.getHighs())
                                        .set("lows", price.getLows())
                                        .set("rs1y", price.getRs1y())
                                        .set("rs 6 mth", price.getRs6m())
                                        .set("rs 3 mths", price.getRs3m())
                                        .set("rs 1 mth", price.getRs1m());
// Mongo fields: "rs1y", "rs 6 mth", "rs 3 mths", "rs 1 mth", opens, closes, dates
        UpdateResult result =
        mongoTemplate.updateMulti(query, updateOps, Document.class, collectionName);

        return result.getModifiedCount();
    }

    public void updateLog(Long datetime, Long updateCount, String collectionName){
        Query query = Query.query(Criteria.where("ticker" ).is("log"));
        Update updateOps = new Update()
                            .set("updateCount", updateCount)
                            .set("dates", datetime);

        mongoTemplate.updateMulti(query, updateOps, Document.class, collectionName);
    }

    public List<Document> getIndexPrices(String collectionName){
        return mongoTemplate.find(new Query(), Document.class, collectionName);
    }


    public Document getPriceByTicker(String ticker, String collectionName){
        Query query = Query.query(Criteria.where("ticker").is(ticker));
        return mongoTemplate.find(query, Document.class, collectionName).get(0);
    }

    public void saveIndustries(String ticker, HashMap<String, String[]> sectors, String collectionName){
        
        Query query = Query.query(Criteria.where("ticker").is(ticker));
        Update updateOps = new Update().set("Sector", sectors.get(ticker)[0])
                                        .set("sub industry", sectors.get(ticker)[1]);

        mongoTemplate.updateFirst(query, updateOps, Document.class, collectionName);
    }


    public List<List<Document>> getRS(String collectionName){
        List<List<Document>> rsDocs = new ArrayList<>();
        
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "rs1y")).limit(20);
        List<Document> rs12 = mongoTemplate.find(query, Document.class, collectionName);

        Query query2 = new Query().with(Sort.by(Sort.Direction.DESC, "rs 6 mth")).limit(20);
        List<Document> rs6 = mongoTemplate.find(query2, Document.class, collectionName);
        
        Query query3 = new Query().with(Sort.by(Sort.Direction.DESC, "rs 3 mths")).limit(20);
        List<Document> rs3 = mongoTemplate.find(query3, Document.class, collectionName);

        rsDocs.add(rs3);
        rsDocs.add(rs6);
        rsDocs.add(rs12);

        return rsDocs;
    }

}   
    // "_id" : ObjectId("63be2a37f2442c75d3224ea6"),
    // "ticker" : "STLD",
    // "opens" : []
    // "closes" : []
    // "dates" : []
    // "rs1y" : 1.6732133626937866,
    // "rs 6 mth" : 1.3731638193130493,
    // "rs 3 mths" : 1.3731638193130493,
    // "rs 1 mth" : 1.0001649856567383,
    // "Sector" : "Materials",
    // "sub industry" : "Steel"

