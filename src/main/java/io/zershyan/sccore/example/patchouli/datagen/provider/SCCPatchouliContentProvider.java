package io.zershyan.sccore.example.patchouli.datagen.provider;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.compat.patchouli.api.datagen.PatchouliContentProvider;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliCategoryData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliEntryData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.IPatchouliTemplateData;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.component.EntityComponent;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.component.TextComponent;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.format.*;
import io.zershyan.sccore.compat.patchouli.api.datagen.data.page.*;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;

import java.util.concurrent.CompletableFuture;

public class SCCPatchouliContentProvider extends PatchouliContentProvider {
    public SCCPatchouliContentProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(SCCore.MODID, output, registries);
    }

    /**
     * All the text is lang-key available <br>
     * You can find all the relevant content in this: <a href="https://vazkiimods.github.io/Patchouli/docs/intro">Patchouli WIKI</a>
     * @param provider provider
     * @param output pack output
     * @see IPatchouliCategoryData
     * @see IPatchouliTemplateData
     * @see IPatchouliEntryData
     */
    @Override
    protected void addContent(HolderLookup.Provider provider, PackOutput output) {
        //Create category
        IPatchouliCategoryData category1 = createCategory(
                ResourceLocation.fromNamespaceAndPath("lexicon", "category1"),
                "category1",
                "This is category1.",
                ItemFormat.of(Items.APPLE)
        ).sortNum(0);
        IPatchouliCategoryData myDirectoryCategory1 = createCategory(
                //directory available
                ResourceLocation.fromNamespaceAndPath("lexicon", "my_directory/category1"),
                "category2",
                "This is category2.",
                ItemFormat.of(Items.CRAFTING_TABLE)
        ).sortNum(1);

        //Create variable through this way
        Variable<EntityFormat> entity = new Variable<>("entity");
        Variable<StringFormat> text = new Variable<>("text");
        Variable<StringFormat> text1 = new Variable<>("text1");

        //Template component
        TextComponent textComponent = new TextComponent(text);
        TextComponent text1Component = new TextComponent(text1);
        EntityComponent entityComponent = new EntityComponent(entity);

        //Create template
        IPatchouliTemplateData template0 = createTemplate(
                ResourceLocation.fromNamespaceAndPath("lexicon", "util/template0")
        ).addComponents(textComponent, entityComponent);
        IPatchouliTemplateData template = createTemplate(
                ResourceLocation.fromNamespaceAndPath("lexicon", "my_template")
        ).addComponents(text1Component).include(
                TemplateInclude.of(template0, "template0")
                        .x(100)
                        .y(100)
                        //Using Ins meaning: "var" : "value"
                        .usingIns(text.assignment("This is text in template0"))
                        //Using var meaning: "var" : "#var"
                        .usingVar(entity)
        );
        IPatchouliEntryData entry1 = createEntry(
                ResourceLocation.fromNamespaceAndPath("lexicon", "entry0"),
                "Entry 0 name",
                category1.getId(),
                ItemFormat.of(Items.OAK_LOG)
        ).addPages(new TextPage("This is text1 content.").title("This is text1 title."))
                .addPages(new LinkPage("https://www.bing.com/", "Click to open Bing."));
        createEntry(
                ResourceLocation.fromNamespaceAndPath("lexicon", "entry1"),
                "Entry 1 name",
                category1.getId(),
                ItemFormat.of(Items.DIAMOND)
        ).addPages(new TextPage("This is entry1."))
                .addPages(new SpotlightPage(ItemFormat.Multi.tagOf(ItemTags.WOOL)).linkRecipe(true).text("This is wool."))
                .addPages(new ImagePage()
                        .addImage(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/wheat.png"))
                        .border(true)
                        .text("This is wheat icon.")
                );
        createEntry(
                ResourceLocation.fromNamespaceAndPath("lexicon", "my_entry/entry0"),
                "My entry 0 name",
                category1.getId(),
                ItemFormat.of(Items.DIAMOND_SWORD)
        ).addPages(new TextPage("This is entry0 of my entry."))
                .addPages(new CraftingRecipePage(ResourceLocation.fromNamespaceAndPath("minecraft", "crafting_table")))
                .addPages(new SmeltingRecipePage(ResourceLocation.fromNamespaceAndPath("minecraft", "stone")))
                .addPages(new TemplatePage(template).addVariable(
                        //Assignment value to variable
                        //It will parse to string: "var" : "value"
                        text1.assignment("This is text1, this is a chicken."),
                        entity.assignment(EntityFormat.of(EntityType.CHICKEN))
                ))
                .addPages(new EmptyPage())
                .addPages(new EntityPage(EntityFormat.of(EntityType.HORSE)))
                .addPages(new QuestPage().trigger(null))
                .addPages(new RelationsPage().addEntries(entry1))
                .addPages(new MultiblockPage(
                        "This is a custom multiblock",
                        MultiblockFormat.create()
                                .mapping('S', BlockStateFormat.of(Blocks.STONE))
                                .mapping('D', BlockStateFormat.of(Blocks.DIAMOND_BLOCK))
                                .mapping('R', BlockStateFormat.of(Blocks.REDSTONE_BLOCK))
                                .mapping('Q', BlockStateFormat.of(Blocks.QUARTZ_BLOCK))
                                //It is a tag key.
                                .mapping('W', BlockStateFormat.tagOf(BlockTags.WOOL))
                                //You can add a property to block unless it is a tag key
                                .mapping('B', BlockStateFormat.of(Blocks.STONE_STAIRS)
                                        .addProperty(StairBlock.FACING, Direction.EAST.getName())
                                )
                                //Blank meaning air, "_" meaning anything.
                                .pattern(" QRQ ", " QBQ ", " QWQ ", " QBQ ", " QRQ ")
                                .pattern("_____", "DDDDD", "SSSSS", "DDDDD", "_____")
                                //Set multiblock center to pattern
                                //X is the pattern index of all patterns. (Start with 0)
                                //Y is the string index of the pattern. (Start with 0)
                                //Z is the character index of the string. (Start with 0)
                                //In this case:
                                //X is 1, point to the second pattern.
                                //Y is 2, point to the third string.
                                //Z is 2, point to the third character.
                                //The center index points to the third character of "SSSSS". It is "SS[S]SS"
                                .centerIndex(new Vec3i(1, 2, 2))
                                .symmetrical(true)

                ));
    }
}
