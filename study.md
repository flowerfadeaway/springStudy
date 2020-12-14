## 在pom.xml中导入相关依赖

```xml
        <!--1、数据库驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

        <!--2、lombok插件引入-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.16.18</version>
        </dependency>

        <!--3、引入mybatis_plus-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>
```

<font color='red'>说明：我们使用mybatis-plus可以节省大量代码，尽量不要同时导入mybati和mybatis-plus的依赖！存在版本差异</font>

## application.yml中配置数据源

```yaml
# 数据源
spring:
  datasource:
    username: root
    password: 123456
    # Mac电脑需要配置：useSSL=false
    # MySQL8中时区配置：serverTimezone=UTC
    # 编码格式配置：useUnicode=true&characterEncoding=utf-8
    url: jdbc:mysql://47.104.231.144:3306/mybatis_plus?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
    # MySQL5.x 驱动：com.mysql.jdbc.Driver
    # MySQL8 驱动：com.mysql.cj.jdbc.Driver
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 配置日志
```yaml
# 配置日志
# 我们所有的sql现在是控制台可见的
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 创建pojo实体

```java
@Data
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
```

## 创建mapper，持续化接口

```java
// 在对应的Mapper上面继承接口BaseMapper
@Repository //代表的是持久层的
public interface UserMapper extends BaseMapper<User> {
    //所有的crud操作已经编写完成
    //不需要像以前一样配置一大堆文件了！
}
```

## 在测试类中测试功能

```java
@SpringBootTest
class MybatisPlusApplicationTests {

    // 因为我们继承了BaseMapper，所有的方法都来自父类
    // 我们也可以扩展自己的方法
    @Autowired
    private UserMapper userMapper;



    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
    }

}
```

## CRUD扩展

### 插入操作（Insert）

```java
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
```

#### 主键生成
主键生成策略:`默认==ID_WORKER==全局唯一ID`,分布式系统唯一id生成方案汇总：[分布式系统唯一id生成方案](https://link.zhihu.com/?target=https%3A//www.cnblogs.com/haoxinyue/p/5208136.html)

==雪花算法==

snowflake是Twitter开源的分布式ID生成算法，结果是一个long型的ID。其核心思想是：使用41bit作为毫秒数，10bit作为机器的ID（5个bit是数据中心，5个bit的机器ID），12bit作为毫秒内的流水号（意味着每个节点在每毫秒可以产生 4096 个 ID），最后还有一个符号位，永远是0。可以保证几乎全球唯一！

#### 主键自增

我们需要配置主键自增

1. 在实体类的ID字段上增加`@TableId(type = IdType.ID_WORKER)`注解

2. 数据库ID字段一定要是自增的

#### 源码解释

```java
public enum IdType {
    AUTO(0), //数据库ID自增
    NONE(1), //未设置主键
    INPUT(2),// 手动输入，一旦手动输入后需要自己设置ID
    ID_WORKER(3),//全局默认唯一id
    UUID(4),// 全局唯一id uuid
    ID_WORKER_STR(5);//ID_WORER的字符串表示法

    private int key;

    private IdType(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
```
