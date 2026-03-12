package com.jsamuelsen11.daykeeper.core.model.people

import com.jsamuelsen11.daykeeper.core.model.DayKeeperModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class AddressTest {

  private val address =
    Address(
      addressId = "addr-1",
      personId = "person-1",
      label = "Home",
      createdAt = 1_000L,
      updatedAt = 2_000L,
    )

  @Test
  fun `implements DayKeeperModel`() {
    address.shouldBeInstanceOf<DayKeeperModel>()
  }

  @Test
  fun `all address fields default to null`() {
    address.street shouldBe null
    address.city shouldBe null
    address.state shouldBe null
    address.postalCode shouldBe null
    address.country shouldBe null
    address.deletedAt shouldBe null
  }
}
