import java.util.Map;

public class CrawlStat {
	private int fetchesAttemptedCount;
	private int fetchesSucceededCount;
	private int fileSizeLess1Kb;
	private int fileSize1To10Kb;
	private int fileSize10To100Kb;
	private int fileSize100KbTo1Mb;
	private int fileSizeGreater1Mb;
	  private long totalLinks;
	  private long uniqueTotalLinks;
	  private long uniqueTotalLinksWithinSchool;
	  private long uniqueTotalUSCLinksOutsideSchool;
	  private long uniqueTotalLinksOutsideUSC;
	  private Map<Integer,Long> statusCodeAndCount;
	  private Map<String,Integer> contentTypeMap;
	  private long totalTextSize;	
	  
	  public int getFetchesAttemptedCount() {
		return fetchesAttemptedCount;
	  }

	  public void setFetchesAttemptedCount(int fetchesAttemptedCount) {
		this.fetchesAttemptedCount = fetchesAttemptedCount;
	  }

	  public int getFetchesSucceededCount() {
		return fetchesSucceededCount;
	  }

	  public void setFetchesSucceededCount(int fetchesSucceededCount) {
		this.fetchesSucceededCount = fetchesSucceededCount;
	  }
		
	  public long getTotalLinks() {
	    return totalLinks;
	  }

	  public void setTotalLinks(long totalLinks) {
	    this.totalLinks = totalLinks;
	  }

	  public long getUniqueTotalLinksWithinSchool() {
		return uniqueTotalLinksWithinSchool;
	}

	public void setUniqueTotalLinksWithinSchool(long uniqueTotalLinksWithinSchool) {
		this.uniqueTotalLinksWithinSchool = uniqueTotalLinksWithinSchool;
	}

	public long getUniqueTotalUSCLinksOutsideSchool() {
		return uniqueTotalUSCLinksOutsideSchool;
	}

	public void setUniqueTotalUSCLinksOutsideSchool(long uniqueTotalUSCLinksOutsideSchool) {
		this.uniqueTotalUSCLinksOutsideSchool = uniqueTotalUSCLinksOutsideSchool;
	}

	public long getUniqueTotalLinksOutsideUSC() {
		return uniqueTotalLinksOutsideUSC;
	}

	public void setUniqueTotalLinksOutsideUSC(long uniqueTotalLinksOutsideUSC) {
		this.uniqueTotalLinksOutsideUSC = uniqueTotalLinksOutsideUSC;
	}

	public Map<Integer, Long> getStatusCodeAndCount() {
		return statusCodeAndCount;
	}

	public void setStatusCodeAndCount(Map<Integer, Long> statusCodeAndCount) {
		this.statusCodeAndCount = statusCodeAndCount;
	}

	public long getTotalTextSize() {
	    return totalTextSize;
	  }

	  public void setTotalTextSize(long totalTextSize) {
	    this.totalTextSize = totalTextSize;
	  }

	  public void incTotalLinks(int count) {
	    this.totalLinks += count;
	  }

	  public void incTotalTextSize(int count) {
	    this.totalTextSize += count;
	  }

	public long getUniqueTotalLinks() {
		return uniqueTotalLinks;
	}

	public void setUniqueTotalLinks(long uniqueTotalLinks) {
		this.uniqueTotalLinks = uniqueTotalLinks;
	}

	public int getFileSizeLess1Kb() {
		return fileSizeLess1Kb;
	}

	public void setFileSizeLess1Kb(int fileSizeLess1Kb) {
		this.fileSizeLess1Kb = fileSizeLess1Kb;
	}

	public int getFileSize1To10Kb() {
		return fileSize1To10Kb;
	}

	public void setFileSize1To10Kb(int fileSize1To10Kb) {
		this.fileSize1To10Kb = fileSize1To10Kb;
	}

	public int getFileSize10To100Kb() {
		return fileSize10To100Kb;
	}

	public void setFileSize10To100Kb(int fileSize10To100Kb) {
		this.fileSize10To100Kb = fileSize10To100Kb;
	}

	public int getFileSize100KbTo1Mb() {
		return fileSize100KbTo1Mb;
	}

	public void setFileSize100KbTo1Mb(int fileSize100KbTo1Mb) {
		this.fileSize100KbTo1Mb = fileSize100KbTo1Mb;
	}

	public int getFileSizeGreater1Mb() {
		return fileSizeGreater1Mb;
	}

	public void setFileSizeGreater1Mb(int fileSizeGreater1Mb) {
		this.fileSizeGreater1Mb = fileSizeGreater1Mb;
	}

	public Map<String,Integer> getContentTypeMap() {
		return contentTypeMap;
	}

	public void setContentTypeMap(Map<String,Integer> contentTypeMap) {
		this.contentTypeMap = contentTypeMap;
	}	
}
