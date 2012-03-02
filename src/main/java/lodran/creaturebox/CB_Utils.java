package lodran.creaturebox;

import org.bukkit.entity.EntityType;

public class CB_Utils {
	
	  public static EntityType getMobFromDurability(int theCreatureIndex) {
		 EntityType cType;
		  switch(theCreatureIndex) {
		  	case 90:	cType = EntityType.PIG ; 		break;	// pig
		  	case 93:	cType = EntityType.CHICKEN; 	break;	// chicken
		  	case 92:	cType = EntityType.COW; 		break;	// cow
		  	case 91:	cType = EntityType.SHEEP;		break;	// sheep
		  	case 94:	cType = EntityType.SQUID;		break;	// squid
		  	case 50:	cType = EntityType.CREEPER;	break;	// creeper
		  	case 56:	cType = EntityType.GHAST; 	break;	// ghast
		  	case 57:	cType = EntityType.PIG_ZOMBIE; break;	// pig zombie
		  	case 51:	cType = EntityType.SKELETON; 	break;	// skeleton
		  	case 52:	cType = EntityType.SPIDER; 	break;	// spider
		  	case 54:	cType = EntityType.ZOMBIE; 	break;	// zombie
		  	case 55:	cType = EntityType.SLIME; 	break;	// slime
		  	case 53:	cType = EntityType.GIANT; 	break;	// giant
		  	case 95:	cType = EntityType.WOLF; 		break;	// wolf
		  	case 59:	cType = EntityType.CAVE_SPIDER; break;	// cave spider
		  	case 58:	cType = EntityType.ENDERMAN; 	break;	// enderman
		  	case 60:	cType = EntityType.SILVERFISH; break;	// silverfish
		  	case 61:	cType = EntityType.BLAZE; 	break; 	// blaze
		  	case 63: 	cType = EntityType.ENDER_DRAGON; break; 	// enderdragon
		  	case 62: 	cType = EntityType.MAGMA_CUBE; break;	// magmacube
		  	case 96: 	cType = EntityType.MUSHROOM_COW; break;  // mooshroom
		  	case 97: 	cType = EntityType.SNOWMAN; 	break;	// snowgolem
		  	case 120:	cType = EntityType.VILLAGER; break;  // villager
		  	default: 	cType = EntityType.PIG; 		break;	// pig
		  }
		  return cType;
	  }
}
