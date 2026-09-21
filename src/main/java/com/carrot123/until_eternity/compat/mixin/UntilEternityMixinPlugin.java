package com.carrot123.until_eternity.compat.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;

import net.minecraftforge.fml.loading.FMLLoader;

public final class UntilEternityMixinPlugin
        implements IMixinConfigPlugin {

    private static final String AETHER_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.aether.";

    private static final String SUMMONING_RITUALS_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.summoningrituals.";

    private static final String REVELATION_FIX_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.revelationfix.";

    private static final String MOWZIES_MOBS_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.mowziesmobs.";

    private static final String ENIGMATIC_LEGACY_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.enigmaticlegacy.";

    private static final String ENIGMATIC_DELICACY_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.enigmaticdelicacy.";

    private static final String ENIGMATIC_ADDONS_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.enigmaticaddons.";

    private static final String NIGHT_SCROLL_CLASS =
            "auviotre.enigmatic.addon.contents.items.NightScroll";

    private static final String BERSERK_EMBLEM_CLASS =
            "com.aizistral.enigmaticlegacy.items.BerserkEmblem";

    private static final String IBETRAYED_INTERFACE =
            "auviotre/enigmatic/addon/api/items/IBetrayed";

    private static final String IBLESSED_INTERFACE =
            "auviotre/enigmatic/addon/api/items/IBlessed";

    private static final String ICURSED_INTERFACE =
            "com/aizistral/enigmaticlegacy/api/items/ICursed";

    @Override
    public void onLoad(String mixinPackage) {
        MixinSquaredBootstrap.init();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(
            String targetClassName,
            String mixinClassName
    ) {
        if (mixinClassName.startsWith(AETHER_COMPAT)) {
            return isModLoaded("aether");
        }

        if (mixinClassName.startsWith(
                SUMMONING_RITUALS_COMPAT
        )) {
            return isModLoaded("summoningrituals");
        }

        if (mixinClassName.startsWith(
                REVELATION_FIX_COMPAT
        )) {
            return isModLoaded("goety_revelation")
                    && isModLoaded("revelationfix");
        }

        if (mixinClassName.startsWith(
                MOWZIES_MOBS_COMPAT
        )) {
            return isModLoaded("mowziesmobs");
        }

        if (mixinClassName.startsWith(
                ENIGMATIC_LEGACY_COMPAT
        )) {
            return isModLoaded("enigmaticlegacy");
        }

        if (mixinClassName.startsWith(
                ENIGMATIC_DELICACY_COMPAT
        )) {
            return isModLoaded("enigmaticlegacy")
                    && isModLoaded("enigmaticdelicacy");
        }

        if (mixinClassName.startsWith(
                ENIGMATIC_ADDONS_COMPAT
        )) {
            return isModLoaded("enigmaticaddons");
        }

        return true;
    }

    private static boolean isModLoaded(String modId) {
        return FMLLoader
                .getLoadingModList()
                .getModFileById(modId) != null;
    }

    @Override
    public void acceptTargets(
            Set<String> myTargets,
            Set<String> otherTargets
    ) {
    }

    @Override
    public List<String> getMixins() {
        MixinSquaredBootstrap.reOrderExtensions();

        if (!isModLoaded("enigmaticaddons")) {
            return null;
        }

        return List.of(
                "compat.enigmaticaddons.NightScrollRecognitionMixin",
                "compat.enigmaticaddons.BerserkEmblemRecognitionMixin",
                "compat.enigmaticaddons.BlessRingRecognitionMixin",
                "compat.enigmaticaddons.AddonEventHandlerRecognitionMixin"
        );
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
        if (!isModLoaded("enigmaticaddons")) {
            return;
        }

        if (NIGHT_SCROLL_CLASS.equals(targetClassName)) {
            targetClass.interfaces.remove(
                    IBETRAYED_INTERFACE
            );

            addInterface(
                    targetClass,
                    ICURSED_INTERFACE
            );

            addInterface(
                    targetClass,
                    IBLESSED_INTERFACE
            );

            return;
        }

        if (BERSERK_EMBLEM_CLASS.equals(targetClassName)) {
            addInterface(
                    targetClass,
                    IBLESSED_INTERFACE
            );
        }
    }

    private static void addInterface(
            ClassNode targetClass,
            String interfaceName
    ) {
        if (!targetClass.interfaces.contains(interfaceName)) {
            targetClass.interfaces.add(interfaceName);
        }
    }
}