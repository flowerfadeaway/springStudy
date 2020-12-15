package com.example.demo.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
//@TableName("user")
public class User {
    @TableId(type = IdType.ID_WORKER)
    private Long id;
//    @TableField(condition = SqlCondition.LIKE)
    private String name;
    private Integer age;
    private String email;

    @Version//代表这是一个乐观锁
    @TableField(fill = FieldFill.INSERT)
    private int version;
    //添加填充内容
    @TableField(fill = FieldFill.INSERT)
//    @TableField(value = "create_time",fill = FieldFill.INSERT)
//    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic//逻辑删除
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}