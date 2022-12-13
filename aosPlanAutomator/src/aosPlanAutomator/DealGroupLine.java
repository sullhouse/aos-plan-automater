package aosPlanAutomator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.core.IsNull;

public class DealGroupLine {
	int planId;
	String externalLineId;
    String name;
    String productId;
    String groupLineId;
    DealLines dealLines;
    
	public DealGroupLine(int planId, String externalLineId, String name, String productId, DealLines dealLines) {
		super();
		this.planId = planId;
		this.externalLineId = externalLineId;
		this.name = name;
		this.productId = productId;
		this.dealLines = dealLines;
	}
	
	public DealGroupLine(int planId, String externalLineId, String name, String productId) {
		super();
		this.planId = planId;
		this.externalLineId = externalLineId;
		this.name = name;
		this.productId = productId;
		this.dealLines = null;
	}

	public DealLines getDealLines() {
		return dealLines;
	}

	public void setDealLines(DealLines dealLines) {
		this.dealLines = dealLines;
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

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public String getGroupLineId() {
		return groupLineId;
	}

	public void setGroupLineId(String groupLineId) {
		this.groupLineId = groupLineId;
	}

	@Override
	public String toString() {
		return "DealGroupLine [planId=" + planId + ", externalLineId=" + externalLineId + ", name=" + name
				+ ", productId=" + productId + ", dealLines=" + dealLines + "]";
	}

	public String toAOSCreateJson() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("createDigitalGroupTemplate.json")));
		json = json.replace("{{externalLineId}}", externalLineId);
		json = json.replace("{{name}}", name);
		json = json.replace("{{productId}}", productId);
		return json;
	}
	
	public String toAOSAddChildrenJson() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("addChildrenToDigitalGroupTemplate.json")));
		json = json.replace("{{externalLineId}}", this.externalLineId);
		json = json.replace("{{groupLineId}}", this.groupLineId);
		json = json.replace("{{childLines}}", this.dealLines.toAddChildrenToGroupJson());
		return json;
	}
}