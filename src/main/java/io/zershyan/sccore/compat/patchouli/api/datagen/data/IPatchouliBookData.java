package io.zershyan.sccore.compat.patchouli.api.datagen.data;

import com.google.gson.JsonObject;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.ItemFormat;
import net.minecraft.resources.ResourceLocation;

/**
 * <a href="https://vazkiimods.github.io/Patchouli/docs/reference/book-json">Patchouli WIKI - Book JSON Format</a>
 */
public interface IPatchouliBookData {
    IPatchouliBookData version(String version);

    IPatchouliBookData pamphlet(Boolean pamphlet);

    IPatchouliBookData showProgress(Boolean showProgress);

    IPatchouliBookData showToasts(Boolean showToasts);

    IPatchouliBookData useBlockyFont(Boolean useBlockyFont);

    IPatchouliBookData pauseGame(Boolean pauseGame);

    IPatchouliBookData allowExtensions(Boolean allowExtensions);

    IPatchouliBookData extend(ResourceLocation extend);

    IPatchouliBookData creativeTab(ResourceLocation creativeTab);

    IPatchouliBookData subtitle(String subtitle);

    IPatchouliBookData textColor(String textColor);

    IPatchouliBookData headerColor(String headerColor);

    IPatchouliBookData nameplateColor(String nameplateColor);

    IPatchouliBookData linkColor(String linkColor);

    IPatchouliBookData linkHoverColor(String linkHoverColor);

    IPatchouliBookData progressBarColor(String progressBarColor);

    IPatchouliBookData progressBarBackground(String progressBarBackground);

    IPatchouliBookData.TextOverflowMode textOverflowMode();

    IPatchouliBookData textOverflowMode(String textOverflowMode);

    IPatchouliBookData indexIcon(ItemFormat itemFormat);

    IPatchouliBookData dontGenerateBook(Boolean dontGenerateBook);

    IPatchouliBookData useResourcePack(Boolean useResourcePack);

    IPatchouliBookData i18n(Boolean i18n);

    IPatchouliBookData macros(String key, String value);

    IPatchouliBookData bookTexture(ResourceLocation bookTexture);

    IPatchouliBookData fillerTexture(ResourceLocation fillerTexture);

    IPatchouliBookData craftingTexture(ResourceLocation craftingTexture);

    IPatchouliBookData advancementsTab(ResourceLocation advancementsTab);

    IPatchouliBookData model(ResourceLocation model);

    IPatchouliBookData openSound(ResourceLocation openSound);

    IPatchouliBookData flipSound(ResourceLocation flipSound);

    IPatchouliBookData customBookItem(ResourceLocation customBookItem);

    JsonObject serialize();

    class TextOverflowMode {
        private final IPatchouliBookData bookData;

        public TextOverflowMode(IPatchouliBookData bookData) {
            this.bookData = bookData;
        }

        public IPatchouliBookData overflow() {
            return bookData.textOverflowMode("overflow");
        }

        public IPatchouliBookData resize() {
            return bookData.textOverflowMode("resize");
        }

        public IPatchouliBookData truncate() {
            return bookData.textOverflowMode("truncate");
        }
    }
}
