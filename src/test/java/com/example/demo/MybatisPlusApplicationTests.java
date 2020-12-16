package com.example.demo;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class MybatisPlusApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserService userService;

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


    @Test
    public void test1(){
        List<User> j = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getName, "J"));
        j.forEach(System.out::println);
    }


    @Test
    public void test2(){
        User user = new User();
//        user.setId(5l);
        user.setAge(111);
        user.setEmail("123.com");
        user.setName("西门吹雪");
        user.setId(1338724107558473729l);
        System.out.println(user);

        userService.saveOrUpdate(user);
        System.out.println(user);
        System.out.println("这是delete："+user.getDeleted());
        System.out.println(new Date());
    }

    // 测试一下！（成功 案例）

    @Test
    public void testOptimisticLocker(){
        // 1、查询用户信息
        User user = userMapper.selectById(1L);

        // 2、修改用户信息
        user.setName("地理热巴");
        user.setAge(38);
        // 3、执行更新操作
        userMapper.updateById(user);

    }

    // 测试一下！ （失败 案例） -------多线程情况下
    @Test
    public void testOptimisticLocker2(){
        //线程1
        User user = userMapper.selectById(1L);
        user.setName("地理热巴11111111111111");
        user.setAge(38);

        //模拟另一个线程执行了插队操作

        User user2 = userMapper.selectById(1L);
        user2.setName("地理热巴2222222222222222222");
        user2.setAge(38);
        userMapper.updateById(user2);


        userMapper.updateById(user); //如果没有乐观锁就会覆插队线程的值！

    }
}
