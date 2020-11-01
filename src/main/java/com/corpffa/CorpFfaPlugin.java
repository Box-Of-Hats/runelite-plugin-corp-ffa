package com.corpffa;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.FriendsChatMemberJoined;
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

    @Inject
    private CorpFfaOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Provides
    CorpFfaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CorpFfaConfig.class);
    }


    public HashMap<String, PlayerState> PlayersInCave;

    private boolean IsActive;

    private List<String> TaggedPlayers;

    private final List<Integer> BannedItems = new ArrayList<Integer>(Arrays.asList(
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

    private final List<Integer> RangedWeapons = new ArrayList<>(Arrays.asList(
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

    private final List<Integer> IgnoredAnimations = new ArrayList<>(Arrays.asList(
            AnimationID.IDLE,
            AnimationID.CONSUMING,
            AnimationID.DEATH
    ));

    @Override
    protected void startUp() throws Exception {
        PlayersInCave = new HashMap();
        RefreshTaggedPlayers();
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        PlayersInCave.clear();
        IsActive = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            Player currentPlayer = client.getLocalPlayer();
            int location = currentPlayer.getWorldLocation().getRegionID();
            PlayersInCave.clear();

            //Corp cave - 11844
            IsActive = location == 11844 || config.alwaysOn();

            if (IsActive) {
                overlayManager.add(overlay);
                RefreshTaggedPlayers();
            }
        }

    }

    @Subscribe
    public void onPlayerDespawned(PlayerDespawned playerDespawned) {
        String playerName = playerDespawned.getPlayer().getName();
        if (PlayersInCave.containsKey(playerName)) {
            PlayerState playerState = PlayersInCave.get(playerName);
            playerState.HasLeft = true;
        }
    }

    @Subscribe
    public void onPlayerSpawned(PlayerSpawned playerSpawned) {
        if (!IsActive || !config.gearCheckOnSpawn()) {
            return;
        }
        Player player = playerSpawned.getPlayer();

        PlayerComposition playerComposition = player.getPlayerComposition();
        if (playerComposition == null) {
            return;
        }

        String playerName = player.getName();
        PlayerState playerState = GetOrAddPlayerState(player, playerName);


        playerState.HasLeft = false;

        boolean isTagged = DoTaggedCheck(playerState, playerName);

        boolean hadBannedGear = DoBannedGearCheck(playerState, playerComposition);
        if (!hadBannedGear && !isTagged){
            playerState.HideFromList = true;
        }
    }

    @Subscribe
    public void onFriendsChatMemberJoined(FriendsChatMemberJoined friendsChatMemberJoined){
        RefreshTaggedPlayers();
        String playerName = friendsChatMemberJoined.getMember().getName().toLowerCase();
        boolean isPlayerTagged = TaggedPlayers.contains(playerName);
        if (isPlayerTagged){
            client.addChatMessage(ChatMessageType.FRIENDSCHAT, "Corp FFA", "Tagged player joined chat: " + playerName, null);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (npc.getCombatLevel() != 785) {
            return;
        }
        IsActive = true;
        PlayersInCave.clear();
        RefreshTaggedPlayers();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if (!IsActive) {
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

        String playerName = player.getName();
        PlayerState playerState = GetOrAddPlayerState(player, playerName);

        playerState.HideFromList = false;
        playerState.HasLeft = false;

        DoTaggedCheck(playerState, playerName);

        if (DoBannedGearCheck(playerState, playerComposition)) return;

        if (DoRangerCheck(playerState, playerComposition)) return;

        if (DoSpecCheck(playerState, player)) return;
    }

    private boolean DoBannedGearCheck(PlayerState playerState, PlayerComposition playerComposition) {
        List<Integer> bannedGear = new ArrayList<>();
        if (config.bannedItemCountToShow() > 0) {
            bannedGear = GetBannedItems(playerComposition);
        }
        boolean hasBannedGear = bannedGear.size() > 0;
        if (hasBannedGear) {
            playerState.BannedGear = Stream.concat(playerState.BannedGear.stream(), bannedGear.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return hasBannedGear;
    }

    private boolean DoRangerCheck(PlayerState playerState, PlayerComposition playerComposition) {
        boolean isRanger = IsRanger(playerComposition);
        playerState.IsRanger = isRanger;
        return isRanger;
    }

    private boolean DoSpecCheck(PlayerState playerState, Player player) {
        boolean isSpeccing = IsSpeccing(player);
        if (isSpeccing) {
            playerState.SpecCount += 1;
        }
        return isSpeccing;
    }

    private boolean DoTaggedCheck(PlayerState playerState, String playerName) {
        boolean isTaggedPlayer = TaggedPlayers.contains(playerName.toLowerCase());
        playerState.IsTagged = isTaggedPlayer;
        return isTaggedPlayer;
    }

    private PlayerState GetOrAddPlayerState(Player player, String playerName) {
        if (!PlayersInCave.containsKey(playerName)) {
            PlayersInCave.put(
                    playerName,
                    new PlayerState(player)
            );
        }

        return PlayersInCave.get(playerName);
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

    private List<Integer> GetBannedItems(PlayerComposition playerComposition) {
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


    public void RefreshTaggedPlayers() {
        TaggedPlayers = Arrays.asList(config.taggedPlayers().split(","))
                .stream()
                .map(a -> a.trim().toLowerCase())
                .collect(Collectors.toList());
    }

    public class PlayerState {
        public int SpecCount;
        public List<Integer> BannedGear;
        public boolean IsRanger;
        public boolean HasLeft;
        public boolean IsTagged;
        public Player Player;
        public boolean HideFromList;

        public PlayerState(Player player, int specCount, List<Integer> bannedGear, boolean isRanger, boolean isTagged) {
            Player = player;
            SpecCount = specCount;
            BannedGear = bannedGear;
            IsRanger = isRanger;
            HasLeft = false;
            IsTagged = isTagged;
            HideFromList = false;
        }

        public PlayerState(Player player) {
            Player = player;
            SpecCount = 0;
            BannedGear = new ArrayList<>();
            IsRanger = false;
            HasLeft = false;
            IsTagged = false;
            HideFromList = false;
        }
    }
}
