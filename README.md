## 牛客高薪求职课项目-完整版实现
### 涉及技术
 - Spring MVC 
 - Spring Boot 
 - Spring Security
 - MyBatis
 - MySQL
 - Redis
 - Kafka
 - Elasticsearch
 - Quartz
 - Caffeine
 - 。。。

### pull下来后如何运行：
 1. 替换配置文件中以下配置
    - 目录
    - application-produce.properties
    - application-develop.properties
    
```
# 邮箱配置
spring.mail.username=
spring.mail.password=

# qiniu CDN自定义
qiniu.key.access=
qiniu.key.secret=
```
 2. 安装软件
    - 对应pom.xml版本
    1. MySQL8.x
    2. JDK11+
    3. Kafka
    4. Elasticsearch 
    5. Maven
    6. apache-jmeter
    7. wkhtmltopdf