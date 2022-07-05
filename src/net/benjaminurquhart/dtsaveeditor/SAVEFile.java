package net.benjaminurquhart.dtsaveeditor;

import java.util.List;

public class SAVEFile {
	
	  private static String[] itemNames = 
			  {
			    "Test Food",
			    "Test Knife",
			    "Test Chest",
			    "Pencil",
			    "Bandage",
			    "Butterscotch Pie",
			    "Big Pencil",
			    "Bandage",
			    "Snow Ring",
			    "Wristwatch",
			    "Monster Candy",
			    "Spider Donut",
			    "Spider Cider",
			    "Toy Knife",
			    "Faded Ribbon",
			    "Heavy Branch",
			    "Egg",
			    "Chocolate Candy",
			    "Quiet Shroom",
			    "Hard Hat",
			    "Clean Pan",
			    "Cracked Bat",
			    "Skip Sandwich",
			    "Hamburger",
			    "Punch Card",
			    "Stick",
			    "Kris's Knife",
			    "Real Knife",
			    "Snail Pie",
			    "Banana",
			    "Boiled Egg",
			    "Aluminum Bat",
			    "Tough Glove"
			  };

	public String name;
	public int exp;
	public List<Integer> items;
	public int[] weapon;
	public int[] armor;
	public boolean susieActive;
	public boolean noelleActive;
	public int playTime;
	public int zone;
	public int gold;
	public Object[] flags;
	public Object[] persFlags;
	public String zoneName;
	public int deaths;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DeltaTraveler SAVEFile:");
		sb.append("\n\tPlayer name: ");
		sb.append(name);
		sb.append("\n\tPlaytime: ");
		sb.append(playTime / 60);
		sb.append(':');
		sb.append(playTime % 60);
		sb.append("\n\tGold: ");
		sb.append(gold);
		sb.append("\n\tEXP: ");
		sb.append(exp);
		sb.append("\n\tDeaths: ");
		sb.append(deaths);
		sb.append("\n\tEquipped Weapons: [");
		for(int i = 0; i < weapon.length; i++) {
			sb.append(itemNames[weapon[i]]);
			sb.append(" (");
			sb.append(weapon[i]);
			sb.append(')');
			if(i < weapon.length - 1) sb.append(", ");
		}
		sb.append(']');
		sb.append("\n\tEquipped Armor:   [");
		for(int i = 0; i < armor.length; i++) {
			sb.append(itemNames[armor[i]]);
			sb.append(" (");
			sb.append(armor[i]);
			sb.append(')');
			if(i < armor.length - 1) sb.append(", ");
		}
		sb.append(']');
		sb.append("\n\tInventory:");
		for(int item : items) {
			sb.append("\n\t - ");
			sb.append(itemNames[item]);
			sb.append(" (");
			sb.append(item);
			sb.append(')');
		}
		return sb.toString();
	}
}
