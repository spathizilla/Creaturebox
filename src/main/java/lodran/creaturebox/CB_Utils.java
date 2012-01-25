package lodran.creaturebox;

public class CB_Utils {
	
	  public static int getMobEnchantmentFromID(int theCreatureIndex) {
		  int theNCreatureIndex;
		  switch(theCreatureIndex) {
		  	case 90:	theNCreatureIndex = 0 ; break;	// pig
		  	case 93:	theNCreatureIndex = 1; break;	// chicken
		  	case 92:	theNCreatureIndex = 2; break;	// cow
		  	case 91:	theNCreatureIndex = 3; break;	// sheep
		  	case 94:	theNCreatureIndex = 4; break;	// squid
		  	case 50:	theNCreatureIndex = 5; break;	// creeper
		  	case 56:	theNCreatureIndex = 6; break;	// ghast
		  	case 57:	theNCreatureIndex = 7; break;	// pig zombie
		  	case 51:	theNCreatureIndex = 8; break;	// skeleton
		  	case 52:	theNCreatureIndex = 9; break;	// spider
		  	case 54:	theNCreatureIndex = 10; break;	// zombie
		  	case 55:	theNCreatureIndex = 11; break;	// slime
		  	case 49:	theNCreatureIndex = 12; break;	// monster
		  	case 53:	theNCreatureIndex = 13; break;	// giant
		  	case 95:	theNCreatureIndex = 14; break;	// wolf
		  	case 59:	theNCreatureIndex = 15; break;	// cave spider
		  	case 58:	theNCreatureIndex = 16; break;	// enderman
		  	case 60:	theNCreatureIndex = 17; break;	// silverfish
		  	case 61:	theNCreatureIndex = 18; break; 	// blaze
		  	case 63: 	theNCreatureIndex = 19; break; 	// enderdragon
		  	case 62: 	theNCreatureIndex = 20; break;	// magmacube
		  	case 96: 	theNCreatureIndex = 21; break;  // mooshroom
		  	case 97: 	theNCreatureIndex = 22; break;	// snowgolem
		  	case 120:	theNCreatureIndex = 23; break;  // villager
		  	default: 	theNCreatureIndex = 0; break;	// pig
		  }
		  return theNCreatureIndex;
	  }

	  public static int getMobNetworkFromEnchant(int theCreatureIndex) {
		  int theNCreatureIndex;
		  switch(theCreatureIndex) {
			case 0:		theNCreatureIndex = 90 ; break;	// pig
			case 1:		theNCreatureIndex = 93; break;	// chicken
			case 2:		theNCreatureIndex = 92; break;	// cow
			case 3:		theNCreatureIndex = 91; break;	// sheep
			case 4:		theNCreatureIndex = 94; break;	// squid
			case 5:		theNCreatureIndex = 50; break;	// creeper
			case 6:		theNCreatureIndex = 56; break;	// ghast
			case 7:		theNCreatureIndex = 57; break;	// pig_zombie
			case 8:		theNCreatureIndex = 51; break;	// skele
			case 9:		theNCreatureIndex = 52; break;	// spider
			case 10:	theNCreatureIndex = 54; break;	// zombie
			case 11:	theNCreatureIndex = 55; break;	// slime
			case 12:	theNCreatureIndex = 49; break;	// monster
			case 13:	theNCreatureIndex = 53; break;	// giant
			case 14:	theNCreatureIndex = 95; break;	// wolf
			case 15:	theNCreatureIndex = 59; break;	// cavespider
			case 16:	theNCreatureIndex = 58; break;	// enderman
			case 17:	theNCreatureIndex = 60; break;	// silverfish
			case 18:	theNCreatureIndex = 61; break; 	// blaze
		  	case 19: 	theNCreatureIndex = 63; break; 	// enderdragon
		  	case 20: 	theNCreatureIndex = 62; break;	// magmacube
		  	case 21: 	theNCreatureIndex = 96; break;  // mooshroom
		  	case 22: 	theNCreatureIndex = 97; break;	// snowgolem
		  	case 23:	theNCreatureIndex = 120; break; // villager
		  	default: 	theNCreatureIndex = 90; break;	// pig
		  }
		  return theNCreatureIndex;
	  }
}
