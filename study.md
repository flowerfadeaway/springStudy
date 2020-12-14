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
    password: 730707
    # Mac电脑需要配置：useSSL=false
    # MySQL8中时区配置：serverTimezone=UTC
    # 编码格式配置：useUnicode=true&characterEncoding=utf-8
    url: jdbc:mysql://localhost:3306/mpStudy?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
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

```
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

2. 数据库ID字段一定要是自增的(数据库中操作)

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

### 更新操作（Update）

```
//测试更新
    @Test
    public  void testUpdate(){
        User user = new User();
        user.setId(1L);
        user.setAge(99);
        user.setName("叶仁平");

        // 注意：updateById的参数是一个 对象！
        int i = userMapper.updateById(user);
        System.out.println(i);

    }
```

### 查询操作（Select）
    
1. 单个用户

```$xslt
//测试测试 -单个用户
    @Test
    public void testSelect(){
        User user = userMapper.selectById(1l);
        System.out.println(user);

    }
```

2. 多个用户

```
//查询多个用户
@Test
public void testSelect2(){
    List<User> users = userMapper.selectBatchIds(Arrays.asList(1, 2, 3));
    users.forEach(System.out::println);

}
```

3. 条件查询-map

```$xslt
//条件查询 -map
    @Test
    public void testSelcts(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","贺晶晶");
        //自定义要查询的条件
        List<User> users = userMapper.selectByMap(map);
        users.forEach(System.out::println);
    }
```

### 自动填充

创建时间、修改时间，这些操作都希望是自动完成的， 我们不再希望手动去更新它们！

阿里巴巴开发手册：所有的表：gmt_carete、gmt_modified、像这两个字段一般所有的表都必须配置上！而且都是自动化的！

1. 数据库级别

在表中新增字段`create_time`,`update_time`

```mysql
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(30) DEFAULT NULL COMMENT '姓名',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' ,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
```

2. 代码级别

* 实体类User字段上需要增加注解操作
```java
@Data
public class User {
    @TableId(type = IdType.ID_WORKER)
    private Long id;
    private String name;
    private Integer age;
    private String email;
    //添加填充内容
    @TableField(fill = FieldFill.INSERT)
    private Data createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Data updateTime;
}
```
* 编写处理器`MyDataObjectHandler.java`处理注解
```java
@Component//一定不要忘记把处理器组件添加到IOC容器中！（Component）
@Slf4j
public class MyDataObjectHandler implements MetaObjectHandler{
    //插入时候的填充策略
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入执行.......");
        this.setFieldValByName("createTime",new Date(),metaObject);
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }

    //更新时候的填充策略
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新执行.......");
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }
}
```
* 测试插入，测试更新，观察数据即可

### 乐观锁

说白了：所有的记录加一个version

乐观锁实现方式：

1. 取出记录时，获取当前version
2. 更新时，带上这个version
3. 执行更新时， `set version = newVersion where version = oldVersion`
4. 如果version不对，就更新失败

#### mabatis-plus乐观锁配置

* 给表添加一个字段，默认值设为1
* 实体类添加对应的字段
```java
@Data
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
}
```
* 注册组件,在config包下，创建MybatisPlusConfig.java，修改如下
```java
/*扫描mapper文件夹*/
@MapperScan("com.ye.mapper")

@EnableTransactionManagement //事务管理

@Configuration //配置类
public class MybatisPlusConfig {
    //插件配置springboot项目下
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

}
```
* 测试乐观锁
```
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
```

### 分页查询

以往

1. 原始的limit进行分页
2. pageHelper第三方插件

MP内置的分页插件-如何使用？

* 配置拦截器组件即可,在MybatisPlusConfig.java中添加如下代码
```
//分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
           return new PaginationInterceptor();
    }
```
* 直接使用page对象即可！

### 删除操作

1. 基本的删除操作
```
//测试删除
    @Test
    public void testDelete(){

        userMapper.deleteById(1L);
    }

    @Test
    //批量删除
    public void testDeleteBatchId(){
        userMapper.deleteBatchIds(Arrays.asList(1L,2L,3L));
    }

    @Test
    //通过map删除
    public void testDleleteByMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","等不到天黑");
        userMapper.deleteByMap(map);
    }
```
2. 逻辑删除,在数据库并没有真正的删除，而是通过一个变量让它失效delete = 0 => delete>=1
3. pojo实体类中添加属性+注解`@TableLogic`
4. 配置，在Application.yml中添加如下数据
```yaml
#配置logic删除
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: flag  # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
```
5. 测试删除
```
//测试删除
@Test
public void testDelete(){

    userMapper.deleteById(1310147365178114052L);
}
```
### 性能分析插件
由于在在我们的平时开发中，会遇到一些慢sql，测试！durd...操作,mybatis-plus提供性能分析插件，如果超过这个时间就停止运行！    

* 导入插件
在MybatisPlusConfig.java中配置如下，在springboot中配置环境为dev或test
```$xslt
/**
     * sql执行效率插件
     */
    @Bean
    @Profile({"dev","test"}) //设置dev环境和test环境开启
    public  PerformanceInterceptor performanceInterceptor(){
        PerformanceInterceptor pi = new PerformanceInterceptor();
        pi.setMaxTime(100); //设置sql能够执行最大时间，如果超过了则不执行
        pi.setFormat(true);//是否格式化
        return new PerformanceInterceptor();
    }
```
* 测试使用,只要超过了设置的时间，就会抛出异常！

### 条件构造器(Wrapper)

[官网文档](https://link.zhihu.com/?target=https%3A//baomidou.com/guide/wrapper.html%23abstractwrapper)

#### 分页查询

```
// 条件查询
LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(UserInfo::getAge, 20);
// 分页对象
Page<UserInfo> queryPage = new Page<>(page, limit);
// 分页查询
IPage<UserInfo> iPage = userInfoMapper.selectPage(queryPage , queryWrapper);
// 数据总数
Long total = iPage.getTotal();
// 集合数据
List<UserInfo> list = iPage.getRecords();
```

#### 分页查询（联表）

假设我们需要的 SQL 语句如下：

```sql
SELECT
	a.*,
	b.`name` AS sex_text 
FROM
	user_info a
	LEFT JOIN user_sex b ON ( a.sex = b.id ) 
WHERE
	1 = 1 
	AND a.age > 20
```

那么我们需要进行如下操作：

1. 新建 `UserInfoVO.java`
```java
@Data
public class UserInfoVO extends UserInfo {

    // 性别
    private String sexText;
}
```

2. `UserInfoMapper.java` 中
```
IPage<UserInfoVO> list(Page<UserInfoVO> page, @Param(Constants.WRAPPER) Wrapper<UserInfoVO> queryWrapper);
```

3. `UserInfoMapper.xml` 中
```$xslt
<select id="list" resultType="com.zyxx.vo.UserInfoVO">
    SELECT
		a.*,
		b.`name` AS sex_text 
	FROM
		user_info a
		LEFT JOIN user_sex b ON ( a.sex = b.id ) 
    ${ew.customSqlSegment}
</select>
```
4. `UserInfoServiceImpl.java` 中
```$xslt
// 条件查询
LambdaQueryWrapper<UserInfoVO> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(UserInfo::getAge, 20);
// 分页对象
Page<UserInfoVO> queryPage = new Page<>(page, limit);
// 分页查询
IPage<UserInfoVO> iPage = userInfoMapper.list(queryPage , queryWrapper);
// 数据总数
Long total = iPage.getTotal();
// 用户数据
List<UserInfoVO> list = iPage.getRecords();
```

#### AND 和 OR

1. 初级

假设我们需要的 SQL 语句如下：

```sql
SELECT
	a.* 
FROM
	user_info a 
WHERE
	1 = 1 
	AND a.id <> 1 
	AND ( a.`name` = 'jack' OR a.phone = '13888888888' )
```

那么我们可以这样写：

```$xslt
LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
// AND a.id <> 1 
queryWrapper.ne(UserInfo::getId, "1");
// AND ( a.`name` = 'jack' OR a.phone = '13888888888' )
queryWrapper.and(i -> i.eq(UserInfo::getName, "jack").or().eq(UserInfo::getPhone, "13888888888"));
// 查询结果
List<UserInfo> list = userInfoMapper.selectList(queryWrapper);
```

2. 复杂

假设我们需要的 SQL 语句如下：

```sql
SELECT
	a.* 
FROM
	user_info a 
WHERE
	1 = 1 
	AND a.id <> 1 
	AND ( (a.`name` = 'jack' AND a.category = 1) OR (a.phone = '13888888888' OR a.category = 2) )
```

那么我们可以这样写：

```$xslt
LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
// AND a.id <> 1 
queryWrapper.ne(UserInfo::getId, "1");
// AND ( (a.`name` = 'jack' AND a.category = 1) OR (a.phone = '13888888888' OR a.category = 2) )
queryWrapper.and(i -> (i.and(j -> j.eq(UserInfo::getName, "jack").eq(UserInfo::getCategory, 1))).or(j -> j.eq(UserInfo::getPhone, "13888888888").eq(UserInfo::getCategory, 2)));
// 查询结果
List<UserInfo> list = userInfoMapper.selectList(queryWrapper);
```

#### 指定查询字段（select）

我们只需要查询年龄等于20的用户的 id、name、phone，所以，可以这样写

```$xslt
LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
// 只查询 id，name，phone
queryWrapper.select(UserInfo::getId, UserInfo::getName, UserInfo::getPhone);
// 查询条件为：age = 20
queryWrapper.eq(UserInfo::getAge, 20);
List<UserInfo> list = userInfoMapper.selectList(queryWrapper );
```

#### 查询一条数据（getOne）

存在一定的风险，也就是说如果表中存在同一个 openId 的两条及两条以上的数据时，会抛出异常，所以我们需要这样：

```$xslt
LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(UserInfo::getOpenId, openId);
UserInfo userInfo = userInfoService.getOne(queryWrapper, false);
```

可以看出，传入了一个 false 参数，该参数的含义为：有多个 result 是否抛出异常,不抛出异常，则会返回第一个对象



### 代码自动生成器

AutoGenerator 是 MyBatis-Plus 的代码生成器，通过 AutoGenerator 可以快速生成 Entity、Mapper、Mapper XML、Service、Controller 等各个模块的代码，极大的提升了开发效率。

实现步骤

1. 导入mybatis-plus到配置到pom.xml中
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

        <!-- 4、模板引擎 -->
        <dependency>
            <groupId>org.apache.velocity</groupId>
            <artifactId>velocity-engine-core</artifactId>
            <version>2.0</version>
        </dependency>

        <!--5、swagger配置-->
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.9.2</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.8.0</version>
        </dependency>
```
2. 配置yml文件
```yaml
# 1、数据源
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://47.104.231.144:3306/mybatis_plus?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.cj.jdbc.Driver
  profiles:
    active: dev # 2、设置开发环境


# 3、配置日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  # 4、配置逻辑删除
  global-config:
    db-config:
      logic-delete-field: flag  # 全局逻辑删除的实体字段名(since 3.3.0,配置后可以忽略不配置步骤2)
      logic-delete-value: 1 # 逻辑已删除值(默认为 1)
      logic-not-delete-value: 0 # 逻辑未删除值(默认为 0)
# 5、配置服务端口
server:
  port:8081
```
3. 创建配置文件MybatisPlusConfig.java
```java
/*扫描mapper文件夹*/
@MapperScan("com.ye.mapper")

@EnableTransactionManagement //事务管理

@Configuration //配置类
public class MybatisPlusConfig {

    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }


    //分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        return new PaginationInterceptor();
    }

    //逻辑删除组件
    @Bean
    public ISqlInjector sqlInjector(){
        return new LogicSqlInjector();
    }


    //sql执行效率插件
    @Bean
    @Profile({"dev","test"}) //设置dev环境和test环境开启
    public  PerformanceInterceptor performanceInterceptor(){
        PerformanceInterceptor pi = new PerformanceInterceptor();
        pi.setMaxTime(100); //设置sql能够执行最大时间，如果超过了则不执行
        pi.setFormat(true);
        return new PerformanceInterceptor();
    }
}
```
4. 编写代码自动生成器YeCode.java
```java
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.ArrayList;

// 代码自动生成器
public class YeCode{
    public static void main(String[] args) {
        // 构建一个代码自动生成器对象
        AutoGenerator mg =  new AutoGenerator();

        // 1、全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath+"/src/main/java");
        gc.setAuthor("是叶十三");
        gc.setOpen(false);//是否打开文件
        gc.setFileOverride(false);// 是否覆盖
        gc.setServiceName("%sService");//去Service的I前缀
        gc.setIdType(IdType.ID_WORKER); //id全局唯一
        gc.setDateType(DateType.ONLY_DATE);// 日期类型
        gc.setSwagger2(true);

        mg.setGlobalConfig(gc);//将配置丢到自动生成器里面

        //2、设置数据源
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://47.104.231.144:3306/mybatis_plus_code?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        dsc.setDbType(DbType.MYSQL);
        mg.setDataSource(dsc);


        //3、包的配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("blog");
        pc.setParent("com.ye");
        pc.setEntity("pojo");
        pc.setMapper("mapper");
        pc.setService("servie");
        pc.setController("controller");
        mg.setPackageInfo(pc);


        //3、策略配置
        StrategyConfig sc = new StrategyConfig();
        sc.setInclude("t_blog","t_blog_tags","t_comment","t_tag","t_tag_copy");
        sc.setNaming(NamingStrategy.underline_to_camel); //下滑线转驼峰命名
        sc.setColumnNaming(NamingStrategy.underline_to_camel);//列-下滑线转驼峰命名
//        strategy.setSuperEntityClass("你自己的父类实体,没有就不用设置!");
        sc.setEntityLombokModel(true); //自动生成lombok
        sc.setLogicDeleteFieldName("deleted"); //逻辑删除配置
        sc.setRestControllerStyle(true);

        // 自动填充配置
        // 自动填充配置
        TableFill create_time = new TableFill("create_time", FieldFill.INSERT);
        TableFill update_time = new TableFill("update_time", FieldFill.INSERT_UPDATE);
        ArrayList<TableFill> tableFills = new ArrayList<>();
        tableFills.add(create_time);
        tableFills.add(update_time);
        sc.setTableFillList(tableFills);


        // 乐观锁配置：
        sc.setVersionFieldName("version");


        //开启restfull的驼峰命名
        sc.setRestControllerStyle(true);

        // url地址变为下划线
        /**
         *
         * localhost:8080/hello_id_23
         */
        sc.setControllerMappingHyphenStyle(true);
        mg.setStrategy(sc);

        //执行
        mg.execute();
    }
}
```