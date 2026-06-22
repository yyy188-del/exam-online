-- 考试公告表
CREATE TABLE IF NOT EXISTS `yy_notice` (
  `id` varchar(32) NOT NULL COMMENT 'ID',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `state` int(11) DEFAULT '0' COMMENT '状态：0启用 1禁用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试公告';