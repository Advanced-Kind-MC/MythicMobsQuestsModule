package com.gmail.berndivader.mythicmobsquests;

import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.drops.DropManager;
import io.lumine.xikage.mythicmobs.util.types.RangedDouble;
import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class MythicItemDeliverObjective 
extends
CustomObjective 
implements
Listener {

	static Quests quests; 
	static DropManager dropmanager;
	static CitizensAPI citizens;
	
	static {
		quests=(Quests)Bukkit.getPluginManager().getPlugin("Quests");
		dropmanager=((MythicMobs)Bukkit.getPluginManager().getPlugin("MythicMobs")).getDropManager();
	}
	
	public MythicItemDeliverObjective() {
		setName("MythicItem NPC Deliver Objective");
		setAuthor("BerndiVader, idea Wahrheit");
		setEnableCount(false);
		this.addData("NPC IDs");
		this.addDescription("NPC IDs","NPC ID or ID list like 1,2,3,4");
		this.addData("Material");
		this.addDescription("Material","Item material type or ANY");
		this.addData("ItemMarker");
		this.addDescription("ItemMarker","ItemMarker defined by reward or NONE");
		this.addData("NameEnds");
		this.addDescription("NameEnds","Item name ends with or NONE");
		this.addData("Amount");
		this.addDescription("Amount","How many items. Can be ranged like 1to3");
		this.addData("HoldItem");
		this.addDescription("HoldItem","Player need to hold the item (true/false)");
		this.addData("TimeLimit");
		this.addDescription("TimeLimit","Limited time in seconds or -1 for unlimit");
	}
	
	@EventHandler
	public void onNPCInteract(NPCRightClickEvent e) {
		if (e.isCancelled()||e.getClicker().isConversing()) return;
		Player player=e.getClicker();
		Quester quester=quests.getQuester(player.getUniqueId());
		for (Quest quest:quester.currentQuests.keySet()) {
			Map<String,Object>map=getDatamap(player,this,quest);
			if (map==null) continue;
			if (npcID(map,e.getNPC().getId())>-1) {
				String[]materials=map.get("Material").toString().split(",");
				String[]itemMarker=map.get("ItemMarker").toString().split(",");
				String[]nameEnds=map.get("NameEnds").toString().split(",");
				RangedDouble rd=new RangedDouble(map.get("Amount").toString());
				boolean bl1=false;
				if (map.get("HoldItem").toString().toUpperCase().equals("TRUE")) {
					ItemStack is=player.getInventory().getItemInMainHand().clone();
					bl1=rd.equals(is.getAmount());
					bl1&=arrContains(materials,is.getType().toString());
				} else {
					ListIterator<ItemStack>lit=player.getInventory().iterator();
					while(lit.hasNext()) {
						ItemStack it=lit.next();
						if(it.getType()==Material.AIR) continue;
					}
				}
				if (bl1) {
					quester.finishObjective(quest,"customObj",null,null,null,null,null,null,null,null,null,this);
				}
			}
		}
	}
	
	static boolean arrContains(String[]arr1,String s1) {
		for(int i1=0;i1<arr1.length;i1++) {
			if(arr1[i1].equals(s1)) return true;
		}
		return false;
	}
	
	static int npcID(Map<String,Object>map,int id) {
		String[]arr1=map.get("NPC IDs").toString().split(",");
		for(int i1=0;i1<arr1.length;i1++) {
			if(arr1[i1].equals(Integer.toString(id))) return id;
		}
		return -1;
	}
	
}
