package aosPlanAutomator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	public static void main(String[] args) throws IOException, JSONException, InterruptedException {
		try {
			if (args.length==0) {
				instructions();
			} else if (args[0].equalsIgnoreCase("-poll")) {
				DataStreamConsumer consumerThread = new DataStreamConsumer();
				consumerThread.start();
			} else if ((Integer.parseInt(args[1])>0)&&
					(args[0].equalsIgnoreCase("-planId"))) {
				int planId = Integer.parseInt(args[1]);
				
	    	    long startTime = System.currentTimeMillis();
	    	    
	    	    PlanGenerator planGenerator = new PlanGenerator(planId);
	    	    planGenerator.generatePlan();
	    	    
	    	    String json = new String(Files.readAllBytes(Paths.get("autoGenConfig.json")));
	    		JSONObject autoGenConfig = new JSONObject(json);
	    	    if (autoGenConfig.getBoolean("triggerExport")) 
	    	    	planGenerator.triggerExport();
	    	                    	    
	    	    long elapsedTime = ((System.currentTimeMillis() - startTime) / 1000) % 60;
	    	    System.out.println("Workspace populated in " + elapsedTime + " seconds");
	    	    if (autoGenConfig.getBoolean("calculateInventory")) 
	    	    	planGenerator.calculateInventory();
	    	    if (!args[2].isEmpty()&&args[2].equalsIgnoreCase("workflow")) {
		    	    WorkflowTask workflowTask = new WorkflowTask(planId);
		    	    APICredentials apiCredentials = new APICredentials();
		    	    workflowTask.getTaskId(apiCredentials);
		    	    workflowTask.addComment(apiCredentials, "Workspace populated in " + elapsedTime + " seconds");
		    	    workflowTask.approve(apiCredentials);
		    	    
		    	    System.out.println("Waiting for " + autoGenConfig.getInt("delayBeforeSubmitSeconds") + " seconds before submitting through workflow");
		    	    TimeUnit.SECONDS.sleep(autoGenConfig.getInt("delayBeforeSubmitSeconds"));
		    	    
		    	    if (autoGenConfig.getBoolean("submitToFinal")) 
		    	    	planGenerator.submitToFinal();
	    	    }
			} else {
				instructions();
			}
		} catch (NumberFormatException e) {
			instructions();
		}
	}
	
	private static void instructions() {
		System.out.println("Missing Arguments. Options:");
		System.out.println(" -poll			Start polling data streams");
		System.out.println(" -planid xxx		Process plan with ID xxx");
		System.out.println(" -planid xxx workflow	Process plan with ID xxx and advance through workflow");
	}
}