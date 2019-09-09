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