package com.corpffa;

import net.runelite.client.config.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@ConfigGroup("corpFfa")
public interface CorpFfaConfig extends Config {
    @ConfigSection(
            name = "Colors",
            position = 0,
            closedByDefault = false,
            description = "Colors"
    )
    String colorSection = "Colors";

    @ConfigSection(
            name = "Player List",
            position = 1,
            closedByDefault = false,
            description = "Player List"
    )
    String playerList = "Player List";


    @ConfigItem(
            keyName = "rangerColor",
            name = "Ranger Color",
            description = "The color to show rangers in",
            section = colorSection
    )
    default Color rangerColor() {
        return Color.PINK;
    }

    @ConfigItem(
            keyName = "cheaterColor",
            name = "Cheater Color",
            description = "The color to show cheaters in",
            section = colorSection
    )
    default Color cheaterColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "goodColor",
            name = "Good Player Color",
            description = "The color to show good players in",
            section = colorSection
    )
    default Color goodColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "defaultColor",
            name = "Default Color",
            description = "The default color to use",
            section = colorSection
    )
    default Color defaultColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "gonePlayerColor",
            name = "Teled Player Color",
            description = "The color to use for players that have teleported/died/despawned",
            section = colorSection
    )
    default Color gonePlayerColor() {
        return Color.BLACK;
    }

    @ConfigItem(
            keyName = "playerCountColor",
            name = "Player Count Color",
            description = "The color to show the player count in",
            section = colorSection
    )
    default Color playerCountColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "alwaysOn",
            name = "Always on",
            description = "Should the plugin always be enabled? Better performance if off but may be required when using private instance."
    )
    default boolean alwaysOn() {
        return false;
    }

    @ConfigItem(
            keyName = "hideGoodPlayers",
            name = "Hide Good Players",
            description = "Should the plugin hide players that have 2 specced and have allowed gear?",
            section = playerList
    )
    default boolean hideGoodPlayers() {
        return false;
    }

    @ConfigItem(
            keyName = "hideRangers",
            name = "Hide Rangers",
            description = "Should rangers be shown in the player list?",
            section = playerList
    )
    default boolean hideRangers() {
        return false;
    }

    @ConfigItem(
            keyName = "hidePlayerCount",
            name = "Hide Player Count",
            description = "Should the player count be hidden?",
            section = playerList
    )
    default boolean hidePlayerCount() {
        return false;
    }

    @ConfigItem(
            keyName = "hideTeledPlayers",
            name = "Hide Teled Players",
            description = "Should teled/dead players be hidden in the player list?",
            section = playerList
    )
    default boolean hideTeledPlayers() {
        return false;
    }

    @ConfigItem(
            keyName = "groupRangers",
            name = "Group Rangers",
            description = "Should the rangers be shown together in the player list?",
            section = playerList
    )
    default boolean groupRangers() {
        return false;
    }

    @ConfigItem(
            keyName = "splitRangersInPlayerCount",
            name = "Split Rangers In Player Count",
            description = "Should the rangers count be shown separately in the player count e.g 20 (+2)?",
            section = playerList
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
            section = playerList
    )
    default int bannedItemCountToShow() {
        return 1;
    }

    @ConfigItem(
            keyName = "taggedPlayers",
            name = "Tagged Players",
            description = "A list of player names that should be tagged. Separate names with commas (,)"
    )
    default String taggedPlayers() { return ""; }


}
