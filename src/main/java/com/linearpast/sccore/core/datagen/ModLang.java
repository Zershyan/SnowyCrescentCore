package com.linearpast.sccore.core.datagen;

import com.linearpast.sccore.SnowyCrescentCore;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class ModLang {
	public record LangEntity<T>(T key, String zhCn, String enUs) { }
	public static final List<LangEntity<?>> langList = new ArrayList<>();
	private final static String translationString = "translation." + SnowyCrescentCore.MODID;
	private final static String command = ".command";
	private static final String animation  = ".animation";
	public enum TranslatableMessage{
		COMMAND_RUN_FAIL(new LangEntity<>(
				translationString + command + animation + ".command_run_fail",
				"命令执行失败。",
				"Command run fail."
		)),
		COMMAND_RUN_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".command_run_success",
				"命令执行成功。",
				"Command run success."
		)),
		ANIMATION_NOT_PRESENT(new LangEntity<>(
				translationString + command + animation + ".animation_not_present",
				"动画不存在。",
				"Animation is not present."
		)),
		ANIMATION_LAYER_NOT_PRESENT(new LangEntity<>(
				translationString + command + animation + ".animation_layer_not_present",
				"动画层不存在。",
				"Animation layer is not present."
		)),
		PLAY_ANIMATION_FAIL(new LangEntity<>(
				translationString + command + animation + ".play_animation_fail",
				"在这些玩家上播放动画失败：%s",
				"Fail to play animation with: %s"
		)),
		PLAY_ANIMATION_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".play_animation_success",
				"在%s个玩家上播放动画成功。",
				"Successfully played animation on %s player(s)."
		)),
		REMOVE_ANIMATION_FAIL(new LangEntity<>(
				translationString + command + animation + ".remove_animation_fail",
				"在这些玩家上移除动画失败：%s",
				"Fail to remove animation with: %s"
		)),
		REMOVE_ANIMATION_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".remove_animation_success",
				"在%s个玩家上移除动画成功。",
				"Successfully removed animation on %s player(s)."
		)),
		CLEAR_ANIMATIONS(new LangEntity<>(
				translationString + command + animation + ".clear_animations",
				"动画已清除。",
				"Animation cleared."
		)),
		REFRESH_ANIMATIONS(new LangEntity<>(
				translationString + command + animation + ".refresh_animations",
				"动画同步状态已刷新。",
				"Animation refreshed."
		)),
		ACCEPT_MESSAGE_CLICK(new LangEntity<>(
				translationString + command + animation + ".accept_message_click",
				"单击此处同意。",
				"Click here to accept."
		)),
		INVITE_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".invite_message",
				"已发送邀请。",
				"Invitation sent."
		)),
		INVITED_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".invited_message",
				"%s§c§l 邀请§r你进行动画：%s。",
				"%s§c§l invites§r you to animation: %s. "
		)),
		ACCEPT_INVITE_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".accept_invite_expired",
				"邀请已超时。（%s分钟）",
				"Invite expired.(%s minute(s))"
		)),
		INVITE_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".invite_expired",
				"%s 接受了你的动画邀请，但是邀请超时了。（%s分钟）",
				"%s has accepted your animation invitation but the invitation has expired. (%s minute(s))"
		)),
		ACCEPT_INVITE_TOO_FAR(new LangEntity<>(
				translationString + command + animation + ".accept_invite_too_far",
				"你们距离太远了。（%s格）",
				"You are too far apart. (%s block(s))"
		)),
		INVITE_TOO_FAR(new LangEntity<>(
				translationString + command + animation + ".invite_too_far",
				"%s 接受了你的动画邀请，但你们距离太远了。（%s格）",
				"%s has accepted your animation invitation but you are too far apart. (%s block(s))"
		)),
		ACCEPT_INVITE_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".accept_invite_success",
				"已接受邀请。",
				"Invitation accepted."
		)),
		INVITE_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".invite_success",
				"%s 接受了你的动画邀请。",
				"%s has accepted your animation invitation."
		)),
		REQUEST_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".request_message",
				"已发送请求。",
				"Request sent."
		)),
		REQUESTED_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".requested_message",
				"%s§d§l 请求§r你进行动画：%s。",
				"%s§d§l requests§r you to animation: %s. "
		)),
		ACCEPT_REQUEST_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".accept_request_expired",
				"请求已超时。（%s分钟）",
				"Request expired.(%s minute(s))"
		)),
		REQUEST_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".request_expired",
				"%s 接受了你的动画请求，但是请求超时了。（%s分钟）",
				"%s has accepted your animation request but the request has expired. (%s minute(s))"
		)),
		ACCEPT_REQUEST_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".accept_request_success",
				"已接受请求。",
				"Request accepted."
		)),
		REQUEST_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".request_success",
				"%s 接受了你的动画请求。",
				"%s has accepted your animation request."
		)),
		APPLY_JOIN_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".apply_join_message",
				"已发送申请。",
				"Application sent."
		)),
		APPLIED_JOIN_MESSAGE(new LangEntity<>(
				translationString + command + animation + ".applied_join_message",
				"%s§b§l 申请§r加入动画。",
				"%S§b§l Apply for §r to join your animation. "
		)),
		ACCEPT_APPLY_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".accept_apply_expired",
				"申请已超时。（%s分钟）",
				"Application expired.(%s minute(s))"
		)),
		APPLY_EXPIRED(new LangEntity<>(
				translationString + command + animation + ".apply_expired",
				"%s 接受了你的动画申请，但是申请超时了。（%s分钟）",
				"%s has accepted your animation application but the application has expired. (%s minute(s))"
		)),
		ACCEPT_APPLY_TOO_FAR(new LangEntity<>(
				translationString + command + animation + ".accept_apply_too_far",
				"你们距离太远了。（%s格）",
				"You are too far apart. (%s block(s))"
		)),
		APPLY_TOO_FAR(new LangEntity<>(
				translationString + command + animation + ".apply_too_far",
				"%s 接受了你的动画申请，但你们距离太远了。（%s格）",
				"%s has accepted your animation application but you are too far apart. (%s block(s))"
		)),
		ACCEPT_APPLY_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".accept_apply_success",
				"%s 接受了 %s 的申请。",
				"%s has accepted the application of %s."
		)),
		APPLY_SUCCESS(new LangEntity<>(
				translationString + command + animation + ".apply_success",
				"%s 接受了你的动画申请。",
				"%s has accepted your animation application."
		)),
		WITHOUT_ANIMATION_RIDE_ENTITY(new LangEntity<>(
				translationString + animation + ".without_animation_ride_entity",
				"命令执行错误，已满人或不支持的动画。",
				"Command run fail, full or unsupported animations."
		)),
		COMMAND_COOLDOWN(new LangEntity<>(
				translationString + animation + ".command_cooldown",
				"你不能执行该指令，冷却中：%s 秒。",
				"You cannot execute this command, cooling down: %s seconds."
		)),
		ANIMATION_TO_JSON(new LangEntity<>(
				translationString + command + animation + ".animation_to_json",
				"动画%s已经存储到%s路径：",
				"The animation %s has been stored in the path on %s:"
		)),
		ANIMATION_JSON_PATH(new LangEntity<>(
				translationString + command + animation + ".animation_json_path",
				"%s",
				"%s"
		)),
		UNKNOWN_ANIMATION(new LangEntity<>(
				translationString + animation + ".unknown_animation",
				"未知的动画%s，请检查你的资源包是否完整。",
				"Unknown animation %s, please check if your resource packs is complete."
		)),
		UNSAFE_FILE_DIRECTORY(new LangEntity<>(
				translationString + command + animation + ".unsafe_file_directory",
				"你选择的文件路径并不安全",
				"%s"
		)),
		;

		private final LangEntity<String> langEntity;
		TranslatableMessage(LangEntity<String> lang){
			this.langEntity = lang;
		}

		public String getKey() {
			return langEntity.key;
		}
	}

	public static void initLang() {
		langList.clear();

		initLangMessage();
	}

	private static void initLangMessage() {
		for (TranslatableMessage value : TranslatableMessage.values()) {
			langList.add(value.langEntity);
		}
	}

	public static String getSoundKey(SoundEvent soundEvent){
		return "subtitle." + SnowyCrescentCore.MODID + ".sound." + soundEvent.getLocation().getPath();
	}
}
