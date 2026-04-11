package com.jsamuelsen11.daykeeper.feature.profile.device

import app.cash.turbine.test
import com.jsamuelsen11.daykeeper.core.data.repository.DeviceRepository
import com.jsamuelsen11.daykeeper.feature.profile.MainDispatcherExtension
import com.jsamuelsen11.daykeeper.feature.profile.TEST_DEVICE_ID
import com.jsamuelsen11.daykeeper.feature.profile.TEST_DEVICE_ID_2
import com.jsamuelsen11.daykeeper.feature.profile.TEST_TENANT_ID
import com.jsamuelsen11.daykeeper.feature.profile.makeDevice
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MainDispatcherExtension::class)
class DeviceManagementViewModelTest {

  private val deviceRepository = mockk<DeviceRepository>()

  private fun createViewModel(): DeviceManagementViewModel =
    DeviceManagementViewModel(deviceRepository = deviceRepository)

  @Test
  fun `empty devices emits Success with empty list`() = runTest {
    every { deviceRepository.observeByTenant(TEST_TENANT_ID) } returns flowOf(emptyList())

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<DeviceManagementUiState.Success>()
      state.devices shouldBe emptyList()
    }
  }

  @Test
  fun `devices are listed with sync time formatted`() = runTest {
    val device1 = makeDevice(deviceId = TEST_DEVICE_ID, deviceName = "Pixel 8")
    val device2 =
      makeDevice(
        deviceId = TEST_DEVICE_ID_2,
        deviceName = "Galaxy S24",
        lastSyncCursor = 1_700_000_000_000L,
      )

    every { deviceRepository.observeByTenant(TEST_TENANT_ID) } returns
      flowOf(listOf(device1, device2))

    val viewModel = createViewModel()

    viewModel.uiState.test {
      val state = awaitItem()
      state.shouldBeInstanceOf<DeviceManagementUiState.Success>()
      state.devices.size shouldBe 2
      state.devices.first { it.device.deviceId == TEST_DEVICE_ID }.lastSyncFormatted shouldBe
        "Never"
    }
  }

  @Test
  fun `removeDevice calls repository delete`() = runTest {
    every { deviceRepository.observeByTenant(TEST_TENANT_ID) } returns flowOf(emptyList())
    coEvery { deviceRepository.delete(TEST_DEVICE_ID) } just runs

    val viewModel = createViewModel()
    viewModel.removeDevice(TEST_DEVICE_ID)

    coVerify { deviceRepository.delete(TEST_DEVICE_ID) }
  }
}
