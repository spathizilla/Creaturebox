package lodran.creaturebox;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public class CB_Config {
	public static int messagePriority;
	public static int debugPriority;
	public static boolean enableRedstone;
	public static boolean operatorPermissions;
	public static boolean showPlacements;
	public static boolean legacyData;
	
	
	public static void initialize(FileConfiguration config) {
		messagePriority = config.getInt("messagePriority");
		debugPriority = config.getInt("debugPriority");
		enableRedstone = config.getBoolean("enableRedstone");
		operatorPermissions = config.getBoolean("operatorPermissions");
		showPlacements = config.getBoolean("showPlacements");
		legacyData = config.getBoolean("legacyData");
	}
}
