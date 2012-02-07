package lodran.creaturebox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.World;

public class CB_DataSource {
    public final static String sqlitedb = "/spawners.db";
    private final static String CB_TABLE = "CREATE TABLE IF NOT EXISTS `"+ CB_Config.mySQLtable +"` (" 
    	    + "`id` INTEGER PRIMARY KEY,"
    	    + "`Spawns` varchar(32) NOT NULL DEFAULT 'pig',"
            + "`World` varchar(32) NOT NULL DEFAULT '0'," 
            + "`X` DOUBLE NOT NULL DEFAULT '0'," 
            + "`Y` DOUBLE NOT NULL DEFAULT '0',"
            + "`Z` DOUBLE NOT NULL DEFAULT '0'," 
            + "`Limit` INTEGER NOT NULL DEFAULT '-1',"
            + "`Count` INTEGER NOT NULL DEFAULT '-1',"
            + "`Period` INTEGER NOT NULL DEFAULT '-1',"
            + "`Space` TINYINT(4) NOT NULL DEFAULT '1',"
            + "`Surface` TINYINT(4) NOT NULL DEFAULT '1',"
            + "`Player` TINYINT(4) NOT NULL DEFAULT '1',"
            + "`Light` TINYINT(4) NOT NULL DEFAULT '1'"
            + ");";
    private static CreatureboxPlugin _plugin;
    
    public static void initialize(CreatureboxPlugin plugin) {
    	CB_DataSource._plugin = plugin;
    	if (!tableExists()) {
    		createTable();
    	}
    	dbTblCheck();
    }
        
    public static World getWorld(String name) {
    	return _plugin.getServer().getWorld(name);
    }
    
    public static ArrayList<HashMap<String, Object>> loadSpawners() {
    	ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
    	
    	Statement statement = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		statement = conn.createStatement();
    		set = statement.executeQuery("SELECT * FROM `"+ CB_Config.mySQLtable + "`");
    		int size = 0;
    		while (set.next()) {
    			size++;
    			HashMap<String, Object> settings = new HashMap<String, Object>(); 
    			settings.put("id", set.getInt("id"));
    			settings.put("Period", (Integer) set.getInt("Period"));
    			settings.put("Spawns", (String) set.getString("Spawns"));
    			settings.put("Count", (Integer) set.getInt("Count"));
    			World world = getWorld(set.getString("World"));
    			Location loc = new Location(world, set.getDouble("X"), set.getDouble("Y"), set.getDouble("Z"));
    			settings.put("Location", loc);
    			ArrayList<String> requirements = new ArrayList<String>();
    			if(set.getInt("Player") == 1) { requirements.add("player"); }
    			if(set.getInt("Space") == 1) { requirements.add("space"); }
    			if(set.getInt("Surface") == 1) { requirements.add("surface"); }
    			if(set.getInt("Light") == 1) { requirements.add("light"); }
    			settings.put("Requirements", requirements);
    			settings.put("Limit", (Integer) set.getInt("Limit"));
    			ret.add(settings);
    		}
    		CB_Logger.info(size + " managed spawners loaded");
    	} catch (SQLException ex) {
    		CB_Logger.severe("Spawner DB Load Exception: " + ex);
    	} finally {
    		try {
    			if (statement != null) {
    				statement.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("DB Load Exception (on close)");
    		}
    	}
    	return ret;
    }

    public static void newSpawner(CB_Spawner spawner) {
    	if(spawner.getNatural()) {
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		ps = conn.prepareStatement("INSERT INTO `"+ CB_Config.mySQLtable + "` (`Spawns`, `World`, `X`, `Y`, `Z`, `Limit`, `Count`, `Period`, `Space`, `Surface`, `Player`, `Light`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
    		ps.setString(1, spawner.getCreatureType().getName());
    		ps.setString(2, spawner.getLocation().getWorld().getName());
    		ps.setDouble(3, spawner.getLocation().getBlockX());
    		ps.setDouble(4, spawner.getLocation().getBlockY());
    		ps.setDouble(5, spawner.getLocation().getBlockZ());
    		ps.setInt(6, spawner.getLimit());
    		ps.setInt(7, spawner.getCount());
    		ps.setInt(8, spawner.getPeriod());
    		ps.setInt(9, spawner.getReqs("space"));
    		ps.setInt(10, spawner.getReqs("surface"));
    		ps.setInt(11, spawner.getReqs("player"));
    		ps.setInt(12, spawner.getReqs("light"));
    		ps.executeUpdate();
    		conn.commit();

    		int insertid = -1;
    		ResultSet rs = ps.getGeneratedKeys();
    		if (rs.next()){
    		    insertid = rs.getInt(1);
    		}
    		rs.close();
    		spawner.setSpawnerID(insertid);
    	} catch (SQLException ex) {
    		CB_Logger.severe("Spawner Insert Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Spawner Insert Exception (on close)", ex);
    		}
    	}
    }
    
    public static void destroySpawner(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1 || spawner.getNatural()) {
    		return;
    	}
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("DELETE FROM `"+ CB_Config.mySQLtable + "` WHERE id = ?");
    		ps.setInt(1, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Spawner Delete Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Spawner Delete Exception (on close)", ex);
    		}
    	}
    }
    
    public static void updateSpawnerType(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1) {
    		newSpawner(spawner);
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE `"+ CB_Config.mySQLtable + "` SET `Spawns` = ? WHERE id = ?");
    		ps.setString(1, spawner.getCreatureType().getName());
    		ps.setInt(2, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Update Type Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Update Type Exception (on close)", ex);
    		}
    	}
    }
    
    public static void updateSpawnerCount(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1) {
    		newSpawner(spawner);
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE `"+ CB_Config.mySQLtable + "` SET `Count` = ? WHERE id = ?");
    		ps.setInt(1, spawner.getCount());
    		ps.setInt(2, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Update Count Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Update Count Exception (on close)", ex);
    		}
    	}
    }
    
    public static void updateSpawnerPeriod(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1) {
    		newSpawner(spawner);
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE `"+ CB_Config.mySQLtable + "` SET `Period` = ? WHERE id = ?");
    		ps.setInt(1, spawner.getPeriod());
    		ps.setInt(2, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Update Period Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Update Period Exception (on close)", ex);
    		}
    	}
    }
    
    public static void updateSpawnerLimit(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1) {
    		newSpawner(spawner);
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE `"+ CB_Config.mySQLtable + "` SET `Limit` = ? WHERE id = ?");
    		ps.setInt(1, spawner.getLimit());
    		ps.setInt(2, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Update Limit Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Update Limit Exception (on close)", ex);
    		}
    	}
    }
    
    public static void updateSpawnerReqs(CB_Spawner spawner) {
    	if(spawner.getSpawnerID() == -1) {
    		newSpawner(spawner);
    		return;
    	}
    	
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE `"+ CB_Config.mySQLtable + "` SET `Player` = ? AND `Space` = ? AND `Surface` = ? and `Light` = ? WHERE id = ?");
    		ps.setInt(1, spawner.getReqs("space"));
    		ps.setInt(2, spawner.getReqs("surface"));
    		ps.setInt(3, spawner.getReqs("player"));
    		ps.setInt(4, spawner.getReqs("light"));
    		ps.setInt(5, spawner.getSpawnerID());
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		CB_Logger.severe("Update Reqs Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Update Reqs Exception (on close)", ex);
    		}
    	}
    }
    
    private static boolean tableExists() {
    	ResultSet rs = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData dbm = conn.getMetaData();
    		rs = dbm.getTables(null, null, CB_Config.mySQLtable, null);
    		if (!rs.next()) {
    			return false;
    		}
    		return true;
    	} catch (SQLException ex) {
    		CB_Logger.severe("Table Check Exception", ex);
    		return false;
    	} finally {
    		try {
    			if (rs != null) {
    				rs.close();
    			}
    		} catch (SQLException ex) {
    			CB_Logger.severe("Table Check SQL Exception (on closing)");
    		}
    	}
    }

    private static void createTable() {
    	Statement st = null;
    	try {
    		CB_Logger.info("Creating Database...");
    		Connection conn = ConnectionManager.getConnection();
    		st = conn.createStatement();
    		st.executeUpdate(CB_TABLE);
    		conn.commit();

    		if(CB_Config.usemySQL){ 
    			// We need to set auto increment on SQL.
    			String sql = "ALTER TABLE `"+ CB_Config.mySQLtable + "` CHANGE `id` `id` INT NOT NULL AUTO_INCREMENT ";
    			CB_Logger.info("Modifying database for MySQL support");
    			st = conn.createStatement();
    			st.executeUpdate(sql);
    			conn.commit();
    		}
    	} catch (SQLException e) {
    		CB_Logger.severe("Create Table Exception", e);
    	} finally {
    		try {
    			if (st != null) {
    				st.close();
    			}
    		} catch (SQLException e) {
    			CB_Logger.severe("Could not create the table (on close)");
    		}
    	}
    }
    
    public static void dbTblCheck() {
    	// Add future modifications to the table structure here

    }

    public static void updateDB(String test, String sql) {
    	// Use same sql for both mysql/sqlite
    	updateDB(test, sql, sql);
    }

    public static void updateDB(String test, String sqlite, String mysql) {
    	// Allowing for differences in the SQL statements for mysql/sqlite.
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		Statement statement = conn.createStatement();
    		statement.executeQuery(test);
    		statement.close();
    	} catch(SQLException ex) {
    		CB_Logger.info("Updating database");
    		// Failed the test so we need to execute the updates
    		try {
    			String[] query;
    			if (CB_Config.usemySQL) {
    				query = mysql.split(";");
    			} else { 
    				query = sqlite.split(";");
    			}

    			Connection conn = ConnectionManager.getConnection();
    			Statement sqlst = conn.createStatement();
    			for (String qry : query) {
    				sqlst.executeUpdate(qry);
    			}
    			conn.commit();
    			sqlst.close();
    		} catch (SQLException exc) {
    			CB_Logger.severe("Failed to update the database to the new version - ", exc);
    			ex.printStackTrace();
    		}	
    	}
    }

    public static void updateFieldType(String field, String type) {
    	try {
    		// SQLite uses dynamic field typing so we dont need to process these.  
    		if (!CB_Config.usemySQL) return;

    		CB_Logger.info("Updating database");

    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData meta = conn.getMetaData();

    		ResultSet colRS = null;
    		colRS = meta.getColumns(null, null, CB_Config.mySQLtable, null);
    		while (colRS.next()) {
    			String colName = colRS.getString("COLUMN_NAME");
    			String colType = colRS.getString("TYPE_NAME");

    			if (colName.equals(field) && !colType.equals(type))
    			{
    				Statement stm = conn.createStatement();
    				stm.executeUpdate("ALTER TABLE `"+ CB_Config.mySQLtable + "` MODIFY " + field + " " + type + "; ");
    				conn.commit();
    				stm.close();
    				break;
    			}
    		}
    		colRS.close();
    	} catch(SQLException ex) {
    		CB_Logger.severe("Failed to update the database to the new version - ", ex);
    		ex.printStackTrace();
    	}
    }
}