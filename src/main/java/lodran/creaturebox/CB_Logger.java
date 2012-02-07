package lodran.creaturebox;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CB_Logger {

    public static final Logger log = Logger.getLogger("Minecraft");

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[creaturebox] " + string, ex);

    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[creaturebox] " + string);
    }

    public static void info(String string) {
        log.log(Level.INFO, "[creaturebox] " + string);
    }

    public static void warning(String string) {
        log.log(Level.WARNING, "[creaturebox] " + string);
    }
}