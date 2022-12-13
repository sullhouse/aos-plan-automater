package aosPlanAutomator;

import org.json.JSONObject;

public class DealHeader {
	int deal_id;
	String deal_name;
	String status;
	String start_date;
	String end_date;
	String advertiser;
	
	public DealHeader(JSONObject planHeaderJson) {
		this.deal_id = planHeaderJson.getInt("planId");
		this.deal_name = planHeaderJson.getString("planName");
		this.status = planHeaderJson.getJSONObject("planStatus").getString("statusName");
		this.start_date = planHeaderJson.getString("startDate");
		this.end_date = planHeaderJson.getString("endDate");
		this.advertiser = planHeaderJson.getJSONArray("advertisers").getJSONObject(0).getString("name");
	}
	
	public DealHeader(int deal_id, String deal_name, String status, String start_date, String end_date, String advertiser) {
		super();
		this.deal_id = deal_id;
		this.deal_name = deal_name;
		this.status = status;
		this.start_date = start_date;
		this.end_date = end_date;
		this.advertiser = advertiser;
	}
	
	public int getDeal_id() {
		return deal_id;
	}
	public void setDeal_id(int deal_id) {
		this.deal_id = deal_id;
	}
	public String getDeal_name() {
		return deal_name;
	}
	public void setDeal_name(String deal_name) {
		this.deal_name = deal_name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStart_date() {
		return start_date;
	}
	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public String getAdvertiser() {
		return advertiser;
	}
	public void setAdvertiser(String advertiser) {
		this.advertiser = advertiser;
	}

	@Override
	public String toString() {
		return "DealHeader [deal_id=" + deal_id + ", deal_name=" + deal_name + ", status=" + status + ", start_date="
				+ start_date + ", end_date=" + end_date + ", advertiser=" + advertiser + "]";
	}
	
	
}
