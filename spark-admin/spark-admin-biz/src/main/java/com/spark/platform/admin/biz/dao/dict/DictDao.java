package com.spark.platform.admin.biz.dao.dict;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spark.platform.admin.api.entity.dict.Dict;
import org.springframework.stereotype.Repository;


/**
 * @author: wangdingfeng
 * @Date: 2020/3/21 13:34
 * @Description: 字典 dao
 */
@Repository
public interface DictDao extends BaseMapper<Dict> {

}
