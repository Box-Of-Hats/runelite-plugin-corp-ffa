package com.corpffa;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.http.api.item.ItemClient;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

public class CorpFfaOverlay extends OverlayPanel {
    private CorpFfaPlugin plugin;
    private CorpFfaConfig config;
    private Client client;

    @Inject
    public CorpFfaOverlay(CorpFfaPlugin plugin, Client client, CorpFfaConfig config)
    {
        super(plugin);

        setPosition(OverlayPosition.DYNAMIC);
        setPosition(OverlayPosition.DETACHED);
        setPosition(OverlayPosition.TOP_LEFT);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics2D)
    {
        List<LayoutableRenderableEntity> renderableEntities = panelComponent.getChildren();
        renderableEntities.clear();

        List<Entry<String, CorpFfaPlugin.PlayerState>> playerStates = new ArrayList<>(plugin.PlayersInCave.entrySet());

        // Sort list alphabetically
        playerStates.sort((player1, player2) -> {
            String playerName1 = player1.getKey();
            String playerName2 = player2.getKey();
            return playerName1.compareTo(playerName2);
        });

        renderableEntities.add(TitleComponent.builder().text("Corp FFA").color(Color.WHITE).build());
        for (Entry<String, CorpFfaPlugin.PlayerState> entry : playerStates)
        {
            CorpFfaPlugin.PlayerState playerState = entry.getValue();

            boolean hasBannedGear = playerState.BannedGear.size() > 0;
            boolean hasSpecced = playerState.SpecCount >= 2;
            boolean allGood = !hasBannedGear && hasSpecced;

            Color baseColor = allGood ? Color.GREEN : Color.WHITE;

            String rightLabel = playerState.SpecCount + "";
            if (hasBannedGear){
                Item item = new Item(playerState.BannedGear.get(0), 1);
                ItemComposition itemComposition = client.getItemDefinition(item.getId());
                rightLabel = itemComposition.getName();
            }

            renderableEntities.add(
                    LineComponent.builder()
                            .leftColor(hasBannedGear ? Color.RED : baseColor).left(entry.getKey())
                            .rightColor(hasBannedGear ? Color.RED : baseColor).right( rightLabel)
                            .build()
            );
        }

        return super.render(graphics2D);
    }
}

