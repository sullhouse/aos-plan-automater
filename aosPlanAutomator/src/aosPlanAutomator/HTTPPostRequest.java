package aosPlanAutomator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class HTTPPostRequest {
	private String responseString;
	private JSONObject responseObject;
	private JSONArray responseArray;
	private String responseType;
	private boolean noResults = true;

	public HTTPPostRequest(String method, APICredentials apiCredentials, String urlString, String body, String... action) throws IOException{
		if(action.length > 0) System.out.println(action[0]);
		
		allowMethods("PATCH");
		
		urlString = urlString.replace("{{envURL}}", apiCredentials.getEnv_url());
	    urlString = urlString.replace("{{apiKey}}", apiCredentials.getApiKey());
	    
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
		conn.setRequestProperty("Authorization","Bearer " + apiCredentials.getToken());
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.getOutputStream().write(body.getBytes("UTF8"));

		int responseCode = conn.getResponseCode();
		
		System.out.println(responseCode + " on " + method + " " + urlString);
	
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer r = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				r.append(inputLine);
			}
			in.close();

			this.responseString = r.toString();
			Object json = new JSONTokener(this.responseString).nextValue();
			if (json instanceof JSONObject) {
				this.responseObject = new JSONObject(responseString);
				this.responseType = "JSONObject";
				this.noResults = false;
			} else if (json instanceof JSONArray) {
				this.responseArray = new JSONArray(responseString);
				this.responseType = "JSONArray";
				this.noResults = false;
			}
		} else {
			this.responseString = "Error";
			this.responseObject = null;
			this.responseArray = null;
		}
	}

	public String getResponseString() {
		return responseString;
	}

	public JSONArray getResponseArray() {
		return responseArray;
	}
	
	public JSONObject getResponseObject() {
		return responseObject;
	}
	
	public String getResponseType() {
		return responseType;
	}

	public boolean isNoResults() {
		return noResults;
	}

	public void setNoResults(boolean noResults) {
		this.noResults = noResults;
	}
	
	private static void allowMethods(String methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}