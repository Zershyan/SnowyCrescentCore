package io.zershyan.sccore.mixin;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SCCoreMixinPlugin implements IMixinConfigPlugin {
    /**
     * 只表示加载环境的分类包名，不作为 modid 参与判定。
     */
    private static final Set<String> ENV_PACKAGES = Set.of("client", "server", "common");

    private String mixinPackage = "";
    private Set<String> loadedModIds = Set.of();

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
        this.loadedModIds = LoadingModList.get().getMods().stream()
                .map(ModInfo::getModId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    /**
     * mixin软件包下其他Mixin类请严格按照 "modid/modid/.../MixinXXX.class"来命名<br><br>
     *
     * 程序会遍历软件包名，检查是否加载了modid与软件包名一致的mod<br>若无则不会加载该软件包的所有mixinClass<br><br>
     *
     * 若遍历到的软件包名为client/server/common中的任意一个<br>其软件包内的所有mixinClass都会生效
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // 类名不属于本 mixin 包时一律不加载，避免包名解析落到约定之外
        if (!mixinClassName.startsWith(mixinPackage + ".")) return false;
        String[] segments = mixinClassName.substring(mixinPackage.length() + 1).split("\\.");
        // 逐段检查分类包名，最后一段是类名本身，不参与判定
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            if (ENV_PACKAGES.contains(segment)) return true;
            if (!loadedModIds.contains(segment)) return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
