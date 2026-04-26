-- 1. 添加 user_id 列（先允许 NULL，便于处理已有数据）
ALTER TABLE documents ADD COLUMN user_id BIGINT NULL AFTER id;

-- 2. 删除无主文档（开发阶段的测试数据，无法回填 user_id）
DELETE FROM documents WHERE user_id IS NULL;

-- 3. 改为 NOT NULL
ALTER TABLE documents MODIFY COLUMN user_id BIGINT NOT NULL;

-- 4. 添加外键约束
ALTER TABLE documents ADD CONSTRAINT fk_documents_user
    FOREIGN KEY (user_id) REFERENCES users (id);

-- 5. 添加索引（按用户查文档是高频查询）
ALTER TABLE documents ADD INDEX idx_documents_user_id (user_id);
