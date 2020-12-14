package com.example.demo;


import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MybatisPlusApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
    }

    //测试插入
    @Test
    public void testInsert(){
        User user = new User();
        user.setName("贺晶晶");
        user.setAge(18);
        user.setEmail("567890qq.com");

        int index = userMapper.insert(user); //自动生成ID
        System.out.println(index);// 受影响的行数
        System.out.println(user); // 结果显示ID会自动回填

    }

}
