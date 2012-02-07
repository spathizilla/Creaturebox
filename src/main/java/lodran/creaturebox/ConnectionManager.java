package lodran.creaturebox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
	private static Connection conn;

	public static Connection initialize() {
		try {
			if(CB_Config.usemySQL) {
				Class.forName("com.mysql.jdbc.Driver");
				String mySQLconn = "jdbc:mysql://" + CB_Config.mySQLhost + ":" + CB_Config.mySQLport + "/" + CB_Config.mySQLdb; 
				conn = DriverManager.getConnection(mySQLconn, CB_Config.mySQLuname, CB_Config.mySQLpass);
				conn.setAutoCommit(false);
				return conn;
			} else {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:" + CB_Config.dataDir.getAbsolutePath() + "/spawners.db");
				conn.setAutoCommit(false);
				return conn;
			}
		} catch (SQLException ex) {
			CreatureboxPlugin.notify(CreatureboxPlugin.priorityError, "SQL exception on init: " + ex.toString());
		} catch (ClassNotFoundException ex) {
			CreatureboxPlugin.notify(CreatureboxPlugin.priorityError, "SQL Libraries not found: " + ex.toString());
		}
		return conn;
	}

	public static Connection getConnection() {
		if(conn == null) conn = initialize();
		if(CB_Config.usemySQL) {
			// We probably dont need to do this for SQLite. 
			try {
				if(!conn.isValid(10)) conn = initialize();
			} catch (SQLException ex) {
				CreatureboxPlugin.notify(CreatureboxPlugin.priorityError, "Failed to check SQL status: " + ex.toString());
			}
		}
		return conn;
	}

	public static void closeConnection() {
		if(conn != null) {
			try {
				if(CB_Config.usemySQL){
					if(conn.isValid(10)) {
						conn.close();
					}
					conn = null;
				} else {
					conn.close();
					conn = null;
				}
			} catch (SQLException ex) {
				CreatureboxPlugin.notify(CreatureboxPlugin.priorityError, "SQL Exception on close: " + ex.toString());
			}
		}
	}
}
