package com.jsamuelsen11.daykeeper.core.database.entity.task

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jsamuelsen11.daykeeper.core.database.entity.account.AccountEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity

@Entity(
  tableName = "tasks",
  foreignKeys =
    [
      ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["project_id"],
        childColumns = ["project_id"],
        onDelete = ForeignKey.SET_NULL,
      ),
      ForeignKey(
        entity = SpaceEntity::class,
        parentColumns = ["space_id"],
        childColumns = ["space_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = TaskCategoryEntity::class,
        parentColumns = ["category_id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.SET_NULL,
      ),
    ],
  indices =
    [
      Index(value = ["space_id", "status", "due_at"]),
      Index(value = ["space_id", "status", "due_date"]),
      Index(value = ["space_id", "updated_at"]),
      Index(value = ["tenant_id"]),
      Index(value = ["project_id"]),
      Index(value = ["category_id"]),
    ],
)
public data class TaskEntity(
  @PrimaryKey @ColumnInfo(name = "task_id") val taskId: String,
  @ColumnInfo(name = "project_id") val projectId: String?,
  @ColumnInfo(name = "space_id") val spaceId: String,
  @ColumnInfo(name = "tenant_id") val tenantId: String,
  @ColumnInfo(name = "title") val title: String,
  @ColumnInfo(name = "description") val description: String?,
  @ColumnInfo(name = "status") val status: String,
  @ColumnInfo(name = "priority") val priority: String,
  @ColumnInfo(name = "due_at") val dueAt: Long?,
  @ColumnInfo(name = "due_date") val dueDate: String?,
  @ColumnInfo(name = "recurrence_rule") val recurrenceRule: String?,
  @ColumnInfo(name = "category_id") val categoryId: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long,
  @ColumnInfo(name = "updated_at") val updatedAt: Long,
  @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
