package aosPlanAutomator;

public class PeriodBudget {
	String periodName;
	String startDate;
	String endDate;
	double budget;
	int imps;
	
	public PeriodBudget(String periodName, String startDate, String endDate, double budget, int imps) {
		super();
		this.periodName = periodName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.budget = budget;
		this.imps = imps;
	}
	public String getPeriodName() {
		return periodName;
	}
	public void setPeriodName(String periodName) {
		this.periodName = periodName;
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
	public double getBudget() {
		return budget;
	}
	public void setBudget(double budget) {
		this.budget = budget;
	}
	public int getImps() {
		return imps;
	}
	public void setImps(int imps) {
		this.imps = imps;
	}
	@Override
	public String toString() {
		return "PeriodBudget [periodName=" + periodName + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", budget=" + budget + ", imps=" + imps + "]";
	}
	
	
}
