package com.jsamuelsen11.daykeeper.app

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ExampleUnitTest {

  interface Greeter {
    suspend fun greet(name: String): String
  }

  @Test
  fun `kotest assertions work`() {
    val result = 2 + 2
    result shouldBe 4
    result shouldNotBe 5
  }

  @Test
  fun `mockk mocking and verification work`() = runTest {
    val greeter = mockk<Greeter>()
    coEvery { greeter.greet("World") } returns "Hello, World!"

    val greeting = greeter.greet("World")

    greeting shouldBe "Hello, World!"
    coVerify(exactly = 1) { greeter.greet("World") }
  }

  @Test
  fun `turbine flow testing works`() = runTest {
    val flow = flowOf("alpha", "beta", "gamma")

    flow.test {
      awaitItem() shouldBe "alpha"
      awaitItem() shouldBe "beta"
      awaitItem() shouldBe "gamma"
      awaitComplete()
    }
  }
}
