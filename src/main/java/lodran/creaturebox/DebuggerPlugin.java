package lodran.creaturebox;

import java.io.File;
import java.io.IOException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.ChatColor;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class DebuggerPlugin extends JavaPlugin
{
  public final static int priorityError = 0;
  public final static int priorityWarning = 1;
  public final static int priorityInfo = 2;
  public final static int priorityNoise = 3;
    
  public static String pluginName = "debugger";
  
  private static int debugPriority = priorityInfo;

  /******************************************************************************/
  
  public void onEnable()
  {
    
    this.loadConfiguration();
  }

  /******************************************************************************/

  public void onDisable()
  {

  }

  /******************************************************************************/

  public boolean onCommand(CommandSender inSender,
                           Command inCommand,
                           String inCommandLabel,
                           String[] inArguments)
  {
    if (inArguments.length < 1)
    {
      return false;
    }
    
    if (inArguments[0].equalsIgnoreCase("debug") == false)
    {
      return false;
    }
    
    if (inArguments.length > 2)
    {
      showDebugUsage(inSender);
      return true;
    }
    
    Integer thePriority = null;
    
    if (inArguments.length == 2)
    {
      thePriority = __priorityIndexByName.get(inArguments[1].toLowerCase());
      
      if (thePriority == null)
      {
        showDebugUsage(inSender);
        return true;
      }
    }
        
    if ((__debuggers.containsKey(inSender)) && (thePriority == null))
    {
      notify(inSender, __debuggers.get(inSender), "you are no longer receiving debug messages.");
      __debuggers.remove(inSender);
    }
    else
    {
      if (thePriority == null)
      {
        thePriority = debugPriority;
      }
      
      __debuggers.put(inSender, thePriority);
      
      notify(inSender, thePriority, "you are now receiving " + __priorityNameByIndex[thePriority] + " messages.");
    }
    
    return true;
  }
  
  /******************************************************************************/
  
  public void showDebugUsage(CommandSender inSender)
  {
    inSender.sendMessage(ChatColor.AQUA + pluginName + " debug usage:");
    inSender.sendMessage(ChatColor.AQUA + "/" + pluginName +" debug");
    inSender.sendMessage(ChatColor.AQUA + "  Turn debugging messages on or off.");
  }
  
  /******************************************************************************/

  protected void loadConfiguration()
  {
    DebuggerPlugin.debugPriority = CreatureboxPlugin._debugPriority;
  }
  
  /******************************************************************************/
  
  public static void notify(int inPriority, String inMessage)
  {    
    if (inPriority <= debugPriority)
    {
      System.out.print("["+pluginName + "] " + inMessage);
    }
    
    // TODO: Debug messages seem to go missing when a player logs out and back in.
    
    for (CommandSender theDebugger : __debuggers.keySet())
    {
      if (inPriority <= __debuggers.get(theDebugger))
      {
        theDebugger.sendMessage(__debugColorByIndex[inPriority] + pluginName + ": " + inMessage);
      }
    }
  }
  
  /******************************************************************************/
  
  public static void notify(CommandSender inSender, int inPriority, String inMessage)
  {
    if (__debuggers.containsKey(inSender))
    {
      if (inPriority <= __debuggers.get(inSender))
      {
        inSender.sendMessage(__debugColorByIndex[inPriority] + pluginName + ": " + inMessage);
      }
    }
  }
  
  /******************************************************************************/
  
  private final static HashMap<CommandSender, Integer> __debuggers = new HashMap<CommandSender, Integer>();
  private final static ChatColor __debugColorByIndex[] = { ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN };

  private static HashMap<String, Integer> __priorityIndexByName = new HashMap<String, Integer>();
  private static String __priorityNameByIndex[] = { "error", "warning", "info", "noise" };
  
  static
  {    
    __priorityIndexByName.put("error", priorityError);
    __priorityIndexByName.put("warning", priorityWarning);
    __priorityIndexByName.put("info", priorityInfo);
    __priorityIndexByName.put("noise", priorityNoise);
  }
}