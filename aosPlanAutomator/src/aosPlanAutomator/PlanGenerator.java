package aosPlanAutomator;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlanGenerator {
	int planId;
	int audienceSegmentTargetId;
	String newPackageId;
	APICredentials apiCredentials;
	String planLongId;
	JSONObject planHeaderCustomFields;
	String genreFilters = "";
	String tagFilters = "";
	List<Target> audienceTargets = new ArrayList<Target>();
	List<PeriodBudget> periodBudgets = new ArrayList<PeriodBudget>();
	
	public PlanGenerator(int planId) throws IOException {
		this.planId = planId;
		String json = new String(Files.readAllBytes(Paths.get("autoGenConfig.json")));
		JSONObject autoGenConfig = new JSONObject(json);
		this.audienceSegmentTargetId = autoGenConfig.getInt("audienceSegmentTargetId");
		this.newPackageId = autoGenConfig.getString("newPackageId");
	}
	
	public void generatePlan() throws IOException {
		this.apiCredentials = new APICredentials();
		
		JSONObject planHeaderJson = getPlanHeaderJson(planId, apiCredentials);
		this.planLongId = planHeaderJson.getString("id");
		this.planHeaderCustomFields = planHeaderJson.getJSONObject("customFieldValues");
		
		try {
			this.genreFilters = getGenreFilters(planHeaderCustomFields.getJSONArray("autogengenre"), apiCredentials);
		} catch (Exception e) {}
		
		try {
			this.tagFilters = getTagFilters(planHeaderCustomFields.getJSONArray("autogentype"), apiCredentials);
		} catch (Exception e) {}
		
		try {
			this.audienceTargets = getAudienceTargets(planHeaderCustomFields.getJSONArray("autogenaudience"), apiCredentials, audienceSegmentTargetId);
		} catch (Exception e) {}
		
		HTTPGetRequest getBudget = new HTTPGetRequest("GET", apiCredentials, 
				"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/goals/budget/" + planId,
				"Getting dollars, impressions and CPM from Budget to distribute to lines");
		
		try {
			this.periodBudgets = getPeriodBudgets(getBudget.getResponseObject().getJSONArray("budgetData"), 
					apiCredentials, 
					planHeaderJson.getJSONObject("calendar").getString("id"));
		} catch (Exception e) {
			System.out.println(e);
		}
		
		HTTPPostRequest getProducts = new HTTPPostRequest("POST", apiCredentials, 
				"https://{{envURL}}/products/v2/{{apiKey}}/products/_search", 
				"{\"productIdFilters\": []" + tagFilters + genreFilters + ",\"statusFilters\": [\"PUBLISHED\"]}",
				"Getting all published products that match the Inventory Types (tags) and Genres from the header");
		
		if (!getProducts.isNoResults()) {
			for (PeriodBudget pb : periodBudgets) {
				if (audienceTargets!=null) {
					for (Target audience : audienceTargets) {
						UUID uuid = UUID.randomUUID();
						DealGroupLine dealGroupLine = new DealGroupLine(planId, uuid.toString(), audience.getTargetOptionName() + " Package " + pb.getPeriodName(), newPackageId);
						
						DealLines dealLinesForGroup = new DealLines(planId);
						dealLinesForGroup.addDealGroupLine(dealGroupLine);
						
						HTTPPostRequest createGroupLine = new HTTPPostRequest("POST", apiCredentials, 
								"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId + "/workspace/digital", 
								dealLinesForGroup.toAOSCreateJson(),
								"Creating a group for " + audience.getTargetOptionName() + " for " + pb.getPeriodName());
						
						JSONArray products = getProducts.getResponseObject().getJSONArray("response");
						
						DealLines childLines = new DealLines(planId);
						for (int i = 0; i < products.length(); i++) {
							uuid = UUID.randomUUID();
							DealLine dealLine = new DealLine(planId, uuid.toString(), audience.getTargetOptionName() + " - " + products.getJSONObject(i).getString("productName"), pb.getStartDate(), pb.getEndDate(), products.getJSONObject(i).getString("productId"), 0, 0);
							dealLine.setTarget(audience);
							childLines.addDealLine(dealLine);
				        }
						
						childLines.optimizeToBudget(pb.getBudget()/audienceTargets.size(), pb.getImps()/audienceTargets.size());
						
						dealGroupLine.setDealLines(childLines);
						dealGroupLine.setGroupLineId(createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getString("lineId"));
						
						new HTTPPostRequest("POST", apiCredentials, 
								"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId + "/workspace/digital", 
								dealGroupLine.toAOSAddChildrenJson(),
								"Creating the child line items for " + audience.getTargetOptionName() + " for " + pb.getPeriodName());
						
						removeChildLine(apiCredentials, planLongId, 
								createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getString("lineId"), 
								createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getJSONArray("childLineMappings").getJSONObject(0).getString("lineId"));
					}
				} else {
					UUID uuid = UUID.randomUUID();
					DealGroupLine dealGroupLine = new DealGroupLine(planId, uuid.toString(), "Auto Generated Package", newPackageId);
					
					DealLines dealLinesForGroup = new DealLines(planId);
					dealLinesForGroup.addDealGroupLine(dealGroupLine);
					
					HTTPPostRequest createGroupLine = new HTTPPostRequest("POST", apiCredentials, 
							"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId + "/workspace/digital", 
							dealLinesForGroup.toAOSCreateJson(),
							"Creating a group for " + pb.getPeriodName());
					
					JSONArray products = getProducts.getResponseObject().getJSONArray("response");
					
					DealLines childLines = new DealLines(planId);
					for (int i = 0; i < products.length(); i++) {
						uuid = UUID.randomUUID();
						DealLine dealLine = new DealLine(planId, uuid.toString(), "Auto Gen Line " + (i+1), "2023-01-01", "2023-01-15", products.getJSONObject(i).getString("productId"), 578000, 32.5);
						childLines.addDealLine(dealLine);
			        }				
					
					childLines.optimizeToBudget(pb.getBudget(), pb.getImps());
					
					dealGroupLine.setDealLines(childLines);
					dealGroupLine.setGroupLineId(createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getString("lineId"));
					
					new HTTPPostRequest("POST", apiCredentials, 
							"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId + "/workspace/digital", 
							dealGroupLine.toAOSAddChildrenJson(),
							"Creating the child line items for " + pb.getPeriodName());
					
					removeChildLine(apiCredentials, planLongId, 
							createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getString("lineId"), 
							createGroupLine.getResponseObject().getJSONObject("createOperation").getJSONArray("lineMapping").getJSONObject(0).getJSONArray("childLineMappings").getJSONObject(0).getString("lineId"));
				}
			}
		}	
	}
	
	private static JSONObject getPlanHeaderJson(int planId, APICredentials apiCredentials) throws IOException {
		String getPlanHeaderUrl = "https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId;
		HTTPGetRequest getPlanHeader = new HTTPGetRequest("GET", apiCredentials, getPlanHeaderUrl, "Getting plan header details for plan " + planId);
		return getPlanHeader.getResponseObject();
	}

	private static List<PeriodBudget> getPeriodBudgets(JSONArray budgets, APICredentials apiCredentials,
			String calendarId) throws IOException {
		String getCalendarsUrl = "https://{{envURL}}/mdm/v1/{{apiKey}}/calendar/allCalendars";
		HTTPGetRequest getCalendars = new HTTPGetRequest("GET", apiCredentials, getCalendarsUrl, "Getting all calendars to understand date ranges in the plan's periods");
		
		List<PeriodBudget> periodBudgets = new ArrayList<PeriodBudget>();
		
		JSONArray calendarDefinitions = null;
		for (int i = 0; i < getCalendars.getResponseArray().length(); i++) {
			if (getCalendars.getResponseArray().getJSONObject(i).getString("id").equals(calendarId)) {
				calendarDefinitions = getCalendars.getResponseArray().getJSONObject(i).getJSONArray("calendarDefinitions");
				break;
			}
	    }
		
		for (int j = 0; j < budgets.length(); j++) {
			for (int i = 0; i < calendarDefinitions.length(); i++) {
				if (budgets.getJSONObject(j).getString("period").equals(calendarDefinitions.getJSONObject(i).getString("periodName"))) {
					periodBudgets.add(new PeriodBudget(
							calendarDefinitions.getJSONObject(i).getString("periodName"), 
							calendarDefinitions.getJSONObject(i).getString("startDate"), 
							calendarDefinitions.getJSONObject(i).getString("endDate"), 
							budgets.getJSONObject(j).getDouble("dollar"), 
							budgets.getJSONObject(j).getInt("imps") * 1000));
					break;
				}
		    }
		}
		
		return periodBudgets;
	}

	private static List<Target> getAudienceTargets(JSONArray planHeaderAudiences, APICredentials apiCredentials, int audienceSegmentTargetId) throws IOException {
		String getAudienceSegmentsUrl = "https://{{envURL}}/target/v1/{{apiKey}}/targets/" + audienceSegmentTargetId + "/options/_search";
		String optionSearch = "{\"names\": [";
		for (int i = 0; i < planHeaderAudiences.length(); i++) {
			optionSearch += "\"" + planHeaderAudiences.getJSONObject(i).getString("name") + "\",";
	    }
		optionSearch = optionSearch.substring(0, optionSearch.length() - 1) + "]}";
		
		HTTPPostRequest getAudienceSegments = new HTTPPostRequest("POST", apiCredentials, getAudienceSegmentsUrl, optionSearch, "Getting all audience targets to match to the header selections");
		
		List<Target> audienceTargets = new ArrayList<Target>();
		for (int i = 0; i < getAudienceSegments.getResponseObject().getJSONArray("targetOptions").length(); i++) {
			int id = getAudienceSegments.getResponseObject().getJSONArray("targetOptions").getJSONObject(i).getInt("id");
			String name = getAudienceSegments.getResponseObject().getJSONArray("targetOptions").getJSONObject(i).getString("name");
			audienceTargets.add(new Target(audienceSegmentTargetId, "Audience Segment", id, name));
	    }
		
		return audienceTargets;
	}

	private static String getTagFilters(JSONArray planHeaderInventoryTypes, APICredentials apiCredentials) throws IOException {
		String getTagsUrl = "https://{{envURL}}/mdm/v1/{{apiKey}}/tags";
		HTTPGetRequest getTags = new HTTPGetRequest("GET", apiCredentials, getTagsUrl, "Getting all tag data to find IDs to pass in the product filter");
		String tagFilters = ",\"tagFilters\": [";
		for (int i = 0; i < planHeaderInventoryTypes.length(); i++) {
			for (int j = 0; j < getTags.getResponseArray().length(); j++) {
				if (getTags.getResponseArray().getJSONObject(j).getString("name").equalsIgnoreCase(planHeaderInventoryTypes.getJSONObject(i).getString("name"))) {
					tagFilters += "\"" + getTags.getResponseArray().getJSONObject(j).getString("id") + "\",";
					break;
				}
			}
	    }
		tagFilters = tagFilters.substring(0, tagFilters.length() - 1) + "]";
		return tagFilters;
	}
	
	private static String getGenreFilters(JSONArray planHeaderGenres, APICredentials apiCredentials) throws IOException {
		String getGenresUrl = "https://{{envURL}}/mdm/v1/{{apiKey}}/genres";
		HTTPGetRequest getGenres = new HTTPGetRequest("GET", apiCredentials, getGenresUrl, "Getting all genres data to find IDs to pass in the product filter");
		String genreFilters = ",\"genreFilter\": [";
		for (int i = 0; i < planHeaderGenres.length(); i++) {
			for (int j = 0; j < getGenres.getResponseArray().length(); j++) {
				if (getGenres.getResponseArray().getJSONObject(j).getString("name").equalsIgnoreCase(planHeaderGenres.getJSONObject(i).getString("name"))) {
					genreFilters += "\"" + getGenres.getResponseArray().getJSONObject(j).getString("id") + "\",";
					break;
				}
			}
	    }
		genreFilters = genreFilters.substring(0, genreFilters.length() - 1) + "]";
		return genreFilters;
	}
	
	private static void removeChildLine(APICredentials apiCredentials, String planLongId, String groupLineId, String childLineId) throws IOException {
		String removeChildLineUrl = "https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planLongId + "/workspace/digital?version=1";
		String json = new String(Files.readAllBytes(Paths.get("removeChildLineTemplate.json")));
		json = json.replace("{{groupLineId}}", groupLineId);
		json = json.replace("{{childLineId}}", childLineId);
		new HTTPPostRequest("DELETE", apiCredentials, removeChildLineUrl, json, "Deleting the line item from the product that needed to be there for the \"empty\" package");
	}

	public void calculateInventory() throws IOException {
		HTTPPostRequest getDigitalDealLines = new HTTPPostRequest("POST", apiCredentials, 
				"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planLongId + "/workspace/digital/lines/_fetch", "", 
				"Getting all the deal lines from plan " + planId + " to pass in the inventory calculation request");
		String lineFilter = "{\"lineIds\":[";
		for (int i=0; i<getDigitalDealLines.getResponseArray().length(); i++) {
			if (getDigitalDealLines.getResponseArray().getJSONObject(i).getJSONObject("planWorkspaceProduct").getString("lineType").equals("STANDARD"))
				lineFilter += "\"" + getDigitalDealLines.getResponseArray().getJSONObject(i).getString("id") + "\",";
		}
		lineFilter = lineFilter.substring(0, lineFilter.length() - 1) + "]}";
		
		new HTTPPostRequest("POST", apiCredentials, 
				"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planLongId + "/workspace/digital/inventory/_calculate?version=1&inventoryType=SUMMARY", lineFilter,
				"Triggering inventory calculation for all child lines in plan " +planId);
	}	
	
	public void triggerExport() throws IOException {
		String json = new String(Files.readAllBytes(Paths.get("exportRequestData.json")));
		new HTTPPostRequest("POST", apiCredentials, 
				"https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/plans/" + planId + "/exports/digital/workspace/initiate?version=1", json,
				"Triggering the export for plan " + planId);
	}

	public void submitToFinal() throws IOException {
		new HTTPPostRequest("POST", apiCredentials, "https://{{envURL}}/unifiedplanner/v1/{{apiKey}}/workflow/plans/" + planId + "/transitions/245", "",
				"Submitting plan " + planId + " through workflow");
	}
}

