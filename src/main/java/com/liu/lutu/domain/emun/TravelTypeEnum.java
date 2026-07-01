package com.liu.lutu.domain.emun;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TravelTypeEnum {
    TRAFFIC(1, "交通"),
    PLAY(2, "游玩"),
    HOUSE(3, "住宿");

    /**
     * 类型
     */
    // 存储到数据库时使用
    @EnumValue
    private final Integer code;

    /**
     * 描述
     */
    // 序列化时使用
    private final String desc;

    TravelTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonCreator
    public static TravelTypeEnum fromCode(Integer code) {
        for (TravelTypeEnum value : TravelTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    @JsonValue
    public Integer getJsonValue() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
