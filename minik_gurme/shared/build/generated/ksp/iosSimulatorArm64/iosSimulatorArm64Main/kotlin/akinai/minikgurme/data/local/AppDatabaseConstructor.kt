package akinai.minikgurme.`data`.local

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  actual override fun initialize(): AppDatabase = akinai.minikgurme.`data`.local.AppDatabase_Impl()
}
