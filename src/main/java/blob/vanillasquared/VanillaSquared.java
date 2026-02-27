package blob.vanillasquared;

import net.fabricmc.api.ModInitializer;
import blob.vanillasquared.util.combat.components.dualwield.DualWieldEvents;
import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import blob.vanillasquared.util.modules.components.RegisterComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaSquared implements ModInitializer {
    public static final String MOD_ID = "vsq";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    @Override
    public void onInitialize() {
        TestItem.initialize();
        RegisterComponents.initialize();
        DualWieldEvents.initialize();

        LOGGER.info("Blob");

        RegisterAttributes.initialize();
    }
}
