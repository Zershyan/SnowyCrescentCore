package io.zershyan.sccore.datagen.init;

import io.zershyan.sccore.datagen.util.ConfigLangEntry;

import java.util.ArrayList;
import java.util.List;

public class SCCConfigLang extends SCCLang {
    private static final List<FinalEntry<String>> TranslatableLang = new ArrayList<>();
    //server
    public static final ConfigLangEntry InviteValidTime = entry("inviteValidTime", "Invite Valid Time (Seconds)", "邀请过期时间(秒)");
    public static final ConfigLangEntry InviteValidDistance = entry("inviteValidDistance", "Invite Valid Distance (Blocks)", "邀请有效距离(格)");
    public static final ConfigLangEntry InviteCooldown = entry("inviteCooldown", "Invite Cooldown (Seconds)", "邀请冷却时间(秒)");
    public static final ConfigLangEntry ApplyValidTime = entry("applyValidTime", "Apply Valid Time (Seconds)", "申请过期时间(秒)");
    public static final ConfigLangEntry ApplyValidDistance = entry("applyValidDistance", "Apply Valid Distance (Blocks)", "申请有效距离(格)");
    public static final ConfigLangEntry ApplyCooldown = entry("applyCooldown", "Apply Cooldown (Seconds)", "申请冷却时间(秒)");
    public static final ConfigLangEntry RequestValidTime = entry("requestValidTime", "Request Valid Time (Seconds)", "请求过期时间(秒)");
    public static final ConfigLangEntry RequestCooldown = entry("requestCooldown", "Request Cooldown (Seconds)", "请求冷却时间(秒)");

    //client

    //common

    //startup
    public static final ConfigLangEntry EnableExample = entry("enableExample", "Enable SCCore Code Example", "开启SCCore的代码示例");

    //type
    public static final ConfigLangEntry DevelopmentConfig = entry("developmentConfig", "Development Config", "开发环境配置");

    private static ConfigLangEntry entry(String name, String enUs, String zhCn) {
        ConfigLangEntry langEntry = new ConfigLangEntry(name);
        TranslatableLang.add(new FinalEntry<>(langEntry.key(), enUs, zhCn));
        return langEntry;
    }

    @Override
    protected List<Entry> init(List<Entry> entries) {
        entries.addAll(TranslatableLang);
        return entries;
    }
}
