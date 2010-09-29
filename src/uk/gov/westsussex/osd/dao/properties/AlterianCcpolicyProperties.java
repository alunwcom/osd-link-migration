package uk.gov.westsussex.osd.dao.properties;

public class AlterianCcpolicyProperties implements DatabaseProperties {
    
    final static String DB_DRIVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    final static String DB_URL = "jdbc:microsoft:sqlserver://w2tashgabat:1433";
    final static String DB_ACCOUNT = "wscc_login";
    final static String DB_PASSWORD = "W3stSu55ex";
    
    public String getAccount() {
        return DB_ACCOUNT;
    }

    public String getDbUrl() {
        return DB_URL;
    }

    public String getDriver() {
        return DB_DRIVER;
    }

    public String getPassword() {
        return DB_PASSWORD;
    }

}
