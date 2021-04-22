package com.corpffa;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUploadStyle;

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

    @Inject
    private ImageCapture imageCapture;


    @Inject
    private ScheduledExecutorService executor;

    @Inject
    DrawManager drawManager;

    @Provides
    CorpFfaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CorpFfaConfig.class);
    }


    public HashMap<String, PlayerState> PlayersInCave;

    /**
     * Is the player currently in the corp beast cave?
     */
    private boolean IsActive;

    private List<String> TaggedPlayers;

    private final Set<Integer> BannedItems = ImmutableSet.of(
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
    );

    private final Set<Integer> RangedWeapons = ImmutableSet.of(
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
    );

    private final Set<Integer> GoodSpecWeapons = ImmutableSet.of(
            ItemID.DRAGON_WARHAMMER,
            ItemID.DRAGON_WARHAMMER_20785,
            ItemID.BANDOS_GODSWORD,
            ItemID.BANDOS_GODSWORD_20782,
            ItemID.BANDOS_GODSWORD_21060,
            ItemID.BANDOS_GODSWORD_OR
    );

    private final Set<Integer> IgnoredAnimations = ImmutableSet.of(
            AnimationID.IDLE,
            AnimationID.CONSUMING,
            AnimationID.DEATH
    );

    private final Pattern receivedADropPattern = Pattern.compile("<col=[\\d\\w]+>(\\w+) received a drop: .+</col>");


    @Override
    protected void startUp() throws Exception {
        PlayersInCave = new HashMap<>();
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
            IsActive = location == 11844;

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
        if (!IsActive) {
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

        if (!hadBannedGear && !isTagged) {
            playerState.HideFromList = true;
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
    public void onChatMessage(ChatMessage chatMessage) {
        if (!IsActive) {
            return;
        }

        boolean isGameMessage = chatMessage.getType() == ChatMessageType.GAMEMESSAGE;
        if (!isGameMessage) {
            return;
        }

        Matcher matcher = receivedADropPattern.matcher(chatMessage.getMessage());
        boolean isLootMessage = matcher.find();
        if (!isLootMessage) {
            return;
        }

        String userName = matcher.group(1);
        if (!PlayersInCave.containsKey(userName)) {
            return;
        }

        PlayerState playerState = PlayersInCave.get(userName);
        boolean playerHasSpecced = playerState.SpecCount >= 2;
        if (playerHasSpecced) {
            return;
        }
        String message = "<col=FF0000>" + userName + " got the kill with " + playerState.SpecCount + " specs!</col>";

        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);

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

        Integer equippedWeapon = playerComposition.getEquipmentId(KitType.WEAPON);
        boolean isHoldingGoodSpecWeapon = GoodSpecWeapons.contains(equippedWeapon);
        if (!isHoldingGoodSpecWeapon) {
            playerState.Weapon = equippedWeapon;
        } else {
            playerState.Weapon = -1;
        }

        DoTaggedCheck(playerState, playerName);

        boolean hasBannedGear = DoBannedGearCheck(playerState, playerComposition);


        if (hasBannedGear && !playerState.HasBeenScreenshotted && config.captureOnCrash()) {
            playerState.HasBeenScreenshotted = true;
            takeScreenshot("crash--" + playerName + "--");
        }

        if (hasBannedGear) return;

        if (DoRangerCheck(playerState, playerComposition)) return;

        DoSpecCheck(playerState, player);
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
        if (playerName == null) {
            playerName = "";
        }
        boolean isTaggedPlayer = TaggedPlayers.contains(playerName.toLowerCase());
        playerState.IsTagged = isTaggedPlayer;
        return isTaggedPlayer;
    }

    private PlayerState GetOrAddPlayerState(Player player, String playerName) {
        return PlayersInCave.computeIfAbsent(playerName, k -> new PlayerState(player));
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
        List<Integer> illegalItems = new ArrayList<>();

        if (playerComposition == null) {
            return illegalItems;
        }

        List<Integer> equippedItems = Arrays.asList(
                playerComposition.getEquipmentId(KitType.TORSO),
                playerComposition.getEquipmentId(KitType.LEGS),
                playerComposition.getEquipmentId(KitType.WEAPON)
        );

        for (Integer equippedItem : equippedItems) {
            if (BannedItems.contains(equippedItem)) {
                illegalItems.add(equippedItem);
            }
        }

        return illegalItems;
    }


    public void RefreshTaggedPlayers() {
        TaggedPlayers = Arrays.stream(config.taggedPlayers().split(","))
                .map(a -> a.trim().toLowerCase())
                .collect(Collectors.toList());
    }

    private void takeScreenshot(String fileName) {
        boolean shouldNotify = config.nofifyOnCapture();
        boolean shouldCopyToClipboard = config.saveToClipboard();

        Consumer<Image> imageCallback = (img) ->
                executor.submit(() -> imageCapture.takeScreenshot((BufferedImage) img, fileName, "corp-ffa", shouldNotify, shouldCopyToClipboard ? ImageUploadStyle.CLIPBOARD : ImageUploadStyle.NEITHER));

        drawManager.requestNextFrameListener(imageCallback);
    }
}
