package com.carrot123.until_eternity.item.curio;
import net.minecraft.world.item.Item;

public class ImmuneCurioItem extends Item {

    public enum CurioType {
        LIMITED,   // 仅免疫指定原版负面效果 + 火焰/岩浆块伤害
        ALL        // 免疫所有有害效果（含模组） + 火焰/岩浆块伤害
    }

    private final CurioType curioType;

    public ImmuneCurioItem(Properties properties, CurioType curioType) {
        super(properties);
        this.curioType = curioType;
    }

    public CurioType getCurioType() {
        return curioType;
    }
}