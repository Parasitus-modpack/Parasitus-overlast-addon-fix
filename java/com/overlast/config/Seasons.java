package com.overlast.config;

import net.minecraftforge.common.config.Config;

@Config.LangKey("config.overlast:seasons")
public class Seasons {

    @Config.LangKey("config.overlast:seasons.enableSeasons")
    public boolean enableSeasons = true;

    @Config.LangKey("config.overlast:seasons.winterLength")
    @Config.RangeInt(min = 0, max = 365)
    public int winterLength = 10;

    @Config.LangKey("config.overlast:seasons.springLength")
    @Config.RangeInt(min = 0, max = 365)
    public int springLength = 10;

    @Config.LangKey("config.overlast:seasons.summerLength")
    @Config.RangeInt(min = 0, max = 365)
    public int summerLength = 10;

    @Config.LangKey("config.overlast:seasons.autumnLength")
    @Config.RangeInt(min = 0, max = 365)
    public int autumnLength = 10;

    @Config.LangKey("config.overlast:seasons.enableSummerParasiteEffect")
    public boolean enableSummerParasiteEffect = false;

    @Config.LangKey("config.overlast:seasons.enableSummerPlayerEffect")
    public boolean enableSummerPlayerEffect = false;

    @Config.LangKey("config.overlast:seasons.enableWinterParasiteEffect")
    public boolean enableWinterParasiteEffect = false;

    @Config.LangKey("config.overlast:seasons.enableWinterPlayerEffect")
    public boolean enableWinterPlayerEffect = false;
}
