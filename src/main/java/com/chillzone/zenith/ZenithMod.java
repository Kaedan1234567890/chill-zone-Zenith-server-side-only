package com.chillzone.zenith;

import com.chillzone.zenith.block.ZenithBlocks;
import com.chillzone.zenith.command.ZenithCommands;
import com.chillzone.zenith.drop.ZenithMobDrops;
import com.chillzone.zenith.item.ZenithItems;
import com.chillzone.zenith.item.ZenithCreativeTabExtras;
import com.chillzone.zenith.item.ZenithAbilityScheduler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZenithMod implements ModInitializer {
    public static final String MOD_ID = "chillzonezenith";
    public static final Logger LOGGER = LoggerFactory.getLogger("ChillZoneZenith");

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Chill Zone Zenith...");

        // Register gameplay first so the /zenith command and server systems
        // are initialized independently of the generated resource-pack step.
        ZenithItems.initialize();
        ZenithBlocks.initialize();
        ZenithCreativeTabExtras.initialize();
        ZenithAbilityScheduler.initialize();
        ZenithCommands.register();
        ZenithMobDrops.initialize();
        ZenithJoinMessage.initialize();

        LOGGER.info("Chill Zone Zenith loaded. /zenith commands registered.");
    }
}
