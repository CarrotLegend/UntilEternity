package com.carrot123.until_eternity.compat.revelationfix;

import com.carrot123.until_eternity.until_eternity;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.InvocationTargetException;

/** Keeps RevelationFix's optional API out of the main mod class loader when it is absent. */
public final class RevelationFixAbsoluteDamageBootstrap {
    private static final String[] HANDLERS = {
            "com.carrot123.until_eternity.compat.revelationfix.TrueChefsKnifeDamageHandler",
            "com.carrot123.until_eternity.compat.revelationfix.NetherworldKatanaDamageHandler"
    };

    private RevelationFixAbsoluteDamageBootstrap() {
    }

    public static void registerIfLoaded() {
        ModList mods = ModList.get();
        if (!mods.isLoaded("goety_revelation") || !mods.isLoaded("revelationfix")) {
            return;
        }

        try {
            for (String handler : HANDLERS) {
                Class.forName(handler).getMethod("register").invoke(null);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to load RevelationFix absolute damage compatibility", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Unable to register RevelationFix absolute damage compatibility",
                    exception.getCause()
            );
        }
        until_eternity.LOGGER.info("Registered RevelationFix absolute weapon damage compatibility");
    }
}
