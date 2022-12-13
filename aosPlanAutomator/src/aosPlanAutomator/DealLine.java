package aosPlanAutomator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DealLine {
	int planId;
	String externalLineId;
    String name;
    String startDate;
    String endDate;
    String productId;
    int quantity;
    Target target;
    double netUnitCost;
    
	public DealLine(int planId, String externalLineId, String name, String startDate, String endDate, 
			String productId, int quantity, double netUnitCost) {
		super();
		this.planId = planId;
		this.externalLineId = externalLineId;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.productId = productId;
		this.quantity = quantity;
		this.netUnitCost = netUnitCost;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public String getExternalLineId() {
		return externalLineId;
	}

	public void setExternalLineId(String externalLineId) {
		this.externalLineId = externalLineId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getNetUnitCost() {
		return netUnitCost;
	}

	public void setNetUnitCost(double netUnitCost) {
		this.netUnitCost = netUnitCost;
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}

	@Override
	public String toString() {
		return "DealLine [planId=" + planId + ", externalLineId=" + externalLineId + ", name=" + name + ", startDate="
				+ startDate + ", endDate=" + endDate + ", productId=" + productId
				+ ", quantity=" + quantity + ", netUnitCost=" + netUnitCost + "]";
	}	
	
	public String toAOSCreateJson() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("createDigitalLineTemplate.json")));
		json = json.replace("{{externalLineId}}", externalLineId);
		json = json.replace("{{name}}", name);
		json = json.replace("{{startDate}}", startDate);
		json = json.replace("{{endDate}}", endDate);
		json = json.replace("{{productId}}", productId);
		json = json.replace("{{quantity}}", "" + quantity);
		json = json.replace("{{netUnitCost}}", "" + netUnitCost);
		return json;
	}
	
	public String toAddToGroupJson() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("digitalChildLineTemplate.json")));
		json = json.replace("{{name}}", name);
		json = json.replace("{{startDate}}", startDate);
		json = json.replace("{{endDate}}", endDate);
		json = json.replace("{{productId}}", productId);
		json = json.replace("{{quantity}}", "" + quantity);
		if (target!=null) {
			String targetJson = new String(Files.readAllBytes(Paths.get("dealLineTargetsTemplate.json")));
			targetJson = targetJson.replace("{{targetId}}", "" + target.getTargetId());
			targetJson = targetJson.replace("{{targetOptionId}}", "" + target.getTargetOptionId());
			json = json.replace("{{targets}}", targetJson);
		} else {
			json = json.replace("{{targets}}", "");
		}
		json = json.replace("{{netUnitCost}}", "" + netUnitCost);
		return json;
	}
}