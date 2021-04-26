package com.corpffa;

import net.runelite.api.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    public int SpecCount;
    public List<Integer> BannedGear;
    public boolean IsRanger;
    public boolean HasLeft;
    public boolean IsTagged;
    public net.runelite.api.Player Player;
    public boolean HideFromList;
    public boolean HasBeenScreenshotted;

    public Integer Weapon;

    public PlayerState(Player player) {
        Player = player;
        SpecCount = 0;
        BannedGear = new ArrayList<>();
        IsRanger = false;
        HasLeft = false;
        IsTagged = false;
        HideFromList = false;
        Weapon = -1;
        HasBeenScreenshotted = false;
    }
}