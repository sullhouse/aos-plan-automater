package aosPlanAutomator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class APICredentials {
	String apiKey;
	String token;
	String tenantName;
	String userId;
	String password;
	String env_url;
	int UserIdNumber = 63;
	
	public APICredentials() throws IOException {
		//this.userId = System.getenv("USER_ID");
		//this.password = System.getenv("PASSWORD");
		//this.apiKey = System.getenv("API_KEY");
		//this.tenantName = System.getenv("TENANT_NAME");
		String configJsonString = new String(Files.readAllBytes(Paths.get("autoGenConfig.json")));
		JSONObject configJson = new JSONObject(configJsonString);
		this.userId = configJson.getString("user_id");
		this.password = configJson.getString("api_password");
		this.apiKey = configJson.getString("api_key");
		this.tenantName = configJson.getString("tenant_name");
		this.env_url = configJson.getString("env_url");
		
		JSONObject json = new JSONObject();
		json.put("apiKey", this.apiKey);
		json.put("expiration", 360);
		json.put("password", this.password);
		json.put("userId", this.userId);
		
		URL url = new URL("https://staging-api.aos.operative.com/mayiservice/tenant/" + this.tenantName);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.setDoOutput(true);
		
		try(OutputStream os = conn.getOutputStream()) {
		    byte[] input = json.toString().getBytes("utf-8");
		    os.write(input, 0, input.length);			
		}
		
		int responseCode = conn.getResponseCode();
		
		System.out.println("Success :: API Credentials for " + this.userId);
	
		try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			JSONObject responseJson = new JSONObject(response.toString());
			this.token = responseJson.getString("token");
		}
		
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getToken() {
		return token;
	}

	public String getTenantName() {
		return tenantName;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}
	
	public int getUserIdNumber() {
		return UserIdNumber;
	}

	public String getEnv_url() {
		return env_url;
	}

	public void setEnv_url(String env_url) {
		this.env_url = env_url;
	}
}
