package vttp.trading.rs.models;

import org.bson.Document;

public class RS {
    
    private String ticker;
    private Double rs1y;
    private Double rs6mth;
    private Double rs3mth;
    private String sector;
    private String subIndustry;

    public String getTicker() {        return ticker;    }
    public void setTicker(String ticker) {        this.ticker = ticker;    }

    public Double getRs1y() {        return rs1y;    }
    public void setRs1y(Double rs1y) {        this.rs1y = rs1y;    }

    public Double getRs6mth() {        return rs6mth;    }
    public void setRs6mth(Double rs6mth) {        this.rs6mth = rs6mth;    }

    public Double getRs3mth() {        return rs3mth;    }
    public void setRs3mth(Double rs3mth) {        this.rs3mth = rs3mth;    }

    public String getSector() {        return sector;   }
    public void setSector(String sector) {        this.sector = sector;    }

    public String getSubIndustry() {        return subIndustry;    }
    public void setSubIndustry(String subIndustry) {        this.subIndustry = subIndustry;    }

    public static RS create(Document doc){
        RS rs = new RS();
        rs.setTicker(doc.getString("ticker"));
        rs.setRs1y(doc.getDouble("rs1y"));
        rs.setRs6mth(doc.getDouble("rs 6 mth"));
        rs.setRs3mth(doc.getDouble("rs 3 mths"));
        rs.setSector(doc.getString("Sector"));
        rs.setSubIndustry(doc.getString("sub industry"));
        return rs;
    }

}
