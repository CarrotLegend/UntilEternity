package com.carrot123.until_eternity.mixin.compat.eternalcareer;

import com.carrot123.until_eternity.recipe.StormAltarRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "com.carrot123.eternal_career.soulblessing.SoulBlessingItem", remap = false)
public abstract class SoulBlessingStormAltarMixin {
    @Inject(method = "getSoulBlessingAttributes(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private void untilEternity$stormAltarAttributes(ItemStack stack, CallbackInfoReturnable<List<?>> cir) {
        if (!stack.hasTag() || !stack.getTag().contains(StormAltarRecipe.OVERRIDES_KEY, 9)) return;
        ListTag overrides = stack.getTag().getList(StormAltarRecipe.OVERRIDES_KEY, 10);
        if (overrides.isEmpty()) return;
        List<Object> modified = new ArrayList<>(cir.getReturnValue());
        try {
            Class<?> entryClass = Class.forName("com.carrot123.eternal_career.soulblessing.SoulBlessingAttribute");
            Method attributeGetter = entryClass.getMethod("attribute");
            Constructor<?> constructor = entryClass.getConstructor(Attribute.class, double.class, AttributeModifier.Operation.class);
            for (int n = 0; n < overrides.size(); n++) {
                CompoundTag data = overrides.getCompound(n);
                ResourceLocation id = ResourceLocation.tryParse(data.getString("Attribute"));
                if (id == null || !ForgeRegistries.ATTRIBUTES.containsKey(id)) continue;
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                AttributeModifier.Operation operation = switch (data.getString("Operation")) {
                    case "addition" -> AttributeModifier.Operation.ADDITION;
                    case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
                    case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
                    default -> null;
                };
                if (operation == null || !data.contains("Amount", 99)) continue;
                Object replacement = constructor.newInstance(attribute, data.getDouble("Amount"), operation);
                boolean replaced = false;
                for (int i = 0; i < modified.size(); i++) {
                    if (attributeGetter.invoke(modified.get(i)) == attribute) {
                        modified.set(i, replacement);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) modified.add(replacement);
            }
            cir.setReturnValue(List.copyOf(modified));
        } catch (ReflectiveOperationException ignored) {
            // A changed EternalCareer API leaves the original blessing untouched.
        }
    }
}
