package com.whyrano

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WhyranoApplication

fun main(args: Array<String>) {
	runApplication<WhyranoApplication>(*args)
}
