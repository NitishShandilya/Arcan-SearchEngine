import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {		
	
	File urlsFileName, fetchFileName, visitFileName, pagerankdataFileName;
	private FileWriter urlsWriter, fetchWriter, visitWriter, pagerankdataWriter;
	int fetchesAttemptedCount = 0;
	int fetchesSucceededCount = 0;
	int fetchesFailedCount = 0;
	long linksCount = 0;	
	int fileSizeLess1Kb, fileSize1To10Kb, fileSize10To100Kb, fileSize100KbTo1Mb, fileSizeGreater1Mb = 0;
	
	HashSet<String> urlHashSet = new HashSet<String>();
    Map<Integer,Long> statusCodeMap = new HashMap<Integer,Long>();
    Map<String,Integer> contentTypeMap = new HashMap<String,Integer>();
	CrawlStat myCrawlStat;

	public MyCrawler() {
	  myCrawlStat = new CrawlStat();
	}
	
	@Override
	public void onStart() {
		try {			
			urlsFileName = new File("urls.csv");
	        if (!urlsFileName.exists()) 
	        {
	        	urlsFileName.createNewFile();
	        }
			urlsWriter = new FileWriter(urlsFileName.getAbsoluteFile(), true);
			
			fetchFileName = new File("fetch.csv");
	        if (!fetchFileName.exists()) 
	        {
	        	fetchFileName.createNewFile();
	        }
	        fetchWriter = new FileWriter(fetchFileName.getAbsoluteFile(), true);
	        	        
	        visitFileName = new File("visit.csv");
	        if (!visitFileName.exists()) 
	        {
	        	visitFileName.createNewFile();
	        }
	        visitWriter = new FileWriter(visitFileName.getAbsoluteFile(), true);	        
	        
	        pagerankdataFileName = new File("pagerankdata.csv");
	        if (!pagerankdataFileName.exists()) 
	        {
	        	pagerankdataFileName.createNewFile();
	        }
	        pagerankdataWriter = new FileWriter(pagerankdataFileName.getAbsoluteFile(), true);
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {		
		try {			
			urlsWriter.append(url.getURL());
			urlsWriter.append(',');				
			String urlForComparison = url.getURL().toLowerCase();
			String indicator = indicateClass(urlForComparison);
			urlsWriter.append(indicator);
			urlsWriter.append('\n');										
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		return url.getURL().toLowerCase().startsWith("https://cinema.usc.edu/");		
	}
	
	private String indicateClass(String urlForComparison) {		
		if (urlForComparison.startsWith("https://cinema.usc.edu/")){
			return("OK");
		}
		else if (urlForComparison.contains("usc.edu")){
			return("USC");
		}
		else {
			return("outUSC");
		}		    
	}
	
	@Override
	public void visit(Page page) {
		
		String url = page.getWebURL().getURL();
		System.out.println("URL: " + url);			
		
		String[] contentArray = page.getContentType().split(";");
		String contentType = contentArray[0];						
		
		/*
		 * Explicitly Converting the not-handled cases
		 */
		if (url.endsWith(".pdf")){
			contentType = "application/pdf";
		}
		else if (url.endsWith(".doc")){
			contentType = "application/msword";
		}
		else if (url.endsWith(".docx")){
			contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		}
		else if (url.endsWith(".gif")){
			contentType = "image/gif";
		}
		else if (url.endsWith(".jpg") || url.endsWith(".jpeg")){
			contentType = "image/jpeg";
		}
		else if (url.endsWith(".mp3")){
			contentType = "audio/mpeg";
		}
		else if (url.endsWith(".mp4")){
			contentType = "video/mp4";
		}			
				
		if (contentTypeMap.containsKey(contentType)){
			int value =  contentTypeMap.get(contentArray[0]);
			value++;
			contentTypeMap.put(contentArray[0], value);
		}
		else {
			contentTypeMap.put(contentArray[0], 1);
		}
		
		if (contentType.equals("text/html") || contentType.equals("application/pdf") || contentType.equals("application/msword") ||
				contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {						
		
			byte[] fileBytes = page.getContentData();
			long fileSize = fileBytes.length;
			if (fileSize < 1024){
				fileSizeLess1Kb++;
			}
			else if (fileSize >= 1024 && fileSize < 10240){
				fileSize1To10Kb++;
			}
			else if (fileSize>= 10240 && fileSize < 102400){
				fileSize10To100Kb++;
			}
			else if (fileSize>= 102400 && fileSize < 1048576){
				fileSize100KbTo1Mb++;
			}
			else if (fileSize>=1048576){
				fileSizeGreater1Mb++;
			}			
						
			ParseData parseData = page.getParseData();
			Set<WebURL> links = parseData.getOutgoingUrls();		

			/*
			 * Downloading and saving to a file
			 */
			String[] ab = contentType.split("/");
			String ext1 = ab[1];
			String ext = "";
			if (ext1.equals("html")){
				ext = ".html";
			}
			
			try {
				String fileNam = URLEncoder.encode(url,"UTF-8") + ext;
				pagerankdataWriter.append(fileNam);
				pagerankdataWriter.append(',');
				FileOutputStream fos = new FileOutputStream("/downloaded_files/" + fileNam );
				fos.write(page.getContentData());
				fos.close();			
			
				/*
				 * To handle outgoing links
				 */
				linksCount += links.size();
				Object[] arr = links.toArray();
				for (Object a : arr) {
					urlHashSet.add(a.toString());								
					
					urlsWriter.append(a.toString());					
					urlsWriter.append(',');
					
					pagerankdataWriter.append(a.toString());
					pagerankdataWriter.append(',');
					
					String urlForComparison = a.toString().toLowerCase();
					String indicator = indicateClass(urlForComparison);
					urlsWriter.append(indicator);
					urlsWriter.append('\n');						
				}
				pagerankdataWriter.append('\n');
										
				visitWriter.append(url);
				visitWriter.append(',');			
				visitWriter.append(Integer.toString(fileBytes.length));
				visitWriter.append(',');
				visitWriter.append(Integer.toString(links.size()));
				visitWriter.append(',');
				visitWriter.append(contentType);
				visitWriter.append('\n');					
										
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}		
	}	
	
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		try {			
			fetchWriter.append(webUrl.getURL());
			fetchWriter.append(',');
			fetchWriter.append(Integer.toString(statusCode));
			fetchWriter.append('\n');
			fetchesAttemptedCount++;
			if (statusCode >= 200 && statusCode < 300)
				fetchesSucceededCount++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if (statusCodeMap.containsKey(statusCode)){
			long value =  statusCodeMap.get(statusCode);
			value++;
			statusCodeMap.put(statusCode, value);
		}
		else {
			statusCodeMap.put(statusCode, (long) 1);
		}
	}
	
	/**
	 * This function is called by controller to get the local data of this crawler when job is finished
	 */
	@Override
	public Object getMyLocalData() {
		return myCrawlStat;
	}
	
	/**
	 * This function is called by controller before finishing the job.
	 * You can put whatever stuff you need here.
	 */
	 @Override
	 public void onBeforeExit() {		  	
		 try {
			 urlsWriter.flush();
			 urlsWriter.close();
			 visitWriter.flush();
			 visitWriter.close();
			 fetchWriter.flush();
			 fetchWriter.close();
			 pagerankdataWriter.flush();
			 pagerankdataWriter.close();
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 myCrawlStat.setFetchesAttemptedCount(fetchesAttemptedCount);
		 myCrawlStat.setFetchesSucceededCount(fetchesSucceededCount);
		 myCrawlStat.setTotalLinks(linksCount);
		 myCrawlStat.setUniqueTotalLinks(urlHashSet.size());
		 long uniqueTotalLinksWithinSchool = 0;
		 long uniqueTotalUSCLinksOutsideSchool = 0;
		 long uniqueTotalLinksOutsideUSC = 0;
		 for (String entry : urlHashSet){
			 if (entry.startsWith("https://cinema.usc.edu/")){
				 uniqueTotalLinksWithinSchool++;
			 }
			 else if (entry.contains("usc.edu")){
				 uniqueTotalUSCLinksOutsideSchool++;
			 }
			 else {
				 uniqueTotalLinksOutsideUSC++;
			 }
		  }
		  myCrawlStat.setUniqueTotalLinksWithinSchool(uniqueTotalLinksWithinSchool);
		  myCrawlStat.setUniqueTotalUSCLinksOutsideSchool(uniqueTotalUSCLinksOutsideSchool);
		  myCrawlStat.setUniqueTotalLinksOutsideUSC(uniqueTotalLinksOutsideUSC);		  
		  myCrawlStat.setStatusCodeAndCount(statusCodeMap);
		  myCrawlStat.setContentTypeMap(contentTypeMap);
		  myCrawlStat.setFileSizeLess1Kb(fileSizeLess1Kb);
		  myCrawlStat.setFileSize1To10Kb(fileSize1To10Kb);
		  myCrawlStat.setFileSize10To100Kb(fileSize10To100Kb);
		  myCrawlStat.setFileSize100KbTo1Mb(fileSize100KbTo1Mb);
		  myCrawlStat.setFileSizeGreater1Mb(fileSizeGreater1Mb);		 
	}
}
