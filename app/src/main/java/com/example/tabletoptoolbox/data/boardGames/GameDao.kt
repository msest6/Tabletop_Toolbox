package com.example.tabletoptoolbox.data.boardGames

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY eloRating DESC")
    fun getAllSortedByElo(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games ORDER BY userRating DESC")
    fun getAllSortedByUserRating(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): GameEntity?

    @Update
    suspend fun updateGame(game: GameEntity)

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT * FROM games ORDER BY comparisonCount ASC LIMIT :limit")
    suspend fun getLeastComparedGames(limit: Int): List<GameEntity>
}