package aosPlanAutomator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DealLines {
	int planId;
	List<DealLine> dealLines = new ArrayList<DealLine>();
	List<DealGroupLine> dealGroupLines = new ArrayList<DealGroupLine>();
	int numLines = 0;
	
	public DealLines(int planId) {
		super();
		this.planId = planId;
	}
	
	public DealLines() {
		super();
	}
    
	public void addDealLine(DealLine dealLine) {
		this.dealLines.add(dealLine);
		this.numLines++;
	}
	
	public void addDealGroupLine(DealGroupLine dealGroupLine) {
		this.dealGroupLines.add(dealGroupLine);
		this.numLines++;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public List<DealLine> getDealLines() {
		return dealLines;
	}

	public void setDealLines(List<DealLine> dealLines) {
		this.dealLines = dealLines;
		this.numLines = dealLines.size();
	}
	
	public void optimizeToBudget(double budget, int imps) {
		for (DealLine dl : dealLines) {
			dl.setQuantity(imps/this.numLines);
			dl.setNetUnitCost(1000 * (budget/this.numLines) / dl.getQuantity());
		}
	}
	
	public String toAOSCreateJson() throws IOException {
		String json = "[";
		Boolean first = true;
		for (DealLine dl : dealLines) {
			if (first) {
				json += dl.toAOSCreateJson();
				first = false;
			} else {
				json += "," + dl.toAOSCreateJson();
			}
		}
		for (DealGroupLine dg : dealGroupLines) {
			if (first) {
				json += dg.toAOSCreateJson();
				first = false;
			} else {
				json += "," + dg.toAOSCreateJson();
			}
		}
		json += "]";
		return	json;
	}
	
	public String toAddChildrenToGroupJson() throws IOException {
		String json = "[";
		Boolean first = true;
		for (DealLine dl : dealLines) {
			if (first) {
				json += dl.toAddToGroupJson();
				first = false;
			} else {
				json += "," + dl.toAddToGroupJson();
			}
		}
		json += "]";
		return	json;
	}
	
	public List<DealGroupLine> getDealGroupLines() {
		return dealGroupLines;
	}

	public void setDealGroupLines(List<DealGroupLine> dealGroupLines) {
		this.dealGroupLines = dealGroupLines;
	}
}