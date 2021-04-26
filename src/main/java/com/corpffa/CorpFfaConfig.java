package com.corpffa;

import net.runelite.client.config.*;

import java.awt.*;

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
            name = "Banned Gear",
            position = 1,
            closedByDefault = false,
            description = "Banned Gear"
    )
    String bannedGearSection = "Banned Gear";

    @ConfigSection(
            name = "Hiding",
            position = 2,
            closedByDefault = false,
            description = "Hiding"
    )
    String hidingSection = "Hiding";

    @ConfigSection(
            name = "Colors",
            position = 3,
            closedByDefault = false,
            description = "Colors"
    )
    String colorsSection = "Colors";

    @ConfigSection(
            name = "Screenshots",
            position = 4,
            closedByDefault = false,
            description = "Screenshots"
    )
    String screenshotsSection = "Screenshots";

    @ConfigItem(
            keyName = "rangerColor",
            name = "Ranger Color",
            description = "The color to show rangers in",
            section = colorsSection
    )
    default Color rangerColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "cheaterColor",
            name = "Cheater Color",
            description = "The color to show cheaters in",
            section = colorsSection
    )
    default Color cheaterColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "goodColor",
            name = "Good Player Color",
            description = "The color to show good players in",
            section = colorsSection
    )
    default Color goodColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "defaultColor",
            name = "Default Color",
            description = "The default color to use",
            section = colorsSection
    )
    default Color defaultColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "gonePlayerColor",
            name = "Teled Player Color",
            description = "The color to use for players that have teleported/died/despawned",
            section = colorsSection
    )
    default Color gonePlayerColor() {
        return Color.BLACK;
    }

    @ConfigItem(
            keyName = "playerCountColor",
            name = "Player Count Color",
            description = "The color to show the player count in",
            section = colorsSection
    )
    default Color playerCountColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "taggedPlayerColor",
            name = "Tagged Player Color",
            description = "The color to show tagged players in",
            section = colorsSection
    )
    default Color taggedPlayerColor() {
        return Color.CYAN;
    }

    @ConfigItem(
            keyName = "hideGoodPlayers",
            name = "Hide Good Players",
            description = "Should the plugin hide players that have 2 specced and have allowed gear?",
            section = hidingSection
    )
    default boolean hideGoodPlayers() {
        return false;
    }

    @ConfigItem(
            keyName = "hidePlayerCount",
            name = "Hide Player Count",
            description = "Should the player count be hidden?",
            section = hidingSection
    )
    default boolean hidePlayerCount() {
        return false;
    }

    @ConfigItem(
            keyName = "hideBanner",
            name = "Hide \"Corp FFA\" Banner",
            description = "Should the \"Corp FFA\" banner be hidden?",
            section = hidingSection
    )
    default boolean hideBanner() {
        return false;
    }

    @ConfigItem(
            keyName = "hideTeledPlayers",
            name = "Hide Teled Players",
            description = "Should teled/dead players be hidden in the player list?",
            section = hidingSection
    )
    default boolean hideTeledPlayers() {
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
            section = bannedGearSection
    )
    default int bannedItemCountToShow() {
        return 1;
    }

    @ConfigItem(
            keyName = "allowArclight",
            name = "Allow Arclight specs",
            description = "Allows Arclight to be used as a special attack weapon",
            section = bannedGearSection
    )
    default boolean allowArclight() {
        return false;
    }

    @ConfigItem(
            keyName = "taggedPlayers",
            name = "Tagged Players",
            description = "A list of player names that should be tagged. Separate names with commas (,)",
            section = generalSection
    )
    default String taggedPlayers() {
        return "";
    }

    @ConfigItem(
            keyName = "nonFriendChatLabel",
            name = "Non-friends chat label",
            description = "The label to give players who aren't in the FC.",
            section = generalSection
    )
    default String nonFriendChatLabel() {
        return "*";
    }

    @ConfigItem(
            keyName = "saveToClipboard",
            name = "Copy To Clipboard",
            description = "Should screenshots also be saved to the clipboard?",
            section = screenshotsSection
    )
    default boolean saveToClipboard() {
        return false;
    }

    @ConfigItem(
            keyName = "captureOnCrash",
            name = "Screenshot Crashers",
            description = "Should screenshots be taken of crashers?",
            section = screenshotsSection
    )
    default boolean captureOnCrash() {
        return false;
    }

    @ConfigItem(
            keyName = "nofifyOnCapture",
            name = "Notify On Screenshot",
            description = "Should a notification be given when a screenshot is taken?",
            section = screenshotsSection
    )
    default boolean nofifyOnCapture() {
        return false;
    }

}
