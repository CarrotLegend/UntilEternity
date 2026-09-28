package com.carrot123.until_eternity.item;

import com.carrot123.until_eternity.client.ClientBookOpener;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TavernOwnerDiaryItem extends Item {
    public TavernOwnerDiaryItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("酒馆老板日记");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            ClientBookOpener.open(createReadableBook());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static ItemStack createReadableBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        CompoundTag tag = new CompoundTag();
        tag.putString("title", "酒馆老板日记");
        tag.putString("author", "酒馆老板");
        tag.putInt("generation", 0);
        tag.putBoolean("resolved", true);

        ListTag pages = new ListTag();

        addPage(
                pages,
                "十月 3日",
                "最近镇子外面的骷髅越来越多了。\n\n" +
                "昨晚关门的时候，我还看见两只在路口晃悠。\n\n" +
                "卫兵说没什么好担心的，每年入秋之后亡灵都会多一些。\n\n" +
                "希望如此。"
        );

        addPage(
                pages,
                null,
                "生意倒是没有受到太大影响。\n\n" +
                "佣兵们甚至挺高兴。\n\n" +
                "他们说骷髅越多，委托越多。\n\n" +
                "只希望他们别把沾血的剑放在我的桌子上。"
        );

        addPage(
                pages,
                "十月 11日",
                "又有人失踪了。\n\n" +
                "是住在镇子东边的那个农夫。\n\n" +
                "他的妻子今天来酒馆问有没有人见过他。\n\n" +
                "没人见过。"
        );

        addPage(
                pages,
                null,
                "晚上几个佣兵出去找。\n\n" +
                "最后只带回来一只靴子。\n\n" +
                "上面全是血。\n\n" +
                "今晚关门以后，我在神像前点了两根蜡烛。\n\n" +
                "愿神灵保佑我们。"
        );

        addPage(
                pages,
                "十月 18日",
                "镇子外面的亡灵已经多得不正常了。\n\n" +
                "卫兵把东门封了。\n\n" +
                "从酒馆二楼往外看，晚上能看到远处树林里全是蓝绿色的火光。"
        );

        addPage(
                pages,
                null,
                "一开始我以为是萤火虫。\n\n" +
                "后来才发现，那是亡灵的眼睛。\n\n" +
                "神官说，这是对我们信仰的考验。\n\n" +
                "只要保持虔诚，神灵不会抛弃祂的子民。"
        );

        addPage(
                pages,
                null,
                "所以今天晚上，我又点了四根蜡烛。"
        );

        addPage(
                pages,
                "十月 25日",
                "今天没有商队来。\n\n" +
                "这是我经营酒馆十四年以来第一次。\n\n" +
                "通往南方的路断了。\n\n" +
                "有人说桥那边全是死人。"
        );

        addPage(
                pages,
                null,
                "也有人说附近几个村子已经没人了。\n\n" +
                "不知道真假。\n\n" +
                "酒馆里住满了逃难的人。\n\n" +
                "房间不够，我让他们睡在大厅。"
        );

        addPage(
                pages,
                null,
                "有人主动给钱。\n\n" +
                "我没收。\n\n" +
                "这种时候还收什么钱。"
        );

        addPage(
                pages,
                "十一月 2日",
                "它们开始袭击镇子了。\n\n" +
                "昨天晚上钟响了三次。\n\n" +
                "所有能拿武器的人都去了城墙。\n\n" +
                "早上回来的人不到一半。"
        );

        addPage(
                pages,
                null,
                "老霍恩也没回来。\n\n" +
                "他在我这里喝了二十多年酒。\n\n" +
                "他的杯子还挂在柜台后面。\n\n" +
                "我没拿下来。\n\n" +
                "也许他明天就会回来。"
        );

        addPage(
                pages,
                "十一月 5日",
                "老霍恩回来了。\n\n" +
                "我宁愿他没有。\n\n" +
                "他站在酒馆外面。\n\n" +
                "脸已经烂了一半。\n\n" +
                "一直在撞门。"
        );

        addPage(
                pages,
                null,
                "我认出了他的衣服。\n\n" +
                "他的杯子还在柜台后面。\n\n" +
                "我把它砸了。"
        );

        addPage(
                pages,
                "十一月 9日",
                "神官让所有人集中祈祷。\n\n" +
                "酒馆里的人全去了。\n\n" +
                "我也去了。\n\n" +
                "教堂里挤满了人。"
        );

        addPage(
                pages,
                null,
                "大家跪在地上，一遍又一遍地念祷词。\n\n" +
                "神官告诉我们，神灵一定听得见。\n\n" +
                "一个女人问：\n\n" +
                "“那祂为什么还不救我们？”"
        );

        addPage(
                pages,
                null,
                "没人回答她。"
        );

        addPage(
                pages,
                "十一月 13日",
                "北门失守了。\n\n" +
                "现在没有人敢离开酒馆。\n\n" +
                "我们用桌子和酒桶堵住门。\n\n" +
                "窗户也钉死了。"
        );

        addPage(
                pages,
                null,
                "一共三十七个人。\n\n" +
                "六个孩子。\n\n" +
                "食物还能撑十天。\n\n" +
                "如果省着吃，也许十五天。"
        );

        addPage(
                pages,
                null,
                "水不多。\n\n" +
                "酒倒是很多。\n\n" +
                "真讽刺。"
        );

        addPage(
                pages,
                "十一月 14日",
                "昨晚它们从街上经过。\n\n" +
                "很多。\n\n" +
                "非常多。\n\n" +
                "我们熄了所有灯。\n\n" +
                "没人敢说话。"
        );

        addPage(
                pages,
                null,
                "那个最小的孩子差点哭出来，他母亲死死捂着他的嘴。\n\n" +
                "我们就在黑暗里听。\n\n" +
                "脚步声持续了大概一个小时。\n\n" +
                "也许更久。"
        );

        addPage(
                pages,
                null,
                "我不知道。\n\n" +
                "没有人敢动。\n\n" +
                "我一直握着胸前的神徽。\n\n" +
                "在心里祈祷。\n\n" +
                "求祂别让亡灵发现这里。"
        );

        addPage(
                pages,
                null,
                "最后它们真的走了。\n\n" +
                "神灵还是听见了。\n\n" +
                "我就知道。"
        );

        addPage(
                pages,
                "十一月 16日",
                "它们又来了。\n\n" +
                "这一次在门外停了很久。\n\n" +
                "一直抓门。\n\n" +
                "我们所有人都不敢呼吸。\n\n" +
                "最后还是走了。\n\n" +
                "感谢神灵。"
        );

        addPage(
                pages,
                "十一月 18日",
                "今天有人问我：\n\n" +
                "“如果神真的在保护我们，为什么不直接让那些东西消失？”\n\n" +
                "我让他闭嘴。"
        );

        addPage(
                pages,
                null,
                "不要亵渎神灵。\n\n" +
                "我们现在还活着，就是祂庇护我们的证明。"
        );

        addPage(
                pages,
                "十一月 20日",
                "食物只够五天。"
        );

        addPage(
                pages,
                "十一月 21日",
                "外面已经听不见活人的声音了。"
        );

        addPage(
                pages,
                "十一月 22日",
                "教堂的钟响了。\n\n" +
                "只响了一下。\n\n" +
                "然后停了。\n\n" +
                "晚上有人敲酒馆的门。\n\n" +
                "三下。\n\n" +
                "很轻。"
        );

        addPage(
                pages,
                null,
                "有人说可能是幸存者。\n\n" +
                "我们没有开。\n\n" +
                "那东西在外面站了一夜。\n\n" +
                "第二天早上，从门缝下面流进来一些已经发黑的血。"
        );

        addPage(
                pages,
                "十一月 23日",
                "我们今天没有祈祷。\n\n" +
                "不知道是谁先忘了。\n\n" +
                "等我想起来的时候，已经晚上了。\n\n" +
                "我一个人跪在神像前。"
        );

        addPage(
                pages,
                null,
                "念了很久。\n\n" +
                "没有其他人过来。"
        );

        addPage(
                pages,
                "十一月 24日",
                "有人死了。\n\n" +
                "不是亡灵杀的。\n\n" +
                "是病。\n\n" +
                "我们不知道尸体会不会变成那些东西。"
        );

        addPage(
                pages,
                null,
                "最后只能把他绑在椅子上。\n\n" +
                "他的妻子坐在旁边哭了一整天。\n\n" +
                "半夜，他睁眼了。\n\n" +
                "我们只能杀他第二次。"
        );

        addPage(
                pages,
                "十一月 25日",
                "我今天向神灵许诺。\n\n" +
                "如果我们能活着出去，\n\n" +
                "我会把酒馆卖掉。\n\n" +
                "把所有的钱捐给教会。"
        );

        addPage(
                pages,
                null,
                "我会戒酒。\n\n" +
                "我会每天祈祷。\n\n" +
                "我会成为最虔诚的人。\n\n" +
                "只要祂救我们。"
        );

        addPage(
                pages,
                "十一月 26日",
                "门快撑不住了。"
        );

        addPage(
                pages,
                "十一月 27日",
                "它们知道我们在这里。\n\n" +
                "已经不用保持安静了。\n\n" +
                "整条街都是亡灵。\n\n" +
                "它们在撞门。\n\n" +
                "一下。\n\n" +
                "一下。\n\n" +
                "一下。"
        );

        addPage(
                pages,
                null,
                "桌子在移动。\n\n" +
                "我们把所有能搬的东西都堵上去了。\n\n" +
                "楼上的窗户也开始有声音。\n\n" +
                "它们在爬墙。"
        );

        addPage(
                pages,
                null,
                "神官也在这里。\n\n" +
                "他让所有人祈祷。\n\n" +
                "所以我们跪在地上祈祷。\n\n" +
                "所有人都在喊。\n\n" +
                "求祂降下圣火。"
        );

        addPage(
                pages,
                null,
                "求祂派来天使。\n\n" +
                "求祂让太阳升起来。\n\n" +
                "求祂做什么都好。\n\n" +
                "只要救救我们。"
        );

        addPage(
                pages,
                "十一月 27日，夜",
                "门还在响。\n\n" +
                "神官说，这是最后的考验。\n\n" +
                "他说黎明之前神迹一定会出现。\n\n" +
                "大家相信他。"
        );

        addPage(
                pages,
                null,
                "我也相信。\n\n" +
                "一定会的。"
        );

        addPage(
                pages,
                "十一月 28日",
                "太阳升起来了。\n\n" +
                "没有神迹。"
        );

        addPage(
                pages,
                "十一月 28日，稍晚",
                "门裂了。\n\n" +
                "我们把厨房里的刀都拿出来了。\n\n" +
                "还有斧头。\n\n" +
                "桌腿。\n\n" +
                "酒瓶。"
        );

        addPage(
                pages,
                null,
                "任何能当武器的东西。\n\n" +
                "神官还在祈祷。\n\n" +
                "我让他拿把剑。\n\n" +
                "他说神会保护我们。\n\n" +
                "随他吧。"
        );

        addPage(
                pages,
                "不知道什么时间",
                "门破了。"
        );

        addPage(
                pages,
                "……",
                "太多了。\n\n" +
                "根本杀不完。\n\n" +
                "前面倒下一个。\n\n" +
                "后面还有十个。\n\n" +
                "二楼也进来了。"
        );

        addPage(
                pages,
                null,
                "到处都是血。\n\n" +
                "我不知道还有多少人活着。\n\n" +
                "我躲在酒窖。\n\n" +
                "外面一直有人惨叫。"
        );

        addPage(
                pages,
                null,
                "神官刚才也在叫。\n\n" +
                "他喊得比谁都大声。"
        );

        addPage(
                pages,
                "……",
                "现在安静了一点。\n\n" +
                "门外有东西在走。\n\n" +
                "我的左手被咬了。\n\n" +
                "血止不住。"
        );

        addPage(
                pages,
                null,
                "我把柜子推到了门口。\n\n" +
                "撑不了多久。"
        );

        addPage(
                pages,
                null,
                "我刚才又祈祷了一次。\n\n" +
                "最后一次。\n\n" +
                "我把所有知道的神都叫了一遍。"
        );

        addPage(
                pages,
                null,
                "连那些以前神官不允许我们祭拜的古神，我也求了。\n\n" +
                "我说，\n\n" +
                "谁都可以。\n\n" +
                "只要有一个是真的。"
        );

        addPage(
                pages,
                null,
                "只要有一个愿意救我。\n\n" +
                "我什么都愿意给。\n\n" +
                "什么都没有发生。"
        );

        addPage(
                pages,
                "……",
                "我突然觉得很好笑。\n\n" +
                "这些年，\n\n" +
                "收成好的时候，我们感谢祂们。\n\n" +
                "孩子出生，我们感谢祂们。"
        );

        addPage(
                pages,
                null,
                "病治好了，我们感谢祂们。\n\n" +
                "战争胜利，我们感谢祂们。\n\n" +
                "遇到灾难，神官说这是祂们的考验。"
        );

        addPage(
                pages,
                null,
                "祈祷没有回应，神官说我们的信仰还不够虔诚。\n\n" +
                "人死了，又说这是神的安排。"
        );

        addPage(
                pages,
                null,
                "好像无论发生什么，\n\n" +
                "祂们永远都是对的。\n\n" +
                "门快开了。"
        );

        addPage(
                pages,
                null,
                "如果真的有神，\n\n" +
                "祂们应该听得见这里的声音。\n\n" +
                "三十七个人。\n\n" +
                "六个孩子。"
        );

        addPage(
                pages,
                null,
                "我们祈祷了那么久。\n\n" +
                "没有回应。\n\n" +
                "所以如果有人以后找到这本日记，\n\n" +
                "替我记住一件事。"
        );

        addEmphasisPage(
                pages,
                "不要把自己的命交给神。"
        );

        addPage(
                pages,
                null,
                "能救你的，\n\n" +
                "只有你手里的剑，\n\n" +
                "你身边还活着的人，\n\n" +
                "还有你自己。"
        );

        addPage(
                pages,
                null,
                "至于神——\n\n" +
                "我已经等够了。"
        );

        tag.put("pages", pages);
        book.setTag(tag);

        return book;
    }

    private static void addPage(ListTag pages, String title, String body) {
        Component component;

        if (title != null && !title.isEmpty()) {
            component = Component.literal(title)
                    .withStyle(ChatFormatting.BOLD)
                    .append(Component.literal("\n\n" + body)
                            .withStyle(ChatFormatting.RESET));
        } else {
            component = Component.literal(body);
        }

        pages.add(StringTag.valueOf(Component.Serializer.toJson(component)));
    }

    private static void addEmphasisPage(ListTag pages, String text) {
        Component component = Component.literal(text)
                .withStyle(ChatFormatting.BOLD);

        pages.add(StringTag.valueOf(Component.Serializer.toJson(component)));
    }
}