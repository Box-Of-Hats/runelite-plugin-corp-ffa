package com.corpffa;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.kit.KitType;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "Corp FFA"
)


public class CorpFfaPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private CorpFfaConfig config;

    private HashMap<String, PlayerState> PlayersInCave;

    //@inject
    //private CorpFfaOverlay overlay;

    @Override
    protected void startUp() throws Exception {
        //log.info("Example started!");
    }

    @Override
    protected void shutDown() throws Exception {
        //log.info("Example stopped!");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            Player currentPlayer = client.getLocalPlayer();
            int location = currentPlayer.getWorldLocation().getRegionID();
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "region id: " + location, null);
//            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
            //Corp cave - 11844
            PlayersInCave.clear();
        }

    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (!(e.getActor() instanceof Player))
            return;
        Player player = (Player) e.getActor();
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "anim changed " + player.getAnimation(), null);

        String playerName = player.getName();

        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", playerName, null);

        List<Integer> bannedItems = getBannedItems(player);
        boolean hasUsedBannedItem = bannedItems.size() > 0;
        boolean isSpeccing = IsSpeccing(player);
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", playerName + " - " + isSpeccing + " | " + hasUsedBannedItem, null);


        if (PlayersInCave.containsKey(playerName)){
            PlayerState playerState =PlayersInCave.get(playerName);
            if (hasUsedBannedItem){
                playerState.HasUsedBannedGear = true;
            }
            if (isSpeccing){
                playerState.SpecCount += 1;
            }
        } else {
            PlayersInCave.put(
                    playerName,
                    new PlayerState(isSpeccing ? 1 : 0, hasUsedBannedItem)
            );
        }
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", playerName + " - " + isSpeccing + " | " + hasUsedBannedItem, null);
    }

    private boolean IsSpeccing(Player player){
        if (player == null){
            return false;
        }

        switch (player.getAnimation()){
            case 7642: // BGS
            case 7643: // BGS
            case 401: // DWH?
            case 1378: // DWH?
                return true;
        }
        return false;
    }

    private List<Integer> getBannedItems(Player player){
        List<Integer> illegalItems = new ArrayList();

        if (player == null){
            return illegalItems;
        }
        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null){
            return illegalItems;
        }

        int[] equipmentIds = playerComposition.getEquipmentIds();

        int[] bannedItems = new int[]{
            ItemID.BANDOS_CHESTPLATE,
                ItemID.BANDOS_TASSETS,
                ItemID.BANDOS_TASSETS_23646,
                ItemID.OBSIDIAN_PLATEBODY,
                ItemID.OBSIDIAN_PLATELEGS,
                ItemID.FIGHTER_TORSO,
                ItemID.FIGHTER_TORSO_L,
                ItemID.DRAGON_HALBERD
        };

        for (int equippedItem : equipmentIds) {
            if (Arrays.asList(bannedItems).contains(equippedItem)){
                illegalItems.add(equippedItem);
            }
        }

        return illegalItems;
    }


    @Provides
    CorpFfaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CorpFfaConfig.class);
    }

    private class PlayerState {
        public int SpecCount;
        public boolean HasUsedBannedGear;

        public PlayerState(int specCount, boolean hasUsedBannedGear){
            SpecCount = specCount;
            HasUsedBannedGear = hasUsedBannedGear;
        }
    }
}
