package com.corpffa;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
    private final CorpFfaPlugin plugin;
    private final CorpFfaConfig config;
    private final Client client;

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
        Rectangle overlayPosition = super.getBounds();

        List<Entry<String, PlayerState>> playerStates = new ArrayList<>(plugin.PlayersInCave.entrySet());

        if (playerStates.size() <= 1) {
            return super.render(graphics2D);
        }

        long numberOfSpeccedPlayers = playerStates.stream().filter(playerEntry -> playerEntry.getValue().SpecCount >= 2).count();
        boolean shouldHaveSpecced = numberOfSpeccedPlayers > 3;


        // Sort list alphabetically
        playerStates.sort((player1, player2) -> {
            String playerName1 = player1.getKey();
            String playerName2 = player2.getKey();

            return playerName1.compareToIgnoreCase(playerName2);
        });

        if (!config.hideBanner()) {
            renderableEntities.add(TitleComponent.builder().text("Corp FFA").color(config.defaultColor()).build());
        }

        drawPlayerList(graphics2D, renderableEntities, overlayPosition, playerStates, shouldHaveSpecced);


        if (!config.hidePlayerCount()) {
            drawPlayerCount(renderableEntities, shouldHaveSpecced);
        }

        return super.render(graphics2D);
    }


    /**
     * Draw the list of players with highlights
     *
     * @param graphics2D
     * @param renderableEntities
     * @param overlayPosition
     * @param playerStates
     * @param shouldHaveSpecced  Are players expected to have specced yet?
     */
    private void drawPlayerList(Graphics2D graphics2D, List<LayoutableRenderableEntity> renderableEntities, Rectangle overlayPosition, List<Entry<String, PlayerState>> playerStates, boolean shouldHaveSpecced) {
        for (Entry<String, PlayerState> entry : playerStates) {
            PlayerState playerState = entry.getValue();
            Player player = playerState.Player;
            String nonFriendsChatIndicator = config.nonFriendChatLabel();

            if (playerState.HideFromList) {
                continue;
            }

            boolean hasBannedGear = playerState.BannedGear.size() > 0;
            boolean hasSpecced = playerState.SpecCount >= 2;
            boolean allGood = !hasBannedGear && hasSpecced;
            boolean isNonSpeccer = !hasSpecced && shouldHaveSpecced;

            String rightLabel = playerState.SpecCount + "";
            Color rightColor = config.defaultColor();

            String leftLabel = player.getName() + (player.isClanMember() || player.isFriend() || player.isFriendsChatMember() ? "" : " " + nonFriendsChatIndicator);
            Color leftColor = config.defaultColor();

            Color highlightColor = null;
            String highlightText = null;

            boolean shouldRender = true;

            if (playerState.HasLeft) {
                Color goneColor = config.gonePlayerColor();
                leftColor = goneColor;
                rightColor = goneColor;

                if (config.hideTeledPlayers()) {
                    shouldRender = false;
                }
            } else if (hasBannedGear && config.bannedItemCountToShow() > 0) {
                List<String> itemNames = playerState.BannedGear
                        .stream()
                        .limit(config.bannedItemCountToShow())
                        .map(gearId -> client.getItemDefinition(gearId).getName())
                        .collect(Collectors.toList());
                rightLabel = playerState.SpecCount + " - " + String.join(", ", itemNames);

                highlightPlayer(graphics2D, player, rightLabel, config.cheaterColor(), overlayPosition.x, overlayPosition.y);

                Color cheaterColor = config.cheaterColor();
                leftColor = cheaterColor;
                rightColor = cheaterColor;

            } else if (isNonSpeccer) {
                Color cheaterColor = config.cheaterColor();
                leftColor = cheaterColor;
                rightColor = cheaterColor;

                highlightColor = cheaterColor;
                highlightText = playerState.SpecCount + " spec";
                if (playerState.Weapon != -1) {
                    String weaponName = client.getItemDefinition(playerState.Weapon).getName();
                    String acronym = Arrays.stream(weaponName.split(" ")).map(str -> str.substring(0, 1)).collect(Collectors.joining(""));

                    highlightText += "( " + acronym + ")";
                }

            } else if (allGood) {
                Color goodColor = config.goodColor();
                leftColor = goodColor;
                rightColor = goodColor;

                if (config.hideGoodPlayers()) {
                    shouldRender = false;
                }
            }

            if (playerState.IsTagged) {
                Color taggedPlayerColor = config.taggedPlayerColor();

                highlightColor = taggedPlayerColor;
                highlightText = leftLabel;

                leftColor = taggedPlayerColor;
                leftLabel += "*";
                shouldRender = true;
            }

            if (shouldRender) {
                renderableEntities.add(
                        LineComponent.builder()
                                .leftColor(leftColor).left(leftLabel)
                                .rightColor(rightColor).right(rightLabel)
                                .build()
                );
                if (highlightText != null && highlightColor != null && !playerState.HasLeft) {
                    highlightPlayer(graphics2D, player, highlightText, highlightColor, overlayPosition.x, overlayPosition.y);
                }
            }

        }
    }


    private void drawPlayerCount(List<LayoutableRenderableEntity> renderableEntities, boolean showCount) {

        List<PlayerState> playersInCave = plugin.PlayersInCave.values()
                .stream()
                .filter(o -> !o.HasLeft)
                .collect(Collectors.toList());
        int playerCount = playersInCave.size();

        String playerCountText = "-";

        if (showCount) {
            playerCountText = playerCount + "";
        }

        renderableEntities.add(
                LineComponent.builder()
                        .leftColor(config.playerCountColor()).left("Players")
                        .rightColor(config.playerCountColor()).right(playerCountText)
                        .build()
        );
    }

    /**
     * Highlight a player with text
     *
     * @param graphics    Graphics object
     * @param actor       The players to highlight
     * @param text        The text to show
     * @param color       The color of the txt
     * @param xTextOffSet The X offset of the text (usually the overlay X position)
     * @param yTextOffSet The Y offset of the text (usually the overlay Y position)
     */
    private void highlightPlayer(Graphics2D graphics, Actor actor, String text, Color color, int xTextOffSet, int yTextOffSet) {
        Point poly = actor.getCanvasTextLocation(graphics, text, 20);
        if (poly == null) {
            return;
        }

        Point offsetPoint = new Point(poly.getX() - xTextOffSet, poly.getY() - yTextOffSet);

        OverlayUtil.renderTextLocation(graphics, offsetPoint, text, color);

    }
}

