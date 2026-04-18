package com.overlast.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.overlast:client")
public class Client {

    @Config.LangKey("config.overlast:client.barPos")
    public String barPositions = "middle right";

    @Config.LangKey("config.overlast:client.evolutionTextYOffset")
    @Config.RangeInt(min = -200, max = 200)
    public int evolutionTextYOffset = 13;
}
