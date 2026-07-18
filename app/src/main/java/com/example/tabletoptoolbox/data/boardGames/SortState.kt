package com.example.tabletoptoolbox.data.boardGames

enum class SortColumn { NAME, RATING, ELO, TIME }
enum class SortDirection { ASC, DESC }

data class SortState(
    val column: SortColumn = SortColumn.ELO,
    val direction: SortDirection = SortDirection.DESC
)