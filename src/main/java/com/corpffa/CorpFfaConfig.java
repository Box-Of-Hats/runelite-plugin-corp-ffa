package com.corpffa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("colors")
public interface CorpFfaConfig extends Config {
    @ConfigItem(
            keyName = "rangerColor",
            name = "Ranger Color",
            description = "The color to show rangers in"

    )
    default Color rangerColor() {
        return Color.PINK;
    }

    @ConfigItem(
            keyName = "cheaterColor",
            name = "Cheater Color",
            description = "The color to show cheaters in"

    )
    default Color cheaterColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "goodColor",
            name = "Good Player Color",
            description = "The color to show good players in"

    )
    default Color goodColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "defaultColor",
            name = "Default Color",
            description = "The default color to use"

    )
    default Color defaultColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            keyName = "alwaysOn",
            name = "Always on",
            description = "Should the plugin always be enabled? Better performance if off but may be required when using private instance."

    )
    default boolean alwaysOn() {
        return false;
    }
}
