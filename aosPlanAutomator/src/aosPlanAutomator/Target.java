package aosPlanAutomator;

public class Target {
	private int targetId;
	private String targetName;
	private int targetOptionId;
	private String targetOptionName;
	
	public Target(int targetId, String targetName, int targetOptionId, String targetOptionName) {
		super();
		this.targetId = targetId;
		this.targetName = targetName;
		this.targetOptionId = targetOptionId;
		this.targetOptionName = targetOptionName;
	}
	public int getTargetId() {
		return targetId;
	}
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public int getTargetOptionId() {
		return targetOptionId;
	}
	public void setTargetOptionId(int targetOptionId) {
		this.targetOptionId = targetOptionId;
	}
	public String getTargetOptionName() {
		return targetOptionName;
	}
	public void setTargetOptionName(String targetOptionName) {
		this.targetOptionName = targetOptionName;
	}
	@Override
	public String toString() {
		return "Target [targetId=" + targetId + ", targetName=" + targetName + ", targetOptionId=" + targetOptionId
				+ ", targetOptionName=" + targetOptionName + "]";
	}
}
