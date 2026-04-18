package com.jsamuelsen11.daykeeper.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManager
import com.jsamuelsen11.daykeeper.core.data.attachment.AttachmentManagerImpl
import com.jsamuelsen11.daykeeper.core.data.attachment.FileCache
import com.jsamuelsen11.daykeeper.core.data.attachment.ImageCompressor
import com.jsamuelsen11.daykeeper.core.data.attachment.ImageCompressorImpl
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationChannelManager
import com.jsamuelsen11.daykeeper.core.data.notification.NotificationDisplayManager
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderScheduler
import com.jsamuelsen11.daykeeper.core.data.notification.ReminderSchedulerImpl
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepository
import com.jsamuelsen11.daykeeper.core.data.preferences.UserPreferencesRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AccountRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AddressRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepository
import com.jsamuelsen11.daykeeper.core.data.repository.AttachmentRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepository
import com.jsamuelsen11.daykeeper.core.data.repository.CalendarRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ContactMethodRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.DeviceRepository
import com.jsamuelsen11.daykeeper.core.data.repository.DeviceRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventReminderRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepository
import com.jsamuelsen11.daykeeper.core.data.repository.EventTypeRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ImportantDateRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepository
import com.jsamuelsen11.daykeeper.core.data.repository.PersonRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ProjectRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListItemRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepository
import com.jsamuelsen11.daykeeper.core.data.repository.ShoppingListRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceMemberRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceMemberRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SpaceRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.SyncCursorRepository
import com.jsamuelsen11.daykeeper.core.data.repository.SyncCursorRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskCategoryRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepository
import com.jsamuelsen11.daykeeper.core.data.repository.TaskRepositoryImpl
import com.jsamuelsen11.daykeeper.core.data.session.CurrentSessionProvider
import com.jsamuelsen11.daykeeper.core.data.session.DefaultSessionProvider
import com.jsamuelsen11.daykeeper.core.data.sync.SyncManager
import com.jsamuelsen11.daykeeper.core.data.sync.SyncStatusProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

private val Context.userPreferencesDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "user_preferences")

public val dataModule = module {
  single<DataStore<Preferences>> { androidContext().userPreferencesDataStore }
  single { UserPreferencesRepositoryImpl(get()) } bind UserPreferencesRepository::class

  single { AccountRepositoryImpl(get()) } bind AccountRepository::class
  single { DeviceRepositoryImpl(get()) } bind DeviceRepository::class
  single { SpaceRepositoryImpl(get()) } bind SpaceRepository::class
  single { SpaceMemberRepositoryImpl(get()) } bind SpaceMemberRepository::class
  single { CalendarRepositoryImpl(get()) } bind CalendarRepository::class
  single { EventTypeRepositoryImpl(get()) } bind EventTypeRepository::class
  single { EventRepositoryImpl(get()) } bind EventRepository::class
  single { EventReminderRepositoryImpl(get()) } bind EventReminderRepository::class
  single { PersonRepositoryImpl(get()) } bind PersonRepository::class
  single { ContactMethodRepositoryImpl(get()) } bind ContactMethodRepository::class
  single { AddressRepositoryImpl(get()) } bind AddressRepository::class
  single { ImportantDateRepositoryImpl(get()) } bind ImportantDateRepository::class
  single { ProjectRepositoryImpl(get()) } bind ProjectRepository::class
  single { TaskCategoryRepositoryImpl(get()) } bind TaskCategoryRepository::class
  single { TaskRepositoryImpl(get()) } bind TaskRepository::class
  single { ShoppingListRepositoryImpl(get()) } bind ShoppingListRepository::class
  single { ShoppingListItemRepositoryImpl(get()) } bind ShoppingListItemRepository::class
  single { AttachmentRepositoryImpl(get()) } bind AttachmentRepository::class
  single { SyncCursorRepositoryImpl(get()) } bind SyncCursorRepository::class

  single { FileCache(File(androidContext().filesDir, "attachment_cache")) }
  single { ImageCompressorImpl() } bind ImageCompressor::class
  single { AttachmentManagerImpl(get(), get(), get()) } bind AttachmentManager::class

  single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
  single { DefaultSessionProvider() } bind CurrentSessionProvider::class
  single { SyncManager(get(), get(), get(), get()) } bind SyncStatusProvider::class

  single { NotificationChannelManager(androidContext()) }
  single { NotificationDisplayManager(androidContext()) }
  single { ReminderSchedulerImpl(androidContext(), get(), get(), get()) } bind
    ReminderScheduler::class
}
