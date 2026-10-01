package net.nukebob.nbconfig.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class TestConfigScreen extends NukebobConfigScreen{
    private final Screen parent;

    private TestConfigScreen(Screen screen) {
        super();
        parent = screen;
    }

    @Override
    public ArrayList<LittleNukebob> setConfig() {
        return new ArrayList<>(){{
            add(new LittleNukebob(Component.literal("Show lives in nametag"), littleNukebob -> {
                //MainConfig.loadConfig().showLivesInNametag=!MainConfig.loadConfig().showLivesInNametag;
            }, littleNukebob -> {
                //littleNukebob.enabled = MainConfig.loadConfig().showLivesInNametag;
            }));
            add(new LittleNukebob(Component.literal("Limit distance for lives nametag display"), littleNukebob -> {
                //MainConfig.loadConfig().livesDistanceLimit=!MainConfig.loadConfig().livesDistanceLimit;
            }, littleNukebob -> {
                //littleNukebob.enabled = MainConfig.loadConfig().livesDistanceLimit;
            }));
        }};
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
        //MainConfig.saveConfig();
    }
}
