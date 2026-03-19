package com.evaluation.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class IdResetUtil {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 重置表的 AUTO_INCREMENT 计数器。
     * 这只会影响后续新增记录的起始值，不会重排已有主键，也不会填补中间缺失的 ID。
     */
    public void resetAutoIncrement(String tableName) {
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` AUTO_INCREMENT = 1");
    }
}
