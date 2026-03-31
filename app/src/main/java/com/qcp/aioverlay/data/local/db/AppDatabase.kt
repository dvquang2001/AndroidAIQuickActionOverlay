package com.qcp.aioverlay.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.qcp.aioverlay.data.local.dao.HistoryDao
import com.qcp.aioverlay.data.local.entity.HistoryEntity
import com.qcp.aioverlay.domain.model.ActionType

class ActionTypeConverters {
    @TypeConverter fun fromActionType(type: ActionType): String = type.name
    @TypeConverter fun toActionType(name: String): ActionType = ActionType.valueOf(name)
}

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ActionTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}