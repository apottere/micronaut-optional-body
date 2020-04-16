package optional.body

import io.micronaut.context.annotation.Context
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.reactivex.Flowable
import io.reactivex.functions.Function
import java.util.Optional
import kotlin.system.exitProcess

@Context
@Controller(consumes = [MediaType.ALL], produces = [MediaType.ALL])
class TestController(@Client("http://localhost:8080") val client: RxHttpClient) {

	@EventListener
	fun test(event: ServerStartupEvent) {
		repeat(5) { num ->
			val endpoint = "/test${num + 1}"
			test(endpoint, "no body", HttpRequest.create<Nothing>(HttpMethod.POST, endpoint))
			test(endpoint, "   body", HttpRequest.create<String>(HttpMethod.POST, endpoint).body("test"))
		}

		exitProcess(0)
	}

	private fun test(endpoint: String, type: String, request: HttpRequest<*>) {
		client
			.exchange(request)
			.doOnNext { response ->
				println("$endpoint: $type: ${response.code()} ${response.getBody(String::class.java).get()}")
			}
			.onErrorResumeNext(Function { t ->
				println("$endpoint: $type: ${t.message}")
				Flowable.empty()
			})
			.blockingForEach {  }
	}

	@Post("/test1")
	fun post1(request: HttpRequest<Optional<ByteBuffer<*>>>) = request.body.isPresent

	@Post("/test2")
	fun post2(request: HttpRequest<ByteBuffer<*>?>) = request.body.isPresent

	@Post("/test3")
	fun post3(request: HttpRequest<*>, @Body body: Optional<ByteBuffer<*>>) = request.body.isPresent

	@Post("/test4")
	fun post4(request: HttpRequest<*>, @Body body: ByteBuffer<*>?) = request.body.isPresent

	@Post("/test5")
	fun post5(request: HttpRequest<ByteBuffer<*>?>, @Body body: ByteBuffer<*>?) = request.body.isPresent
}
