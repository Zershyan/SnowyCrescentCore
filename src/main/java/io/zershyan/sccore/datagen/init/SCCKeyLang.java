package io.zershyan.sccore.datagen.init;

import io.zershyan.sccore.SCCore;
import io.zershyan.sccore.datagen.util.LazyComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class SCCKeyLang extends SCCLang {
    private static final List<FinalEntry<String>> TranslatableLang = new ArrayList<>();
    private static final String ModId = SCCore.MODID;
    private static final String ModName = SCCore.class.getSimpleName();
    public static final MutableComponent Resource = entry(ModId + ".resources", "Resources for " + ModName, ModName + "资源");
    private static final String ModFullName = SCCore.NAME;
    private static final String MessagePrefix = "message." + ModId;
    private static final String CommandPrefix = "command." + ModId + ".";
    private static final String AnimationPrefix  = "animation.";
    // 指令
    public static final MutableComponent CommandRunFail = entry(AnimationPrefix + CommandPrefix + "fail",
            "命令执行失败。",
            "Command run fail.");
    public static final MutableComponent CommandRunSuccess = entry(AnimationPrefix + CommandPrefix + "success",
            "命令执行成功。",
            "Command run success.");
    public static final LazyComponent PlayAnimationFail = entryLazy(AnimationPrefix + CommandPrefix + "play_animation_fail",
            "在这些玩家上播放动画失败：%s",
            "Fail to play animation with: %s");
    public static final LazyComponent PlayAnimationSuccess = entryLazy(AnimationPrefix + CommandPrefix + "play_animation_success",
            "在%s个玩家上播放动画成功。",
            "Successfully played animation on %s player(s).");
    public static final LazyComponent RemoveAnimationFail = entryLazy(AnimationPrefix + CommandPrefix + "remove_animation_fail",
            "在这些玩家上移除动画失败：%s",
            "Fail to remove animation with: %s");
    public static final LazyComponent RemoveAnimationSuccess = entryLazy(AnimationPrefix + CommandPrefix + "remove_animation_success",
            "在%s个玩家上移除动画成功。",
            "Successfully removed animation on %s player(s).");
    public static final MutableComponent ClearAnimations = entry(AnimationPrefix + CommandPrefix + "clear_animations",
            "动画已清除。",
            "Animation cleared.");
    public static final MutableComponent RefreshAnimations = entry(AnimationPrefix + CommandPrefix + "refresh_animations",
            "动画同步状态已刷新。",
            "Animation refreshed.");
    public static final MutableComponent AcceptMessageClick = entry(AnimationPrefix + CommandPrefix + "accept_message_click",
            "单击此处同意。",
            "Click here to accept.");
    public static final MutableComponent InviteMessage = entry(AnimationPrefix + CommandPrefix + "invite_message",
            "已发送邀请。",
            "Invitation sent.");
    public static final LazyComponent InvitedMessage = entryLazy(AnimationPrefix + CommandPrefix + "invited_message",
            "%s§c§l 邀请§r你进行动画：%s。",
            "%s§c§l invites§r you to animation: %s.");
    public static final MutableComponent AcceptInviteSuccess = entry(AnimationPrefix + CommandPrefix + "accept_invite_success",
            "已接受邀请。",
            "Invitation accepted.");
    public static final LazyComponent InviteSuccess = entryLazy(AnimationPrefix + CommandPrefix + "invite_success",
            "%s 接受了你的动画邀请。",
            "%s has accepted your animation invitation.");
    public static final MutableComponent RequestMessage = entry(AnimationPrefix + CommandPrefix + "request_message",
            "已发送请求。",
            "Request sent.");
    public static final LazyComponent RequestedMessage = entryLazy(AnimationPrefix + CommandPrefix + "requested_message",
            "%s§d§l 请求§r你进行动画：%s。",
            "%s§d§l requests§r you to animation: %s. ");
    public static final MutableComponent AcceptRequestSuccess = entry(AnimationPrefix + CommandPrefix + "accept_request_success",
            "已接受请求。",
            "Request accepted.");
    public static final LazyComponent RequestSuccess = entryLazy(AnimationPrefix + CommandPrefix + "request_success",
            "%s 接受了你的动画请求。",
            "%s has accepted your animation request.");
    public static final MutableComponent ApplyJoinMessage = entry(AnimationPrefix + CommandPrefix + "apply_join_message",
            "已发送申请。",
            "Application sent.");
    public static final LazyComponent AppliedJoinMessage = entryLazy(AnimationPrefix + CommandPrefix + "applied_join_message",
            "%s§b§l 申请§r加入动画。",
            "%S§b§l Apply for §r to join your animation.");
    public static final LazyComponent AcceptApplySuccess = entryLazy(AnimationPrefix + CommandPrefix + "accept_apply_success",
            "%s 接受了 %s 的申请。",
            "%s has accepted the application of %s.");
    public static final LazyComponent ApplySuccess = entryLazy(AnimationPrefix + CommandPrefix + "apply_success",
            "%s 接受了你的动画申请。",
            "%s has accepted your animation application.");
    public static final LazyComponent AnimationToJson = entryLazy(AnimationPrefix + CommandPrefix + "animation_to_json",
            "JSON示例已生成到了路径：游戏根目录/%s",
            "The JSON example has been generated into the path: Game_Root_Directory/%s");
    public static final LazyComponent ListAnimationResource = entryLazy(AnimationPrefix + CommandPrefix + "list_animation_resource",
            "%s侧的%s有：%s",
            "The %2$s on %1$s has : %s");
    public static final MutableComponent AnimationExpire = entry(AnimationPrefix + CommandPrefix + "animation_expire",
            "你不能执行该操作: 已过期。",
            "You cannot perform this operation: It has expired.");
    public static final LazyComponent AnimationOutRange = entryLazy(AnimationPrefix + CommandPrefix + "animation_out_range",
            "你不能执行该操作: 距离不在%s格以内。",
            "You cannot perform this operation: The distance is not within %s blocks.");
    public static final MutableComponent AnimationUnsupportedOperation = entry(AnimationPrefix + CommandPrefix + "animation_unsupported_operation",
            "错误: 不支持这样做。",
            "Error: Unsupported operation.");
    public static final LazyComponent AnimationCooldown = entryLazy(AnimationPrefix + CommandPrefix + "animation_cooldown",
            "你不能执行该操作: 冷却中(%s秒)。",
            "You cannot perform this operation: Cooling down (%s second(s)).");
    public static final LazyComponent AnimationResourceNotFound = entryLazy(AnimationPrefix + CommandPrefix + "animation_resource_not_found",
            "错误: 资源未找到，请检查资源或操作是否有误: %s",
            "Error: Resource not found, please check if there are any errors in the resource or operation : %s");
    public static final MutableComponent AnimationCancelledOperation = entry(AnimationPrefix + CommandPrefix + "animation_cancelled_operation",
            "异常: 操作被取消。",
            "Exception: Operation cancelled.");

    private static String entryString(String key, String enUs, String zhCn) {
        TranslatableLang.add(new FinalEntry<>(key, enUs, zhCn));
        return key;
    }

    private static MutableComponent entry(String key, String enUs, String zhCn) {
        return Component.translatable(entryString(key, enUs, zhCn));
    }

    private static LazyComponent entryLazy(String key, String enUs, String zhCn) {
        return new LazyComponent(entryString(key, enUs, zhCn));
    }
}
