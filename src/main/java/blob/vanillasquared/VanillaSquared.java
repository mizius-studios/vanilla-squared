package blob.vanillasquared;

import net.fabricmc.api.ModInitializer;
import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaSquared implements ModInitializer {
	public static final String MOD_ID = "vanilla-squared";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TestItem.initialize();

		LOGGER.info("Loaded 1 Blob Mods");

		RegisterAttributes.initialize();
	}
}
