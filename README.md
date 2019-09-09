# 网关脚本编写DEMO

创建一个Maven项目，如下POM
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>aaa.bbb.ccc.script</groupId>
    <artifactId>script</artifactId>
    <version>1.0-SNAPSHOT</version>
 
 
    <build>
        <finalName>${project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.6.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.gmaven</groupId>
                <artifactId>groovy-maven-plugin</artifactId>
                <!-- add groovy build support -->
                <dependencies>
                    <dependency>
                        <groupId>org.codehaus.groovy</groupId>
                        <artifactId>groovy-all</artifactId>
                        <version>2.4.15</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
 
 
    <dependencies>
        <dependency>
            <groupId>io.daocloud.mircoservice</groupId>
            <artifactId>gateway-sdk</artifactId>
            <version>2.0.7</version>
        </dependency>
 
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-all</artifactId>
            <version>2.4.15</version>
        </dependency>
 
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
 
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <version>3.2.3.RELEASE</version>
            <scope>test</scope>
 
        </dependency>
 
    </dependencies>
 
</project>
```


创建自定义的实现类
```groovy
package aaa.bbb.ccc.script
 
import io.daocloud.mircoservice.gateway.core.context.script.*
import reactor.core.publisher.Mono
 
class CustomScript implements ScriptExecute {
 
    @Override
    Mono<ScriptOutput> execute(ScriptContext scriptContext) {
 
        return scriptContext.webClient()
                .post()
                .uri("http://user-center/check-token")
                .header("x-token", scriptContext.headers().getFirst("x-token"))
                .exchange()
                .flatMap({ cr ->
                    if (cr.statusCode().is2xxSuccessful()) {
                        return Mono.just(DefaultHttpScriptOutput.builder()
                                .headers(scriptContext.headers())
                                .body(scriptContext.body())
                                .method(scriptContext.method())
                                .queryParams(scriptContext.queryParam())
                                .build())
                    }
 
                    throw new ScriptRuntimeException(500, "user token not exist")
                })
    }
}
```

进行测试
```java
package aaa.bbb.ccc.script;
 
 
import io.daocloud.mircoservice.gateway.core.context.script.test.MockScriptContext;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import reactor.test.StepVerifier;
 
public class CustomScriptTest {
 
    @Test
    public void test_custom_script() {
        final MockScriptContext context = MockScriptContext.newBuilder()
                .addHeader("x-token", "1234")
                .body("{}")
                .method(HttpMethod.GET)
                .build();
 
        StepVerifier.create(new CustomScript().execute(context))
                .expectNextMatches(scriptOutput ->
                        scriptOutput.headers().containsKey("x-token"))
                .verifyComplete();
 
    }
}
```
