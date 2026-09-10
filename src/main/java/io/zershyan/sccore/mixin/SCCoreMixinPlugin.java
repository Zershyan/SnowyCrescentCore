package io.zershyan.sccore.mixin;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SCCoreMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    /**
     * mixin软件包下其他Mixin类请严格按照 "modid/modid/.../MixinXXX.class"来命名<br><br>
     * 程序会遍历软件包名，检查是否加载了modid与软件包名一致的mod<br>若无则不会加载该软件包的所有mixinClass<br><br>
     * 若遍历到的软件包名为client/server/common中的任意一个<br>其软件包内的所有mixinClass都会生效
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<ModInfo> modInfos = LoadingModList.get().getMods();
        List<String> modList = modInfos.stream().map(ModInfo::getModId).toList();
        String modIds = mixinClassName.replace(this.getClass().getPackageName() + ".", "").replaceAll("^(.*)(\\.).*$", "$1");
        for (String string : modIds.split("\\.")) {
            if ("client".equals(string) || "server".equals(string) || "common".equals(string)) {
                return true;
            } else if (!modList.contains(string)) return false;
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
