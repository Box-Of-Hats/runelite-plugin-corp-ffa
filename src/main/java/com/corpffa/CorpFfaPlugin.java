package com.corpffa;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.sun.tools.javac.jvm.Items;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Corp FFA"
)


public class CorpFfaPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private CorpFfaConfig config;

    public HashMap<Player, PlayerState> PlayersInCave;

    @Inject
    private CorpFfaOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    private List<Integer> bannedItems = new ArrayList<Integer>(Arrays.asList(
            // Body
            ItemID.BANDOS_CHESTPLATE,
            ItemID.OBSIDIAN_PLATEBODY,
            ItemID.FIGHTER_TORSO,
            ItemID.FIGHTER_TORSO_L,
            // Legs
            ItemID.BANDOS_TASSETS,
            ItemID.BANDOS_TASSETS_23646,
            ItemID.OBSIDIAN_PLATELEGS,
            // Melee
            ItemID.DRAGON_HALBERD,
            ItemID.CRYSTAL_HALBERD,
            ItemID.CRYSTAL_HALBERD_24125,
            ItemID.DRAGON_CLAWS,
            ItemID.DRAGON_CLAWS_20784,
            ItemID.DRAGON_HUNTER_LANCE,
            ItemID.ZAMORAKIAN_HASTA,
            // Ranged
            ItemID.TWISTED_BOW,
            ItemID.TOXIC_BLOWPIPE,
            ItemID.DRAGON_KNIFE,
            ItemID.DRAGON_KNIFE_22812,
            ItemID.DRAGON_KNIFE_22814,
            ItemID.DRAGON_KNIFEP,
            ItemID.DRAGON_KNIFEP_22808,
            ItemID.DRAGON_KNIFEP_22810
    ));

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
        PlayersInCave = new HashMap();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        PlayersInCave.clear();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            Player currentPlayer = client.getLocalPlayer();
            int location = currentPlayer.getWorldLocation().getRegionID();
            //Corp cave - 11844
            PlayersInCave.clear();
        }

    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (npc.getCombatLevel() != 785) {
            return;
        }
        PlayersInCave.clear();
    }


    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (!(e.getActor() instanceof Player))
            return;
        Player player = (Player) e.getActor();

        if (player.getAnimation() == -1) {
            //Idle
            return;
        }

        String playerName = player.getName();

        List<Integer> bannedItems = getBannedItems(player);
        List<Integer> bannedGear = bannedItems;
        boolean isSpeccing = IsSpeccing(player);

        if (PlayersInCave.containsKey(playerName)) {
            PlayerState playerState = PlayersInCave.get(playerName);
            if (bannedGear.size() > 0) {
                playerState.BannedGear = bannedGear;
            }
            if (isSpeccing) {
                playerState.SpecCount += 1;
            }
        } else {
            PlayersInCave.put(
                    player,
                    new PlayerState(isSpeccing ? 1 : 0, bannedGear)
            );
        }
    }

    private boolean IsSpeccing(Player player) {
        if (player == null) {
            return false;
        }

        switch (player.getAnimation()) {
            case 7642: // BGS
            case 7643: // BGS
            case 1378: // DWH
                return true;
        }
        return false;
    }

    private List<Integer> getBannedItems(Player player) {
        List<Integer> illegalItems = new ArrayList();

        if (player == null) {
            return illegalItems;
        }

        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null) {
            return illegalItems;
        }

        List<Integer> equippedItems = new ArrayList(Arrays.asList(
                playerComposition.getEquipmentId(KitType.TORSO),
                playerComposition.getEquipmentId(KitType.LEGS),
                playerComposition.getEquipmentId(KitType.WEAPON)
        ));

        for (Integer equippedItem : equippedItems) {
            if (bannedItems.contains(equippedItem)) {
                illegalItems.add(equippedItem);
            }
        }

        return illegalItems;
    }


    @Provides
    CorpFfaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CorpFfaConfig.class);
    }

    public class PlayerState {
        public int SpecCount;
        public List<Integer> BannedGear;

        public PlayerState(int specCount, List<Integer> bannedGear) {
            SpecCount = specCount;
            BannedGear = bannedGear;
        }
    }
}
