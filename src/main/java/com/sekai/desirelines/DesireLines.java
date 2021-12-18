package com.sekai.desirelines;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(DesireLines.MODID)
public class DesireLines
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "desirelines";

    public DesireLines() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(Events.class);
    }
}
