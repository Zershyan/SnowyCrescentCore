package com.linearpast.sccore.core.datagen.provider;

import com.linearpast.sccore.SnowyCrescentCore;
import com.linearpast.sccore.core.datagen.ModLang;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;


public class ModLangProvider extends LanguageProvider {

    private final Lang lang;

    static {
        ModLang.initLang();
    }

    public ModLangProvider(PackOutput output, Lang lang) {
        super(output, SnowyCrescentCore.MODID, lang.getLangName());
        this.lang = lang;
    }

    @Override
    protected void addTranslations() {
        switch (lang){
            case EN_US -> ModLang.langList.forEach(langEntity -> addTranslation(langEntity.key(), langEntity.enUs()));
            case ZH_CN -> ModLang.langList.forEach(langEntity -> addTranslation(langEntity.key(), langEntity.zhCn()));
        }
    }

    private <T> void addTranslation(T o, String string) {
        if(o instanceof Item object){
            add(object,string);
        }else if(o instanceof Block object){
            add(object,string);
        }else if(o instanceof String object){
            add(object,string);
        }else if(o instanceof ItemStack object){
            add(object,string);
        }else if(o instanceof Enchantment object){
            add(object,string);
        }else if(o instanceof MobEffect object){
            add(object,string);
        }else if(o instanceof EntityType<?> object) {
            add(object,string);
        }else if(o instanceof SoundEvent object) {
            add(ModLang.getSoundKey(object),string);
        }else {
            throw new RuntimeException("Unknown object type: " + o.getClass());
        }
    }

    public enum Lang{
        
        ZH_CN("zh_cn"),
        EN_US("en_us"),
        ;

        private final String langName;
        Lang(String langName) {
            this.langName = langName;
        }
        public String getLangName() {
            return langName;
        }
    }
}
