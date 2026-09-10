package io.zershyan.sccore.datagen.provider;

import com.mojang.logging.LogUtils;
import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.datagen.init.SCCLang;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;


public class SCCLangProvider extends LanguageProvider {
    private static final String enUs = "en_us";
    private static final String zhCn = "zh_cn";
    private final String locale;

    public SCCLangProvider(PackOutput output, String locale) {
        super(output, SCCore.MODID, locale);
        this.locale = locale;
    }

    public static SCCLangProvider runZhCn(PackOutput output) {
        return new SCCLangProvider(output, zhCn);
    }

    public static SCCLangProvider runEnUs(PackOutput output) {
        return new SCCLangProvider(output, enUs);
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case enUs -> SCCLang.getAllLang().forEach(langEntity ->
                    addTranslation(langEntity.key(), langEntity.lang().enDesc())
            );
            case zhCn -> SCCLang.getAllLang().forEach(langEntity ->
                    addTranslation(langEntity.key(), langEntity.lang().zhDesc())
            );
        }
    }

    private <T> void addTranslation(T o, String string) {
        switch (o) {
            case Item object -> add(object, string);
            case Block object -> add(object, string);
            case String object -> add(object, string);
            case ItemStack object -> add(object.getItem(), string);
            case MobEffect object -> add(object, string);
            case EntityType<?> object -> add(object, string);
            case TagKey<?> object -> add(object, string);
            default -> {
                LogUtils.getLogger().error("Unknown object type: {}", o.getClass());
                add(o.toString(), string);
            }
        }
    }
}
