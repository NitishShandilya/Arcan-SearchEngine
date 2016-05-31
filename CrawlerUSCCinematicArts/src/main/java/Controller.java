import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	//private static final Logger logger = LoggerFactory.getLogger(Controller.class);	
	public static void main(String[] args) throws Exception {
		String crawlStorageFolder = "data/crawl/log";
		int numberOfCrawlers = 7;
		int maxPagesToFetch = 5000;
		int maxDepthOfCrawling = 10;
		int politenessDelay = 1000;		
		
		Map<Integer,Long> finalStatusCodeMap = new HashMap<Integer,Long>();
		Map<String,Integer> finalContentTypeMap = new HashMap<String,Integer>();
		
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxPagesToFetch(maxPagesToFetch);
		config.setPolitenessDelay(politenessDelay);
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		config.setIncludeBinaryContentInCrawling(true);				
		
		/*
		 * Instantiating the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
				
		//Crux of the crawler: To add the seed and start the crawler
		controller.addSeed("https://cinema.usc.edu/");		
		controller.start(MyCrawler.class, numberOfCrawlers);
				
		List<Object> crawlersLocalData = controller.getCrawlersLocalData();
	    long totalLinks = 0;
	    long uniqueTotalLinks = 0;
	    long uniqueTotalLinksWithinSchool = 0;
		long uniqueTotalUSCLinksOutsideSchool = 0;
		long uniqueTotalLinksOutsideUSC = 0;
	    int fetchesAttempted = 0;
	    int fetchesSucceeded = 0;
	    int fileSizeLess1Kb =0;
	    int fileSize1To10Kb =0;
	    int fileSize10To100Kb =0;
	    int fileSize100KbTo1Mb =0;
	    int fileSizeGreater1Mb = 0;
	    
	    for (Object localData : crawlersLocalData) {
	    	CrawlStat stat = (CrawlStat) localData;
	    	totalLinks += stat.getTotalLinks();
	    	uniqueTotalLinks += stat.getUniqueTotalLinks();
	    	uniqueTotalLinksWithinSchool += stat.getUniqueTotalLinksWithinSchool();
	    	uniqueTotalUSCLinksOutsideSchool += stat.getUniqueTotalUSCLinksOutsideSchool();
	    	uniqueTotalLinksOutsideUSC += stat.getUniqueTotalLinksOutsideUSC();
	    	fetchesAttempted += stat.getFetchesAttemptedCount();
	    	fetchesSucceeded += stat.getFetchesSucceededCount();
	    	fileSizeLess1Kb += stat.getFileSizeLess1Kb();
	    	fileSize1To10Kb += stat.getFileSize1To10Kb();
	    	fileSize10To100Kb += stat.getFileSize10To100Kb();
	    	fileSize100KbTo1Mb += stat.getFileSize100KbTo1Mb();
	    	fileSizeGreater1Mb += stat.getFileSizeGreater1Mb();

	    	Map<Integer,Long> crawlerStatusCodeAndCount = stat.getStatusCodeAndCount();
	    	for (Map.Entry<Integer, Long> entry : crawlerStatusCodeAndCount.entrySet()) {
	    		int statusCode = entry.getKey();
	    		long statusCodeCount = entry.getValue();
	    		if (finalStatusCodeMap.containsKey(statusCode)){
	    			long value =  finalStatusCodeMap.get(statusCode);
	    			value += statusCodeCount;
	    			finalStatusCodeMap.put(statusCode, value);
	    		}
	    		else {
	    			finalStatusCodeMap.put(statusCode, statusCodeCount);
	    		}
	    	}
	    	
	    	Map<String,Integer> contentTypeMap = stat.getContentTypeMap();
	    	for (Map.Entry<String,Integer> entry : contentTypeMap.entrySet()) {
	    		String contentType = entry.getKey();
	    		int count = entry.getValue();
	    		if (finalContentTypeMap.containsKey(contentType)){
	    			int value =  finalContentTypeMap.get(contentType);
	    			value += count;
	    			finalContentTypeMap.put(contentType, value);
	    		}
	    		else {
	    			finalContentTypeMap.put(contentType, count);
	    		}
	    	}
	    }
	    PrintWriter writer = new PrintWriter("AggregatedStatistics.txt", "UTF-8");
	    writer.println("Aggregated Statistics:");
	    writer.println("\tFetch statistics:");
	    writer.println("\t\tfetches attempted " + fetchesAttempted);
	    writer.println("\t\tfetches succeeded " + fetchesSucceeded);
	    writer.println("\t\tfetches failed or aborted " + (fetchesAttempted - fetchesSucceeded));
	    
	    writer.println("\tOutgoing URL's:");
	    writer.println("\t\tTotal URL's extracted " + totalLinks);
	    writer.println("\t\tUnique URL's extracted " + uniqueTotalLinks);
	    writer.println("\t\tUnique URL's within school " + uniqueTotalLinksWithinSchool);
	    writer.println("\t\tUnique USC URL's outside school " + uniqueTotalUSCLinksOutsideSchool);
	    writer.println("\t\tUnique URL's outside USC " + uniqueTotalLinksOutsideUSC);
	    	   
	    writer.println("\tStatus Codes:");
	    for (Map.Entry<Integer, Long> entry : finalStatusCodeMap.entrySet()) {
	    	System.out.println("\t\t" + entry.getKey() + " : " + entry.getValue());
	    }
	    writer.println("\tFile sizes:");
	    writer.println("\t\t< 1KB: " + fileSizeLess1Kb);
	    writer.println("\t\t1KB ~ <10KB: " + fileSize1To10Kb);
	    writer.println("\t\t10KB ~ <100KB: " + fileSize10To100Kb);
	    writer.println("\t\t100KB ~ <1MB: " + fileSize100KbTo1Mb);
	    writer.println("\t\t>= 1MB: " + fileSizeGreater1Mb);
	    
	    writer.println("\tContent Types:");
	    for (Map.Entry<String, Integer> entry : finalContentTypeMap.entrySet()) {
	    	writer.println("\t\t" + entry.getKey() + " : " + entry.getValue());
	    }
	    writer.close();
	}
}