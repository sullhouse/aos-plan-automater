package aosPlanAutomator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WorkflowTask {
	int taskId;
	int planId;
	
	public WorkflowTask(int taskId, int planId) {
		super();
		this.taskId = taskId;
		this.planId = planId;
	}
	public WorkflowTask(int planId) {
		super();
		this.planId = planId;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public int getPlanId() {
		return planId;
	}
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	
	public int getTaskId(APICredentials apiCredentials) throws IOException {
		HTTPPostRequest getTask = new HTTPPostRequest("POST", apiCredentials, 
				"https://{{envURL}}/unison/v1/{{apiKey}}/tasks/entity/planner/filters", 
				"{\"assignees\":[" + apiCredentials.getUserIdNumber() + "],\"dealId\":\"" + this.planId + "\",\"taskStatuses\":[\"open\"],\"page\":0,\"size\":20}",
				"Getting the details of the approval task assigned to the auto-plan generator");
		
		this.taskId = getTask.getResponseObject().getJSONArray("content").getJSONObject(0).getInt("taskId");
		return this.taskId;
	}
	
	public void approve(APICredentials apiCredentials) throws IOException {
		new HTTPPostRequest("PATCH", apiCredentials, 
				"https://{{envURL}}/unison/v1/{{apiKey}}/tasks/approve/" + taskId,"",
				"Approving the approval task assigned to the auto-plan generator");
	}
	public void addComment(APICredentials apiCredentials, String comment) throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("addCommentToTaskTemplate.json")));
		json = json.replace("{{comment}}", comment);
		json = json.replace("{{taskId}}", "" + taskId);
		new HTTPPostRequest("POST", apiCredentials, "https://{{envURL}}/notes/v1/{{apiKey}}/notes", json,
				"Adding a comment to the approval task assigned to the auto-plan generator: \"" + comment + "\"");
	}
}
