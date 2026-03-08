package com.evaluation.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class IdResetUtil {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 删除记录后重置表的自增ID为当前最大ID+1
     */
    public void resetAutoIncrement(String tableName) {
        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` AUTO_INCREMENT = 1");
    }
}
