package com.spark.platform.common.datasource.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ProjectName: spark-platform
 * @Package: com.spark.platform.common.base.datasource.emuns
 * @ClassName: DataScopeTypeEnum
 * @Author: wangdingfeng
 * @Description: 数据权限枚举
 * @Date: 2020/6/8 17:13
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DataScopeTypeEnum {

    /**
     * 全部
     */
    ALL(1, "全部"),
    /**
     * 本级
     */
    THIS_LEVEL(2, "本级"),
    /**
     * 自定义
     */
    CUSTOMIZE(3, "自定义");

    private int type;

    private String description;
}

