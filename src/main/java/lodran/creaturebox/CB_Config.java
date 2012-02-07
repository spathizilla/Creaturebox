package lodran.creaturebox;
import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class CB_Config {
	public static File dataDir;
	
	public static int messagePriority;
	public static int debugPriority;
	public static boolean enableRedstone;
	public static boolean operatorPermissions;
	public static boolean showPlacements;
	
	public static boolean usemySQL;
	public static String mySQLuname;
	public static String mySQLpass;
	public static String mySQLdb;
	public static String mySQLtable;
	public static String mySQLhost;
	public static int mySQLport;
	
	public static void initialize(FileConfiguration config, File dataFolder) {
		dataDir = dataFolder;
		
		messagePriority = config.getInt("messagePriority");
		debugPriority = config.getInt("debugPriority");
		enableRedstone = config.getBoolean("enableRedstone");
		operatorPermissions = config.getBoolean("operatorPermissions");
		showPlacements = config.getBoolean("showPlacements");
		
		ConfigurationSection confdatabase = config.getConfigurationSection("mysql");
		
		// Database
		usemySQL = confdatabase.getBoolean("enabled", false);
		mySQLhost = confdatabase.getString("host", "'localhost'");
		mySQLport = confdatabase.getInt("port", 3306);
		mySQLuname = confdatabase.getString("username", "'root'");
		mySQLpass = confdatabase.getString("password", "'password'");
		mySQLdb = confdatabase.getString("database", "'minecraft'");
		mySQLtable = confdatabase.getString("table", "'creaturebox'");
	}
}
