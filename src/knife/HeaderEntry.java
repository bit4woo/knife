package knife;

public class HeaderEntry {
	
	private String headerSource;//url or host
	private String headerName;
	private String headerValue;
	private String targetUrl;
	long birthTime;
	long endTime;
	
	
	public HeaderEntry() {
	}


	public HeaderEntry(String headerSource, String headerName, String headerValue, String targetUrl) {
		this.headerSource = headerSource;
		this.headerName = headerName;
		this.headerValue = headerValue;
		this.targetUrl = targetUrl;
		this.birthTime = System.currentTimeMillis();
	}
	
	
	
	public HeaderEntry(String headerSource, String headerName, String headerValue, String targetUrl,
			long birthTime, long endTime) {
		this.headerSource = headerSource;
		this.headerName = headerName;
		this.headerValue = headerValue;
		this.targetUrl = targetUrl;
		this.birthTime = birthTime;
		this.endTime = endTime;
	}
	public String getHeaderSource() {
		return headerSource;
	}
	public void setHeaderSource(String headerSource) {
		this.headerSource = headerSource;
	}
	public String getHeaderName() {
		return headerName;
	}
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}
	public String getHeaderValue() {
		return headerValue;
	}
	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	public long getBirthTime() {
		return birthTime;
	}
	public void setBirthTime(long birthTime) {
		this.birthTime = birthTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
}
