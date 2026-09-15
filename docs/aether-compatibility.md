# The Aether 兼容：重力晶镐与生命补片

## 检查依据

- Minecraft 1.20.1 / Forge 47.4.20 / Java 17。
- 经用户确认，目标为本机 Gradle 缓存的映射版 `aether-1.20.1-1.5.2-neoforge_mapped_official_1.20.1.jar`。
- Manifest 版本：`1.20.1-1.5.2-neoforge`；mods.toml 声明 Forge `[47.1.0,)`。
- 检查副本 SHA-256：`7C435386DC444019265D7207C3E73040EA86F330E8D43A5478082CC921109677`。
- 从实际注册代码确认 `aether:gravitite_pickaxe`、`aether:slider`、`aether:life_shard`。
- 使用本机 Vineflower 1.10.1 反编译 Slider、LifeShardItem、AetherPlayerCapability、AetherOverlays；副本与反编译结果位于忽略版本控制的 `build/aether-audit/`。未修改原 jar。

## 伤害流程

`com.aetherteam.aether.entity.monster.dungeon.boss.Slider.hurt()` 先调用私有 `canDamageSlider()`，后进入父类 `hurt()`。资格检查包含非和平难度、攻击者在 Boss 房间内，以及主手的 `PICKAXE_DIG`、Slider 允许物品标签或雕纹石正确工具判定。Slider 没有在本类另行覆盖 `isInvulnerableTo()`。

玩家攻击 Mixin 只包围 `Player.attack()` 的主目标 `Entity.hurt()` 调用，保存当时的主手匹配结果、玩家、目标、DamageSource 引用。要求实际玩家直接 `PLAYER_ATTACK`，从而排除仅带玩家来源的命令伤害。嵌套调用遮蔽外层上下文，返回或异常均恢复上下文。

`LivingDamageEvent` 的 LOWEST 处理器将通过上述匹配的正伤害乘 2.5，发生在原有工具门槛和减免之后。上下文只能被领取一次；Mixin 不乘伤害，也不额外调用 hurt。其他模组在同优先级且更晚执行的监听器仍可调整或取消伤害。

Slider 首次受到有效攻击时会启动战斗，并在 `start()` 内恢复满血。对照测试应先唤醒 Boss，并避开致死截断及受伤冷却，不能用首次唤醒后的净生命差评价倍率。

## 生命补片与旧存档

`LifeShardItem.use()` 在非创造模式下检查计数上限，服务端消耗物品，通过原同步机制将计数加一。

`AetherPlayerCapability` 保存 NBT `LifeShardCount`，克隆玩家时复制计数，服务端更新调用 `handleLifeShardModifier()`，移除同 UUID 修饰器后重新添加 transient 修饰器。固定 UUID 为 `E11710C8-4247-4CB6-B3B5-729CB34CFC1A`。

本修改只将 `getLifeShardHealthAttributeModifier()` 内唯一的 `2.0F` 改为 `20.0F`，保持 ADDITION 和原 UUID。原计数为 3 的存档会重建为 +60，而不是 +66；没有新增 NBT、迁移补偿、随机 UUID 或第二套修饰器。属性增加由原版更新周期应用，不额外改变治疗行为。

`getLifeShardLimit()` 仍读取 `maximum_life_shards`，此版本默认 10。无其他属性修饰时，计数 0/1/2/3/10 对应最大生命 20/40/60/80/220。

银色心读取实际最大生命及 Life Shard 修饰器数值计算，而非简单把计数画成心。因此保留其渲染实现，让显示自然对应新的生命值。客户端和服务端均应安装更新后的 Core Mod，以保持该计算一致。

## 自动检查与复现

执行 `gradlew.bat build --offline`，普通 jar 与 `-all.jar` 均构建成功；后者包含项目现有 MixinExtras。二者本次兼容类、Mixin 配置和 refmap 内容一致，玩家攻击/伤害调用的生产映射均已生成。

7 项 JUnit 检查全部通过（0 跳过、0 失败）：作用域嵌套及异常恢复、唯一倍率位置、攻击来源/物品 ID/单次领取的字节码约束、精确常量注入配置、实际 jar 的计数与属性替换、Slider 检查顺序、银色心数据来源。最终构建日志为 `build/aether-audit/final-build.log`，结果 `BUILD SUCCESSFUL`。

这组检查不启动 Minecraft，也不等同于 Mixin 实际加载或游戏内验收。实际 jar 检查读取 `build/aether-audit/aether.jar`；其他机器需将上述映射版 jar 复制到该位置。文件缺失时，3 项外部 jar 检查明确跳过，不增加 Aether 构建依赖。

已尝试在 `build/aether-audit/no-aether-run` 独立目录运行无 Aether 的 GameTest 服务端。启动被现有 `revelationfix.mixins.json:LivingEntityMixin` 阻断：`@Shadow method m_21204_ ... was not located in ... LivingEntity`。因此完整的无 Aether 启动仍未验收；没有为此修改其他模组。日志为 `build/aether-audit/no-aether-smoke.log`。

## 游戏内验收步骤（尚未完成）

1. 相同条件、已唤醒 Slider：钻石镐保持基线；重力晶镐为基线 2.5 倍；重复暴击、力量、附魔组合。
2. 重力晶镐打僵尸，其他工具打 Slider，手持镐发射投射物，以及带玩家来源的 `/damage` 均不获得本倍率。检查房间外、和平模式、无敌、取消攻击仍保留原行为。
3. 新生存玩家连续使用生命补片，等待属性更新后读取最大生命；到原配置上限后消费与提示不变。
4. 对已有计数 3 的存档副本重登、死亡重生、跨维度、重启服务端，核对计数不变且仅有同 UUID 的 +60 修饰器。另加其他最大生命修饰器验证共存。
5. 客户端检查满血、残血、吸收生命、不同 GUI 缩放下的银色心布局，尤其是 10 片的高生命场景。
6. 使用相同其他前置、移除 Aether 的独立实例检查启动。

## 本次源码文件

- `src/main/java/com/carrot123/until_eternity/combat/GravititePickaxeAttackContext.java`（新增）
- `src/main/java/com/carrot123/until_eternity/event/GravititePickaxeDamageEvents.java`（新增）
- `src/main/java/com/carrot123/until_eternity/mixin/compat/aether/GravititePickaxePlayerAttackMixin.java`（新增）
- `src/main/java/com/carrot123/until_eternity/mixin/compat/aether/AetherPlayerLifeShardMixin.java`（新增）
- `src/main/java/com/carrot123/until_eternity/compat/mixin/UntilEternityMixinPlugin.java`（修改：Aether 门控）
- `src/main/resources/until_eternity.mixins.json`（修改：注册两个 Mixin）
- `src/test/java/com/carrot123/until_eternity/compat/AetherCompatibilityTest.java`（新增）
- `docs/aether-compatibility.md`（本文）

构建脚本、依赖设计、Aether 资源和工作区原有无关源码修改未改动。
