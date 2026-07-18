package com.example.tabletoptoolbox.data.boardGames

import com.example.tabletoptoolbox.data.games.calculateNewElo
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    fun getGamesSortedByElo(): Flow<List<GameEntity>> = gameDao.getAllSortedByElo()

    fun getAllGames(): Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun getGameById(id: Long): GameEntity? = gameDao.getGameById(id)

    suspend fun recordComparison(winner: GameEntity, loser: GameEntity) {
        val (newWinnerElo, newLoserElo) = calculateNewElo(winner.eloRating, loser.eloRating)
        gameDao.updateGame(winner.copy(
            eloRating = newWinnerElo,
            comparisonCount = winner.comparisonCount + 1
        ))
        gameDao.updateGame(loser.copy(
            eloRating = newLoserElo,
            comparisonCount = loser.comparisonCount + 1
        ))
    }

    suspend fun addGame(
        name: String,
        description: String,
        imagePath: String?,
        userRating: Float,
        playTimeMinutes: Int,
        minPlayers: Int,
        maxPlayers: String?
    ): Long {
        val game = GameEntity(
            name = name,
            description = description,
            imagePath = imagePath,
            userRating = userRating,
            eloRating = 1200.0,
            comparisonCount = 0,
            playTimeMinutes = playTimeMinutes,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers
        )
        return gameDao.insertGame(game)
    }

    suspend fun updateGame(game: GameEntity) = gameDao.updateGame(game)

    suspend fun deleteGame(game: GameEntity) = gameDao.deleteGame(game)
}