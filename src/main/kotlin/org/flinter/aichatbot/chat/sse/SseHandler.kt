package org.flinter.aichatbot.chat.sse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CompletableFuture

@Component
class SseHandler {

    fun stream(producer: (onChunk: (String) -> Unit, onComplete: () -> Unit) -> Unit): SseEmitter {
        val emitter = SseEmitter(120_000L)
        CompletableFuture.runAsync {
            runCatching {
                producer(
                    { chunk -> emitter.send(SseEmitter.event().data(chunk)) },
                    { emitter.complete() },
                )
            }.onFailure { emitter.completeWithError(it) }
        }
        return emitter
    }
}
