package com.carrot123.until_eternity.compat.mixin;

import java.util.List;
import java.util.Set;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.FMLLoader;

public final class UntilEternityMixinPlugin implements IMixinConfigPlugin {

    private static final String AETHER_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.aether.";

    private static final String SUMMONING_RITUALS_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.summoningrituals.";

    private static final String REVELATION_FIX_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.revelationfix.";

    private static final String MOWZIES_MOBS_COMPAT =
            "com.carrot123.until_eternity.mixin.compat.mowziesmobs.";

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

        if (mixinClassName.startsWith(SUMMONING_RITUALS_COMPAT)) {
            return isModLoaded("summoningrituals");
        }

        if (mixinClassName.startsWith(REVELATION_FIX_COMPAT)) {
            return isModLoaded("goety_revelation")
                    && isModLoaded("revelationfix");
        }

        if (mixinClassName.startsWith(MOWZIES_MOBS_COMPAT)) {
            return isModLoaded("mowziesmobs");
        }

        return true;
    }

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
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
        return null;
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
    }
}
