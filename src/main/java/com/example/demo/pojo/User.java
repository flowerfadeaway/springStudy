package com.example.demo.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
//@TableName("user")
public class User {
    @TableId(type = IdType.ID_WORKER)
    private Long id;
    private String name;
    private Integer age;
    private String email;

    @Version//代表这是一个乐观锁
    private int version;
    //添加填充内容
    @TableField(fill = FieldFill.INSERT)
    private Data createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Data updateTime;

    @TableLogic//逻辑删除
    private Integer deleted;
}