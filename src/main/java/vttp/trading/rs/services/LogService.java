package vttp.trading.rs.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.trading.rs.repositories.PriceRepository;

@Service
public class LogService {
    
    @Autowired
    private PriceRepository priceRepo;

    public Long getLogDate(String collectionName){  return priceRepo.getLogDate(collectionName);    }

    public void logPriceUpdate(Long updateCount, Long datetime, String collectionName){
        priceRepo.updateLog(datetime, updateCount, collectionName);   
    }
}
