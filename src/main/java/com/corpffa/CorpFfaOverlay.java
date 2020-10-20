package com.corpffa;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CorpFfaOverlay extends OverlayPanel {
    private CorpFfaPlugin plugin;
    private CorpFfaConfig config;
    private Client client;

    @Inject
    public CorpFfaOverlay(CorpFfaPlugin plugin, Client client, CorpFfaConfig config) {
        super(plugin);

        setPosition(OverlayPosition.DYNAMIC);
        setPosition(OverlayPosition.DETACHED);
        setPosition(OverlayPosition.TOP_LEFT);
        setPreferredSize(new Dimension(100, 600));
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics2D) {
        List<LayoutableRenderableEntity> renderableEntities = panelComponent.getChildren();
        renderableEntities.clear();

        List<Entry<Player, CorpFfaPlugin.PlayerState>> playerStates = new ArrayList<>(plugin.PlayersInCave.entrySet());

        if (playerStates.size() == 0) {
            return super.render(graphics2D);
        }

        long numberOfSpeccedPlayers = playerStates.stream().filter(playerEntry -> playerEntry.getValue().SpecCount >= 2).count();
        boolean shouldHaveSpecced = numberOfSpeccedPlayers > 5;


        // Sort list alphabetically
        playerStates.sort((player1, player2) -> {
            String playerName1 = player1.getKey().getName();
            String playerName2 = player2.getKey().getName();
            return playerName1.compareToIgnoreCase(playerName2);
        });

        renderableEntities.add(TitleComponent.builder().text("Corp FFA").color(config.defaultColor()).build());
        for (Entry<Player, CorpFfaPlugin.PlayerState> entry : playerStates) {
            CorpFfaPlugin.PlayerState playerState = entry.getValue();
            Player player = entry.getKey();

            boolean hasBannedGear = playerState.BannedGear.size() > 0;
            boolean hasSpecced = playerState.SpecCount >= 2;
            boolean allGood = !hasBannedGear && hasSpecced;
            boolean isNonSpeccer = !hasSpecced && shouldHaveSpecced && !playerState.IsRanger;

            if (isNonSpeccer) {
                highlightPlayer(graphics2D, player, playerState.SpecCount + " spec", config.cheaterColor());
            }


            String rightLabel = playerState.SpecCount + "";
            Color playerColor = config.defaultColor();
            boolean shouldRender = true;

            if (hasBannedGear) {
                Item item = new Item(playerState.BannedGear.get(0), 1);
                ItemComposition itemComposition = client.getItemDefinition(item.getId());
                String itemName = itemComposition.getName();
                rightLabel = itemName;

                highlightPlayer(graphics2D, player, itemName, config.cheaterColor());

                playerColor = config.cheaterColor();

            } else if (playerState.IsRanger) {
                rightLabel = "Ranger";
                playerColor = config.rangerColor();
                if (config.hideRangers()) {
                    shouldRender = false;
                }
            } else if (allGood) {
                playerColor = config.goodColor();
                if (config.hideGoodPlayers()) {
                    shouldRender = false;
                }
            }

            if (shouldRender) {
                renderableEntities.add(
                        LineComponent.builder()
                                .leftColor(playerColor).left(player.getName())
                                .rightColor(playerColor).right(rightLabel)
                                .build()
                );
            }

        }

        if (!config.hidePlayerCount()) {
            renderableEntities.add(
                    LineComponent.builder()
                            .leftColor(config.playerCountColor()).left("Players")
                            .rightColor(config.playerCountColor()).right(plugin.PlayersInCave.size() + "")
                            .build()
            );
        }

        return super.render(graphics2D);
    }

    private void highlightPlayer(Graphics2D graphics, Actor actor, String text, Color color) {
        Point poly = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight());
        if (poly == null) {
            return;
        }

        Rectangle overlayPosition = super.getBounds();
        Point offsetPoint = new Point(poly.getX() - overlayPosition.x, poly.getY() - overlayPosition.y);

        OverlayUtil.renderTextLocation(graphics, offsetPoint, text, color);

    }
}

