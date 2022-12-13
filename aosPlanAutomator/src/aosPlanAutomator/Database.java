package aosPlanAutomator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Delayed;

public class Database {
	String url = System.getenv("DATABASE_URL");
	String username = System.getenv("DATABASE_USERNAME");
	String password = System.getenv("DATABASE_PASSWORD");

	public Database() {
		
	}
	
	public void insertDeal(int deal_id, String deal_name, String status, String start_date, String end_date, String advertiser) {
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			String insertStatement = "INSERT INTO aos_optimizer.deals (deal_id, deal_name, status, start_date, end_date, advertiser) VALUES (" + deal_id + ", `" + deal_name + "`, '" + status + "', '" + start_date + "', '" + end_date + "', `" + advertiser + "`);";
			connection.prepareStatement(insertStatement).execute();
			System.out.println("Executed to dB: " + insertStatement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertDeals(List<DealHeader> dealHeaders) {
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			for (DealHeader dealHeader : dealHeaders) {
				String insertStatement = "INSERT INTO aos_optimizer.deals (deal_id, deal_name, status, start_date, end_date, advertiser) VALUES (" + dealHeader.getDeal_id() + ", '" + dealHeader.getDeal_name().replace("'", "\\'") + "', '" + dealHeader.getStatus() + "', '" + dealHeader.getStart_date() + "', '" + dealHeader.getEnd_date() + "', '" + dealHeader.getAdvertiser().replace("'","\\'") + "');";
				connection.prepareStatement(insertStatement).execute();
				System.out.println("Executed to dB: " + insertStatement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
