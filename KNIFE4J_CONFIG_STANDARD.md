# Knife4j配置统一标准

## 概述
本文档定义了XReadUp AI项目中所有微服务Knife4j配置的统一标准。

## 统一配置标准

### 1. 依赖配置
所有微服务统一使用以下依赖：
```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
</dependency>
```

### 2. 配置类标准
所有微服务统一使用以下配置类格式：

#### Java配置类 (Knife4jConfig.java)
```java
package com.xreadup.ai.[模块名].config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j配置类
 * 统一配置Swagger接口文档格式
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("[模块名称]服务API")
                        .version("1.0.0")
                        .description("[模块名称]服务RESTful API文档")
                        .contact(new Contact()
                                .name("XReadUp Team")
                                .email("support@xreadup.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

### 3. application.yml配置标准

#### springdoc配置
```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  group-configs:
    - group: 'default'
      paths-to-match: '/**'
      packages-to-scan: com.xreadup.ai.[模块名].controller
```

#### knife4j增强配置
```yaml
knife4j:
  enable: true
  openapi:
    title: [模块名称]服务API文档
    description: [模块名称]服务RESTful API接口文档
    version: 1.0.0
    group:
      default:
        group-name: [模块名称]服务
        api-rule: package
        api-rule-resources:
          - com.xreadup.ai.[模块名].controller
```

## 各模块配置检查表

| 模块 | 状态 | 配置类 | application.yml | 备注 |
|------|------|--------|----------------|------|
| ai-service | ✅ | 已统一 | 已统一 | - |
| article-service | ✅ | 已统一 | 已统一 | - |
| report-service | ✅ | 已统一 | 已统一 | - |
| user-service | ✅ | 已统一 | 已统一 | - |
| gateway | ❌ | 不需要 | 不需要 | 网关不需要Knife4j |

## 验证方法

1. **启动验证**：每个服务启动后访问 `http://localhost:端口/swagger-ui.html`
2. **接口验证**：确认所有接口都能正常显示
3. **分组验证**：确认API分组显示正确
4. **文档验证**：确认文档描述信息正确

## 访问地址

- AI服务: http://localhost:8084/swagger-ui.html
- 文章服务: http://localhost:8082/swagger-ui.html
- 报告服务: http://localhost:8083/swagger-ui.html
- 用户服务: http://localhost:8081/swagger-ui.html

## 注意事项

1. **包名规范**：所有模块必须使用 `com.xreadup.ai.[模块名]` 的包名结构
2. **控制器路径**：控制器必须放在 `com.xreadup.ai.[模块名].controller` 包下
3. **版本统一**：所有服务版本号统一为 `1.0.0`
4. **依赖检查**：确保所有服务都包含 `spring-boot-starter-validation` 依赖

## 后续维护

新增微服务时，请严格按照此标准进行配置，确保整个系统的配置一致性。