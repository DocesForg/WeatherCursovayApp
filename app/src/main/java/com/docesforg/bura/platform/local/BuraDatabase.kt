package com.docesforg.bura.platform.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey val id: Long,
    val email: String,
    val displayName: String,
)

@Entity(
    tableName = "favorite_city",
    primaryKeys = ["accountId", "id"],
)
data class FavoriteCityEntity(
    val id: Long,
    val accountId: Long,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
)

@Entity(
    tableName = "support_ticket",
    primaryKeys = ["accountId", "id"],
)
data class SupportTicketEntity(
    val id: Long,
    val accountId: Long,
    val email: String,
    val name: String,
    val question: String,
    val sender: String,
    val createdAt: String,
)

@Entity(
    tableName = "radio_signal_test",
    primaryKeys = ["accountId", "id"],
)
data class RadioSignalTestEntity(
    val id: Long,
    val accountId: Long,
    val cityA: String,
    val cityB: String,
    val distanceKm: Double,
    val pathLossDb: Double,
    val quality: String,
    val latencyMs: Double,
    val speedMbps: Double,
    val createdAt: String,
)

@Dao
interface BuraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: AccountEntity)

    @Query("SELECT * FROM account WHERE id = :accountId LIMIT 1")
    suspend fun getAccount(accountId: Long): AccountEntity?

    @Query("DELETE FROM account WHERE id = :accountId")
    suspend fun deleteAccount(accountId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorites(items: List<FavoriteCityEntity>)

    @Query("SELECT * FROM favorite_city WHERE accountId = :accountId")
    suspend fun getFavorites(accountId: Long): List<FavoriteCityEntity>

    @Query("DELETE FROM favorite_city WHERE accountId = :accountId")
    suspend fun deleteFavorites(accountId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSupport(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_ticket WHERE accountId = :accountId ORDER BY createdAt DESC")
    suspend fun getSupportTickets(accountId: Long): List<SupportTicketEntity>

    @Query("DELETE FROM support_ticket WHERE accountId = :accountId")
    suspend fun deleteSupportTickets(accountId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRadioTest(item: RadioSignalTestEntity)

    @Query("SELECT * FROM radio_signal_test WHERE accountId = :accountId ORDER BY id DESC")
    suspend fun getRadioTests(accountId: Long): List<RadioSignalTestEntity>

    @Query("DELETE FROM radio_signal_test WHERE accountId = :accountId")
    suspend fun deleteRadioTests(accountId: Long)
}

@Database(
    entities = [
        AccountEntity::class,
        FavoriteCityEntity::class,
        SupportTicketEntity::class,
        RadioSignalTestEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class BuraDatabase : RoomDatabase() {
    abstract fun dao(): BuraDao

    companion object {
        fun create(context: Context): BuraDatabase = Room.databaseBuilder(
            context,
            BuraDatabase::class.java,
            "bura-room.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
