package com.overlast.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.overlast:broadcast")
public class Broadcast {

    private static final String COLOR_COMMENT =
            "Allowed colors: black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.headerFrameColor")
    public String headerFrameColor = "gold";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.headerTitleColor")
    public String headerTitleColor = "gold";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.linePrefixColor")
    public String linePrefixColor = "dark_gray";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.introColor")
    public String introColor = "blue";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.weatherColor")
    public String weatherColor = "gray";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.mainColor")
    public String mainColor = "yellow";

    @Config.Comment(COLOR_COMMENT)
    @Config.LangKey("config.overlast:broadcast.outroColor")
    public String outroColor = "yellow";
}
