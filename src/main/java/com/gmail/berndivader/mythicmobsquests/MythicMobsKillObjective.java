package com.gmail.berndivader.mythicmobsquests;

import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.module.ICustomObjective;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class MythicMobsKillObjective extends CustomObjective implements Listener {

    public MythicMobsKillObjective() {
        setName("Kill MythicMobs Objective");
        setAuthor("BerndiVader");
        addStringPrompt("Objective Name", "Name your objective (Optional)", "");
        addStringPrompt("Conditions", "Enter a mythicmobs conditions for npc (Optional)", "NONE");
        addStringPrompt("TargetConditions", "Enter a mythicmobs conditions for player (Optional)", "NONE");
        addStringPrompt("Internal Mobnames", "List of MythicMobs Types to use. Split with <,> or use ANY for any MythicMobs mobs. (Optional)", "ANY");
        addStringPrompt("Mob Level", "Level to match. 0 for every level, any singlevalue, or rangedvalue. Example: 2-5 (Optional)", "0");
        addStringPrompt("Mob Faction", "Faction of the mob to match. Split with <,> (Optional)", "ANY");
        addStringPrompt("Notifier enabled", "true/false(default) send counter msg in chat.", false);
        addStringPrompt("Notifier msg", "Notifier message. %c% = placeholder for counter %s% placeholder for amount. (Optional)", "");
        setShowCount(true);
        setCountPrompt("How many MythicMobs to kill");
        setDisplay("%Objective Name%");
    }

    public int getCounter() {
        return this.getCount();
    }

    @EventHandler
    public void onMythicMobDeathEvent(EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        final Player p = e.getEntity().getKiller();
        final Quester qp = Utils.quests.get().getQuester(p.getUniqueId());
        if (qp.getCurrentQuests().isEmpty()) return;
        Optional<MobManager> mobManager = Utils.getMobManager();
        if (mobManager.isEmpty()) return;
        String activeMobFaction = "";
        final Entity bukkitEntity = e.getEntity();
        final ActiveMob activeMob = mobManager.get().getMythicMobInstance(bukkitEntity);
        if (activeMob == null) return;
        final String mobType = activeMob.getType().getInternalName();
        final int mobLevel = NMSUtils.getActiveMobLevel(activeMob);
        if (activeMob.hasFaction()) activeMobFaction = activeMob.getFaction();
        if (mobType == null || mobType.isEmpty()) return;
        for (Quest q : qp.getCurrentQuests().keySet()) {
            Map<String, Object> m = getDataForPlayer(p, this, q);
            if (m == null) continue;
            final String[] kt = m.getOrDefault("Internal Mobnames", "ANY").toString().split(",");
            final String[] parseLvl = m.getOrDefault("Mob Level", "0").toString().split("-");
            final String[] faction = m.getOrDefault("Mob Faction", "ANY").toString().split(",");
            final boolean notifier = Boolean.parseBoolean(m.getOrDefault("Notifier enabled", "FALSE").toString());
            final String notifierMsg = m.getOrDefault("Notifier msg", "Killed %c% of %s%").toString();
            String cC = m.getOrDefault("Conditions", "NONE").toString().toUpperCase();
            String tC = m.getOrDefault("TargetConditions", "NONE").toString().toUpperCase();
            if (cC.equals("NONE")) cC = null;
            if (tC.equals("NONE")) tC = null;
            final boolean useConditions = cC != null || tC != null;
            final MythicCondition mc = useConditions ? new MythicCondition(bukkitEntity, p, cC, tC) : null;
            int level = 0;
            int lmin = 0;
            int lmax = 0;
            if (parseLvl.length == 1) {
                level = 1;
                lmin = Integer.parseInt(parseLvl[0]);
                if (lmin == 0) level = 0;
            } else if (parseLvl.length == 2) {
                level = 2;
                lmin = Integer.parseInt(parseLvl[0]);
                lmax = Integer.parseInt(parseLvl[1]);
                if (lmin > lmax) level = 0;
            }
            if ((level == 0) || (level == 1 && mobLevel == lmin) || (level == 2 && (lmin <= mobLevel && mobLevel <= lmax))) {
                if (kt[0].equalsIgnoreCase("ANY") || ArrayUtils.contains(kt, mobType)) {
                    if (faction[0].equalsIgnoreCase("ANY") || ArrayUtils.contains(faction, activeMobFaction)) {
                        if (useConditions) {
                            Bukkit.getScheduler().runTaskLater(Utils.quests.get(), () -> {
                                if (mc.check()) {
                                    if (notifier) MythicMobsKillObjective.this.notifyQuester(qp, q, p, notifierMsg);
                                    MythicMobsKillObjective.this.incrementObjective(p, MythicMobsKillObjective.this, 1, q);
                                }
                            }, 1);
                        } else {
                            if (notifier) this.notifyQuester(qp, q, p, notifierMsg);
                            incrementObjective(p, this, 1, q);
                        }
                    }
                }
            }
        }
    }

    private void notifyQuester(Quester qp, Quest q, Player p, String msg) {
        int index = -1;
        Iterator<ICustomObjective> iterator = qp.getCurrentStage(q).getCustomObjectives().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (iterator.next() instanceof MythicMobsKillObjective) {
                index = i;
            }
            ++i;
        }
        if (index > -1) {
            int total = qp.getCurrentStage(q).getCustomObjectiveCounts().get(index);
            int count = 1 + qp.getQuestData(q).getCustomObjectiveCounts().get(index);
            msg = msg.replaceAll("%c%", Integer.toString(count));
            msg = msg.replaceAll("%s%", Integer.toString(total));
            p.sendMessage(msg);
        }
    }
}