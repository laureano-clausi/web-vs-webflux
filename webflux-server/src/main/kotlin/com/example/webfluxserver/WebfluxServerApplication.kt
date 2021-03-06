package com.example.webfluxserver

import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.fromSupplier
import reactor.core.scheduler.Schedulers
import java.util.function.Supplier

@SpringBootApplication
class WebfluxServerApplication

fun main(args: Array<String>) {
	runApplication<WebfluxServerApplication>(*args)
}

@RestController
class ControllerNoBloqueante (private val webClient: WebClient = WebClient.create()){

	@GetMapping
	fun saludar() = webClient
		.get()
		.uri("http://localhost:9000")
		.exchangeToMono { it.bodyToMono(String::class.java) }
}

@RestController
class ControllerBloqueante (private val okHttp: OkHttpClient = OkHttpClient()){

	@GetMapping("/bloqueante")
	fun saludar(): String {
		val request: Request = Request.Builder()
			.url("http://localhost:9000")
			.build()

		return okHttp.newCall(request).execute().use { response -> response.body?.string()!! }
	}

	@GetMapping("/bloqueante-bounded-elastic")
	fun saludarFromSupplier(): Mono<String> {
		val request: Request = Request.Builder()
				.url("http://localhost:9000")
				.build()

		val scheduler = Schedulers.boundedElastic()


		return fromSupplier(Supplier { okHttp.newCall(request).execute().use { response -> response.body?.string()!! } })
				.subscribeOn(scheduler)

	}

	@GetMapping("/bloqueante-elastic")
	fun saludarSchedulerElastic(): Mono<String> {
		val request: Request = Request.Builder()
				.url("http://localhost:9000")
				.build()

		val scheduler = Schedulers.newElastic("elastic")


		return fromSupplier(Supplier { okHttp.newCall(request).execute().use { response -> response.body?.string()!! } })
				.subscribeOn(scheduler)

	}

}