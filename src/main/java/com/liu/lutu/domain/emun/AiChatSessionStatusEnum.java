package com.liu.lutu.domain.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI对话会话状态枚举
 *
 * @author liu
 * @since 2026-04-04
 */
@Getter
@AllArgsConstructor
public enum AiChatSessionStatusEnum {

	/**
	 * 对话中
	 */
	IN_PROGRESS(1, "对话中"),

	/**
	 * 已完成
	 */
	COMPLETED(2, "已完成"),

	/**
	 * 已关闭
	 */
	CLOSED(3, "已关闭");

	/**
	 * 状态码
	 */
	private final Integer code;

	/**
	 * 状态描述
	 */
	private final String desc;

	/**
	 * 根据状态码获取枚举
	 *
	 * @param code 状态码
	 * @return 枚举值
	 */
	public static AiChatSessionStatusEnum getByCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (AiChatSessionStatusEnum status : values()) {
			if (status.getCode().equals(code)) {
				return status;
			}
		}
		return null;
	}
}
