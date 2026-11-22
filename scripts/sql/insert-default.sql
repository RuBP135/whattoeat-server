INSERT INTO stall_category (name) VALUES
    ('米饭套餐'),
    ('面食粉类'),
    ('冒菜火锅'),
    ('炒菜小炒'),
    ('炸鸡汉堡与小吃'),
    ('轻食沙拉'),
    ('饮品甜点'),
    ('早餐粥点'),
    ('砂锅煲类与蒸菜'),
    ('水果与便利')
ON DUPLICATE KEY UPDATE name = stall_category.name;
