package uk.gov.westsussex.osd.dao.properties;

public interface DatabaseProperties {
    
    public String getDriver();
    public String getDbUrl();
    public String getAccount();
    public String getPassword();
}
