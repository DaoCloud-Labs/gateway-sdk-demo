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
