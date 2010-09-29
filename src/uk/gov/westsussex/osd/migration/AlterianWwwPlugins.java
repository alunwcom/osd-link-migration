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
 * TEST - check for plugins in Alterian
 * 
 */
public class AlterianWwwPlugins {
	
	public static final String SQL_QUERY =
		"select p_page_id, p_title, p_content " +
		"from PROD_www_live.dbo.page_data " +
		"order by p_page_id";
	
	public static final String WEBSITE_HOST =
	    "www.westsussex.gov.uk";
	
    static Logger log = Logger.getLogger(AlterianWwwPlugins.class);
    
    private PrintWriter csvOutput;
    private StringBuffer csvBuffer = new StringBuffer();

	private DateFormat dateFormat = new SimpleDateFormat("-yyyyMMdd-HHmmss");

    public static void main(String[] args) {
		AlterianWwwPlugins app = new AlterianWwwPlugins();
		app.execute();
	}
    
    protected AlterianWwwPlugins() {
    	
		// Create CSV output file
        try {
    		String home = System.getProperty("user.home");
    		String docs = home + "/My Documents";
    		String filename = docs + "/" + this.getClass().getName() 
				+ dateFormat.format(new Date()) + ".csv";
    		csvOutput = new PrintWriter(new FileOutputStream(filename));
    		
    		log.info("Created new CSV output file: " + filename);
    		
            csvOutput.println("CMC_ID,PAGE_TITLE,PAGE_URL,EMBED_URL,EMBED_WIDTH,EMBED_HEIGHT");
            
        } catch (FileNotFoundException e) {
            log.fatal("Unable to create output file: ", e);
        }
    	
    }
	
	protected void execute() {
		
    	log.info(this.getClass().getName() + ": Starting plugin export");

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

    	log.info(this.getClass().getName() + ": Finishing plugin export");

	}
	
	protected void processItem(ResultSet rs) throws SQLException, IOException {
		
		int cmcId = rs.getInt("p_page_id");

		String url = null;
        String width = null;
        String height = null;
//		String text = null;
		
        String title = rs.getString("p_title");
        String content = rs.getString("p_content");
		
		log.debug("Parsing item: " + cmcId);
		
		// Need to search text in BLOB/CLOB
		if (content != null) {
		    
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(content);
			TagNode[] elements = root.getElementsByName("immcore:embedsite", true);
			
			for (int i = 0; i < elements.length; i++) {
				
                url = elements[i].getAttributeByName("url");
                if (url != null) {
                    url = url.trim();
                }
                
                width = elements[i].getAttributeByName("width");
                if (width != null) {
                    width = width.trim();
                }
                
                height = elements[i].getAttributeByName("height");
                if (height != null) {
                    height = height.trim();
                }
                
				log.debug("[" + cmcId + "] Got URL : " + url);
				
//				text = cleaner.getInnerHtml(elements[i]);
//				if (text != null) {
//					text = stripWhitespace(text);
//				}
				
				if (url != null && url.length() > 0 && !url.startsWith("#")) {
					
			    	csvBuffer.setLength(0);
			    	
                    csvBuffer.append("\"" + cmcId + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + title + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"http://" + WEBSITE_HOST + "/default.aspx?page=" + cmcId + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + url + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + width + "\"");
                    csvBuffer.append(",");
                    csvBuffer.append("\"" + height + "\"");
			        
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
