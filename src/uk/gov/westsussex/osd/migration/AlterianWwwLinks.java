package uk.gov.westsussex.osd.migration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import uk.gov.westsussex.osd.dao.DatabaseConnection;
import uk.gov.westsussex.osd.dao.properties.AlterianWwwProperties;

/**
 * TEST - audit/update of migrated APLAWS links in Alterian
 * 
 */
public class AlterianWwwLinks {
	
	public static final String SQL_QUERY =
		"select p_page_id, p_title, p_content " +
		"from PROD_www_staging.dbo.page_data " +
		"order by p_page_id";
	
    static Logger log = Logger.getLogger(AlterianWwwLinks.class);
    
    private PrintWriter csvOutput;
    private StringBuffer csvBuffer = new StringBuffer();

	private DateFormat dateFormat = new SimpleDateFormat("-yyyyMMdd-HHmmss");

    public static void main(String[] args) {
		AlterianWwwLinks app = new AlterianWwwLinks();
		app.execute();
	}
    
    protected AlterianWwwLinks() {
    	
		// Create CSV output file
        try {
    		String home = System.getProperty("user.home");
    		String docs = home + "/My Documents";
    		String filename = docs + "/" + this.getClass().getName() 
				+ dateFormat.format(new Date()) + ".csv";
    		csvOutput = new PrintWriter(new FileOutputStream(filename));
    		
    		log.info("Created new CSV output file: " + filename);
    		
            csvOutput.println("CMC_ID,PAGE_TITLE,LINK_URL,LINK_TEXT");
            
        } catch (FileNotFoundException e) {
            log.fatal("Unable to create output file: ", e);
        }
    	
    }
	
	protected void execute() {
		
    	log.info(this.getClass().getName() + ": Starting link export");

		try {
			Connection conn = DatabaseConnection.getConnection(
				new AlterianWwwProperties());	
			PreparedStatement ps = conn.prepareStatement(SQL_QUERY);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				processItem(rs);
			}
			
			rs.close();
			ps.close();
			conn.close();
			csvOutput.flush();
			csvOutput.close();
		
		} catch (SQLException e) {
			log.error("SQL Exception: ", e);
		} catch (IOException e) {
			log.error("I/O Exception: ", e);
		}		

    	log.info(this.getClass().getName() + ": Finishing link export");

	}
	
	protected void processItem(ResultSet rs) throws SQLException, IOException {
		
		int cmcId = rs.getInt("p_page_id");

		String url = null;
		String text = null;
		
        String title = rs.getString("p_title");
        String content = rs.getString("p_content");
		
		log.info("Parsing item: " + cmcId);
		
		// Need to search text in BLOB/CLOB
		if (content != null) {
		    
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(content);
			TagNode[] anchors = root.getElementsByName("a", true);
			
			
			for (int i = 0; i < anchors.length; i++) {
				
				url = anchors[i].getAttributeByName("href");
				if (url != null) {
					url = url.trim();
				}
				
				//log.info("Got URL: " + url);
				
				text = cleaner.getInnerHtml(anchors[i]);
				if (text != null) {
					text = stripWhitespace(text);
				}
				
				if (url != null && url.length() > 0 && !url.startsWith("#")) {
					
			    	csvBuffer.setLength(0);
			    	
                    csvBuffer.append("\"" + cmcId + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + title + "\"");
                    csvBuffer.append(",");
			    	csvBuffer.append("\"" + url + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + text + "\"");
			        
			        csvOutput.println(csvBuffer.toString());
				}				
			}
		}
	}
	
    /**
     * Replaces tabs and linefeeds with spaces, and replaces commas
     * with spaces 
     */
    String stripWhitespace(String s) {
    	s = s.replaceAll("\n", " ");
    	s = s.replaceAll("\r", " ");
    	s = s.replaceAll("\t", " ");
    	s = s.replaceAll(",", " ");
    	
    	return s;
    }
}
