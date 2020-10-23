package com.corpffa;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
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
import java.util.stream.Stream;

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

    private boolean isActive;

    private List<Integer> BannedItems = new ArrayList<Integer>(Arrays.asList(
            // Melee
            ItemID.DRAGON_HALBERD,
            ItemID.CRYSTAL_HALBERD,
            ItemID.CRYSTAL_HALBERD_24125,
            ItemID.DRAGON_CLAWS,
            ItemID.DRAGON_CLAWS_20784,
            ItemID.DRAGON_HUNTER_LANCE,
            ItemID.ZAMORAKIAN_HASTA,
            // Body
            ItemID.BANDOS_CHESTPLATE,
            ItemID.OBSIDIAN_PLATEBODY,
            ItemID.FIGHTER_TORSO,
            ItemID.FIGHTER_TORSO_L,
            ItemID.INQUISITORS_HAUBERK,
            // Legs
            ItemID.BANDOS_TASSETS,
            ItemID.BANDOS_TASSETS_23646,
            ItemID.OBSIDIAN_PLATELEGS,
            ItemID.INQUISITORS_PLATESKIRT,
            ItemID.FREMENNIK_KILT,
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

    private List<Integer> RangedWeapons = new ArrayList<>(Arrays.asList(
            ItemID.RUNE_CROSSBOW,
            ItemID.RUNE_CROSSBOW_23601,
            ItemID.DRAGON_CROSSBOW,
            ItemID.DRAGON_HUNTER_CROSSBOW,
            ItemID.ARMADYL_CROSSBOW,
            ItemID.ARMADYL_CROSSBOW_23611,
            ItemID.DARK_BOW,
            ItemID.DARK_BOW_12765,
            ItemID.DARK_BOW_12766,
            ItemID.DARK_BOW_12767,
            ItemID.DARK_BOW_12768,
            ItemID.DARK_BOW_20408
    ));

    private List<Integer> IgnoredAnimations = new ArrayList<>(Arrays.asList(
            AnimationID.IDLE,
            AnimationID.CONSUMING,
            AnimationID.DEATH
    ));

    @Override
    protected void startUp() throws Exception {
        PlayersInCave = new HashMap();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        PlayersInCave.clear();
        isActive = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            Player currentPlayer = client.getLocalPlayer();
            int location = currentPlayer.getWorldLocation().getRegionID();
            PlayersInCave.clear();

            //Corp cave - 11844
            isActive = location == 11844 || config.alwaysOn();

            if (isActive) {
                overlayManager.add(overlay);
            }
        }

    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned playerDespawned)
    {
        Player player = playerDespawned.getPlayer();
        if (PlayersInCave.containsKey(player)){
            PlayerState playerState =  PlayersInCave.get(player);
            playerState.HasLeft = true;
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (npc.getCombatLevel() != 785) {
            return;
        }
        isActive = true;
        PlayersInCave.clear();
    }


    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (!isActive) {
            return;
        }
        if (!(e.getActor() instanceof Player))
            return;
        Player player = (Player) e.getActor();

        int animationId = player.getAnimation();
        if (IgnoredAnimations.contains(animationId)) {
            return;
        }

        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null) {
            return;
        }


        List<Integer> bannedGear = new ArrayList<>();
        if (config.bannedItemCountToShow() > 0) {
            bannedGear = getBannedItems(playerComposition);
        }

        boolean isSpeccing = IsSpeccing(player);
        boolean isRanger = IsRanger(playerComposition);

        if (PlayersInCave.containsKey(player)) {
            PlayerState playerState = PlayersInCave.get(player);
            playerState.HasLeft = false;
            if (bannedGear.size() > 0) {
                playerState.BannedGear = Stream.concat(playerState.BannedGear.stream(), bannedGear.stream())
                        .distinct()
                        .collect(Collectors.toList());
            } else if (isRanger) {
                playerState.IsRanger = true;
            } else if (isSpeccing) {
                playerState.SpecCount += 1;
            }

        } else {
            PlayersInCave.put(
                    player,
                    new PlayerState(isSpeccing ? 1 : 0, bannedGear, isRanger)
            );
        }
    }

    private boolean IsRanger(PlayerComposition playerComposition) {
        return RangedWeapons.contains(playerComposition.getEquipmentId(KitType.WEAPON));
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

    private List<Integer> getBannedItems(PlayerComposition playerComposition) {
        List<Integer> illegalItems = new ArrayList();

        if (playerComposition == null) {
            return illegalItems;
        }

        if (playerComposition == null) {
            return illegalItems;
        }

        List<Integer> equippedItems = new ArrayList(Arrays.asList(
                playerComposition.getEquipmentId(KitType.TORSO),
                playerComposition.getEquipmentId(KitType.LEGS),
                playerComposition.getEquipmentId(KitType.WEAPON)
        ));

        for (Integer equippedItem : equippedItems) {
            if (BannedItems.contains(equippedItem)) {
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
        public boolean IsRanger;
        public boolean HasLeft;

        public PlayerState(int specCount, List<Integer> bannedGear, boolean isRanger) {
            SpecCount = specCount;
            BannedGear = bannedGear;
            IsRanger = isRanger;
            HasLeft = false;
        }
    }
}
