package lodran.creaturebox;

import org.bukkit.entity.CreatureType;

public class CB_Utils {
	
	  public static CreatureType getMobFromDurability(int theCreatureIndex) {
		 CreatureType cType;
		  switch(theCreatureIndex) {
		  	case 90:	cType = CreatureType.PIG ; 		break;	// pig
		  	case 93:	cType = CreatureType.CHICKEN; 	break;	// chicken
		  	case 92:	cType = CreatureType.COW; 		break;	// cow
		  	case 91:	cType = CreatureType.SHEEP;		break;	// sheep
		  	case 94:	cType = CreatureType.SQUID;		break;	// squid
		  	case 50:	cType = CreatureType.CREEPER;	break;	// creeper
		  	case 56:	cType = CreatureType.GHAST; 	break;	// ghast
		  	case 57:	cType = CreatureType.PIG_ZOMBIE; break;	// pig zombie
		  	case 51:	cType = CreatureType.SKELETON; 	break;	// skeleton
		  	case 52:	cType = CreatureType.SPIDER; 	break;	// spider
		  	case 54:	cType = CreatureType.ZOMBIE; 	break;	// zombie
		  	case 55:	cType = CreatureType.SLIME; 	break;	// slime
		  	case 49:	cType = CreatureType.MONSTER; 	break;	// monster
		  	case 53:	cType = CreatureType.GIANT; 	break;	// giant
		  	case 95:	cType = CreatureType.WOLF; 		break;	// wolf
		  	case 59:	cType = CreatureType.CAVE_SPIDER; break;	// cave spider
		  	case 58:	cType = CreatureType.ENDERMAN; 	break;	// enderman
		  	case 60:	cType = CreatureType.SILVERFISH; break;	// silverfish
		  	case 61:	cType = CreatureType.BLAZE; 	break; 	// blaze
		  	case 63: 	cType = CreatureType.ENDER_DRAGON; break; 	// enderdragon
		  	case 62: 	cType = CreatureType.MAGMA_CUBE; break;	// magmacube
		  	case 96: 	cType = CreatureType.MUSHROOM_COW; break;  // mooshroom
		  	case 97: 	cType = CreatureType.SNOWMAN; 	break;	// snowgolem
		  	case 120:	cType = CreatureType.VILLAGER; break;  // villager
		  	default: 	cType = CreatureType.PIG; 		break;	// pig
		  }
		  return cType;
	  }
}
