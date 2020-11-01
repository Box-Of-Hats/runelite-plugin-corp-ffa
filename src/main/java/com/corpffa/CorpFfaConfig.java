package com.corpffa;

import net.runelite.client.config.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@ConfigGroup("corpFfa")
public interface CorpFfaConfig extends Config {
    @ConfigSection(
            name = "General",
            position = 0,
            closedByDefault = false,
            description = "General"
    )
    String generalSection = "General";

    @ConfigSection(
            name = "Gear Check",
            position = 1,
            closedByDefault = false,
            description = "Gear Check"
    )
    String gearCheckSection = "Gear Check";

    @ConfigSection(
            name = "Player Count",
            position = 2,
            closedByDefault = false,
            description = "Player Count"
    )
    String playerCountSection = "Player Count";

    @ConfigSection(
            name = "Teled Players",
            position = 5,
            closedByDefault = false,
            description = "Teled Players"
    )
    String teledPlayersSection = "Teled Players";

    @ConfigSection(
            name = "Tagged Players",
            position = 6,
            closedByDefault = false,
            description = "Tagged Players"
    )
    String taggedPlayersSection = "Tagged Players";

    @ConfigSection(
            name = "Good Players",
            position = 3,
            closedByDefault = false,
            description = "Good Players"
    )
    String goodPlayersSection = "Good Players";

    @ConfigSection(
            name = "Rangers",
            position = 4,
            closedByDefault = false,
            description = "Rangers"
    )
    String rangersSection = "Rangers";

    @ConfigItem(
            keyName = "rangerColor",
            name = "Ranger Color",
            description = "The color to show rangers in",
            section = rangersSection
    )
    default Color rangerColor() {
        return Color.PINK;
    }

    @ConfigItem(
            keyName = "cheaterColor",
            name = "Cheater Color",
            description = "The color to show cheaters in",
            section = generalSection
    )
    default Color cheaterColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "goodColor",
            name = "Good Player Color",
            description = "The color to show good players in",
            section = goodPlayersSection
    )
    default Color goodColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "defaultColor",
            name = "Default Color",
            description = "The default color to use",
            section = generalSection
    )
    default Color defaultColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "gonePlayerColor",
            name = "Teled Player Color",
            description = "The color to use for players that have teleported/died/despawned",
            section = teledPlayersSection
    )
    default Color gonePlayerColor() {
        return Color.BLACK;
    }

    @ConfigItem(
            keyName = "playerCountColor",
            name = "Player Count Color",
            description = "The color to show the player count in",
            section = playerCountSection
    )
    default Color playerCountColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "taggedPlayerColor",
            name = "Tagged Player Color",
            description = "The color to show tagged players in",
            section = taggedPlayersSection
    )
    default Color taggedPlayerColor() {
        return Color.ORANGE;
    }

    @ConfigItem(
            keyName = "alwaysOn",
            name = "Always on",
            description = "Should the plugin always be enabled? Better performance if off but may be required when using private instance.",
            section = generalSection
    )
    default boolean alwaysOn() {
        return false;
    }

    @ConfigItem(
            keyName = "hideGoodPlayers",
            name = "Hide Good Players",
            description = "Should the plugin hide players that have 2 specced and have allowed gear?",
            section = goodPlayersSection
    )
    default boolean hideGoodPlayers() {
        return false;
    }

    @ConfigItem(
            keyName = "hideRangers",
            name = "Hide Rangers",
            description = "Should rangers be shown in the player list?",
            section = rangersSection
    )
    default boolean hideRangers() {
        return false;
    }

    @ConfigItem(
            keyName = "hidePlayerCount",
            name = "Hide Player Count",
            description = "Should the player count be hidden?",
            section = playerCountSection
    )
    default boolean hidePlayerCount() {
        return false;
    }

    @ConfigItem(
            keyName = "hideTeledPlayers",
            name = "Hide Teled Players",
            description = "Should teled/dead players be hidden in the player list?",
            section = teledPlayersSection
    )
    default boolean hideTeledPlayers() {
        return false;
    }

    @ConfigItem(
            keyName = "groupRangers",
            name = "Group Rangers",
            description = "Should the rangers be shown together in the player list?",
            section = rangersSection
    )
    default boolean groupRangers() {
        return false;
    }

    @ConfigItem(
            keyName = "splitRangersInPlayerCount",
            name = "Split Rangers In Player Count",
            description = "Should the rangers count be shown separately in the player count e.g 20 (+2)?",
            section = playerCountSection
    )
    default boolean splitRangersInPlayerCount() {
        return false;
    }

    @Range(
            min = 0,
            max = 9
    )
    @ConfigItem(
            keyName = "bannedItemCountToShow",
            name = "Max Shown Items",
            description = "How many banned items should be shown on a player?",
            section = gearCheckSection
    )
    default int bannedItemCountToShow() {
        return 1;
    }

    @ConfigItem(
            keyName = "taggedPlayers",
            name = "Tagged Players",
            description = "A list of player names that should be tagged. Separate names with commas (,)",
            section = taggedPlayersSection
    )
    default String taggedPlayers() {
        return "";
    }


    @ConfigItem(
            keyName = "gearCheckOnSpawn",
            name = "Check Gear On Spawn",
            description = "Should gear checks be made on player spawn? Default is only on attack",
            section = gearCheckSection
    )
    default boolean gearCheckOnSpawn() {
        return false;
    }
}
