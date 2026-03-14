package com.jsamuelsen11.daykeeper.core.data.mapper

import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceEntity
import com.jsamuelsen11.daykeeper.core.database.entity.space.SpaceMemberEntity
import com.jsamuelsen11.daykeeper.core.model.space.Space
import com.jsamuelsen11.daykeeper.core.model.space.SpaceMember
import com.jsamuelsen11.daykeeper.core.model.space.SpaceRole
import com.jsamuelsen11.daykeeper.core.model.space.SpaceType

public fun SpaceEntity.toDomain(): Space =
  Space(
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    type = SpaceType.valueOf(type),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun Space.toEntity(): SpaceEntity =
  SpaceEntity(
    spaceId = spaceId,
    tenantId = tenantId,
    name = name,
    normalizedName = normalizedName,
    type = type.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun SpaceMemberEntity.toDomain(): SpaceMember =
  SpaceMember(
    spaceId = spaceId,
    tenantId = tenantId,
    role = SpaceRole.valueOf(role),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )

public fun SpaceMember.toEntity(): SpaceMemberEntity =
  SpaceMemberEntity(
    spaceId = spaceId,
    tenantId = tenantId,
    role = role.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
  )
