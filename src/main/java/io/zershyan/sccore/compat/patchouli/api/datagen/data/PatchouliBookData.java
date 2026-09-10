package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PatchouliBookData implements IPatchouliBookData {
    @NotNull
    private final String name;
    @NotNull
    private final String landingText;
    private String version;
    private String subtitle;
    private String textColor;
    private String headerColor;
    private String nameplateColor;
    private String linkColor;
    private String linkHoverColor;
    private String progressBarColor;
    private String progressBarBackground;
    private String textOverflowMode;
    private ItemFormat indexIcon;
    private Boolean dontGenerateBook;
    private Boolean useResourcePack = true;
    private Boolean i18n;
    private Boolean pamphlet;
    private Boolean showProgress;
    private Boolean showToasts;
    private Boolean useBlockyFont;
    private Boolean pauseGame;
    private Boolean allowExtensions;
    private ResourceLocation extend;
    private ResourceLocation creativeTab;
    private ResourceLocation bookTexture;
    private ResourceLocation fillerTexture;
    private ResourceLocation craftingTexture;
    private ResourceLocation advancementsTab;
    private ResourceLocation model;
    private ResourceLocation openSound;
    private ResourceLocation flipSound;
    private ResourceLocation customBookItem;
    private HashMap<String, String> macros;

    public PatchouliBookData(@NotNull String name, @NotNull String landingText) {
        this.name = name;
        this.landingText = landingText;
    }

    @Override
    public PatchouliBookData version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public PatchouliBookData subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @Override
    public PatchouliBookData textColor(String textColor) {
        this.textColor = textColor;
        return this;
    }

    @Override
    public PatchouliBookData headerColor(String headerColor) {
        this.headerColor = headerColor;
        return this;
    }

    @Override
    public PatchouliBookData nameplateColor(String nameplateColor) {
        this.nameplateColor = nameplateColor;
        return this;
    }

    @Override
    public PatchouliBookData linkColor(String linkColor) {
        this.linkColor = linkColor;
        return this;
    }

    @Override
    public PatchouliBookData linkHoverColor(String linkHoverColor) {
        this.linkHoverColor = linkHoverColor;
        return this;
    }

    @Override
    public PatchouliBookData progressBarColor(String progressBarColor) {
        this.progressBarColor = progressBarColor;
        return this;
    }

    @Override
    public PatchouliBookData progressBarBackground(String progressBarBackground) {
        this.progressBarBackground = progressBarBackground;
        return this;
    }

    @Override
    public PatchouliBookData textOverflowMode(String textOverflowMode) {
        this.textOverflowMode = textOverflowMode;
        return this;
    }

    @Override
    public TextOverflowMode textOverflowMode() {
        return new TextOverflowMode(this);
    }

    @Override
    public PatchouliBookData indexIcon(ItemFormat indexIcon) {
        this.indexIcon = indexIcon;
        return this;
    }

    @Override
    public PatchouliBookData dontGenerateBook(Boolean dontGenerateBook) {
        this.dontGenerateBook = dontGenerateBook;
        return this;
    }

    @Override
    public PatchouliBookData useResourcePack(Boolean useResourcePack) {
        this.useResourcePack = useResourcePack;
        return this;
    }

    @Override
    public PatchouliBookData i18n(Boolean i18n) {
        this.i18n = i18n;
        return this;
    }

    @Override
    public PatchouliBookData pamphlet(Boolean pamphlet) {
        this.pamphlet = pamphlet;
        return this;
    }

    @Override
    public PatchouliBookData showProgress(Boolean showProgress) {
        this.showProgress = showProgress;
        return this;
    }

    @Override
    public PatchouliBookData showToasts(Boolean showToasts) {
        this.showToasts = showToasts;
        return this;
    }

    @Override
    public PatchouliBookData useBlockyFont(Boolean useBlockyFont) {
        this.useBlockyFont = useBlockyFont;
        return this;
    }

    @Override
    public PatchouliBookData pauseGame(Boolean pauseGame) {
        this.pauseGame = pauseGame;
        return this;
    }

    @Override
    public PatchouliBookData allowExtensions(Boolean allowExtensions) {
        this.allowExtensions = allowExtensions;
        return this;
    }

    @Override
    public PatchouliBookData extend(ResourceLocation extend) {
        this.extend = extend;
        return this;
    }

    @Override
    public PatchouliBookData creativeTab(ResourceLocation creativeTab) {
        this.creativeTab = creativeTab;
        return this;
    }

    @Override
    public PatchouliBookData bookTexture(ResourceLocation bookTexture) {
        this.bookTexture = bookTexture;
        return this;
    }

    @Override
    public PatchouliBookData fillerTexture(ResourceLocation fillerTexture) {
        this.fillerTexture = fillerTexture;
        return this;
    }

    @Override
    public PatchouliBookData craftingTexture(ResourceLocation craftingTexture) {
        this.craftingTexture = craftingTexture;
        return this;
    }

    @Override
    public PatchouliBookData advancementsTab(ResourceLocation advancementsTab) {
        this.advancementsTab = advancementsTab;
        return this;
    }

    @Override
    public PatchouliBookData model(ResourceLocation model) {
        this.model = model;
        return this;
    }

    @Override
    public PatchouliBookData openSound(ResourceLocation openSound) {
        this.openSound = openSound;
        return this;
    }

    @Override
    public PatchouliBookData flipSound(ResourceLocation flipSound) {
        this.flipSound = flipSound;
        return this;
    }

    @Override
    public PatchouliBookData customBookItem(ResourceLocation customBookItem) {
        this.customBookItem = customBookItem;
        return this;
    }

    @Override
    public PatchouliBookData macros(String key, String value) {
        if(macros == null) {
            macros = new HashMap<>();
        }
        macros.put(key, value);
        return this;
    }

    @Override
    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("landing_text", landingText);
        if(version != null) {
            object.addProperty("version", version);
        }
        if(subtitle != null) {
            object.addProperty("subtitle", subtitle);
        }
        if(textColor != null) {
            object.addProperty("text_color", textColor);
        }
        if(headerColor != null) {
            object.addProperty("header_color", headerColor);
        }
        if(nameplateColor != null) {
            object.addProperty("nameplate_color", nameplateColor);
        }
        if(linkColor != null) {
            object.addProperty("link_color", linkColor);
        }
        if(linkHoverColor != null) {
            object.addProperty("link_hover_color", linkHoverColor);
        }
        if(progressBarColor != null) {
            object.addProperty("progress_bar_color", progressBarColor);
        }
        if(progressBarBackground != null) {
            object.addProperty("progress_bar_background", progressBarBackground);
        }
        if(textOverflowMode != null) {
            object.addProperty("text_overflow_mode", textOverflowMode);
        }
        if(indexIcon != null) {
            object.addProperty("index_icon", indexIcon.parse());
        }
        if(dontGenerateBook != null) {
            object.addProperty("dont_generate_book", dontGenerateBook);
        }
        if(useResourcePack != null) {
            object.addProperty("use_resource_pack", useResourcePack);
        }
        if(i18n != null) {
            object.addProperty("i18n", i18n);
        }
        if(pamphlet != null) {
            object.addProperty("pamphlet", pamphlet);
        }
        if(showProgress != null) {
            object.addProperty("show_progress", showProgress);
        }
        if(showToasts != null) {
            object.addProperty("show_toasts", showToasts);
        }
        if(useBlockyFont != null) {
            object.addProperty("use_blocky_font", useBlockyFont);
        }
        if(pauseGame != null) {
            object.addProperty("pause_game", pauseGame);
        }
        if(allowExtensions != null) {
            object.addProperty("allow_extensions", allowExtensions);
        }
        if(extend != null) {
            object.addProperty("extend", extend.toString());
        }
        if(creativeTab != null) {
            object.addProperty("creative_tab", creativeTab.toString());
        }
        if(bookTexture != null) {
            object.addProperty("book_texture", bookTexture.toString());
        }
        if(fillerTexture != null) {
            object.addProperty("filler_texture", fillerTexture.toString());
        }
        if(craftingTexture != null) {
            object.addProperty("crafting_texture", craftingTexture.toString());
        }
        if(advancementsTab != null) {
            object.addProperty("advancements_tab", advancementsTab.toString());
        }
        if(model != null) {
            object.addProperty("book_model", model.toString());
        }
        if(openSound != null) {
            object.addProperty("open_sound", openSound.toString());
        }
        if(flipSound != null) {
            object.addProperty("flip_sound", flipSound.toString());
        }
        if(customBookItem != null) {
            object.addProperty("custom_book_item", customBookItem.toString());
        }
        if(macros != null) {
            JsonObject macrosObject = new JsonObject();
            macros.forEach(macrosObject::addProperty);
            object.add("macros", macrosObject);
        }
        return object;
    }
}
