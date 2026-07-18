package com.example.tabletoptoolbox.tabletier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.navigation.NavController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    navController: NavController,
    buttonColors: ButtonColors,
    tableTierViewModel: TableTierViewModel,
    tierListViewModel: TierListViewModel
) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    // Zajednički horizontalni scroll state tablice na stranici 0.
    val tableHorizontalScrollState = rememberScrollState()

    // Umetnut IZMEĐU tablice (dijete) i Pagera (predak) u nested-scroll lancu.
    // Standardno ponašanje: tablica prvo dobije priliku scrollati, a ono što
    // joj "ostane" (leftover) automatski se prelijeva prema gore, prema
    // Pageru - upravo TO prelijevanje unutar istog gesta uzrokuje bug (jedan
    // swipe mijenja i tablicu i stranicu).
    //
    // Odluku "smije li Pager dobiti leftover" fotografiramo SAMO na početku
    // svakog gesta (prvi onPreScroll poziv, prije nego što se itko pomaknuo)
    // i držimo je fiksnom do kraja tog gesta - reset tek u onPostFling, kad
    // gesture (drag + eventualni fling) stvarno završi:
    //   - tablica NIJE bila na rubu kad je gesture počeo -> cijeli leftover
    //     progutamo mi u onPostScroll (vratimo available.x) - Pager ga nikad
    //     ne vidi, čak i ako tablica usred OVOG istog pokreta stigne do ruba.
    //   - tablica JEST bila na rubu kad je gesture počeo -> ništa ne
    //     konzumiramo (Offset.Zero) - leftover normalno prolazi do Pagera,
    //     koji onda (za taj novi swipe) mijenja stranicu.
    val nestedScrollConnection = remember(tableHorizontalScrollState) {
        object : NestedScrollConnection {
            // null = odluka za trenutni gesture još nije donesena
            private var tableWasAtEdgeAtGestureStart: Boolean? = null

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (tableWasAtEdgeAtGestureStart == null) {
                    tableWasAtEdgeAtGestureStart =
                        tableHorizontalScrollState.value == 0 ||
                                tableHorizontalScrollState.value == tableHorizontalScrollState.maxValue
                }
                // Ne konzumiramo ništa unaprijed - pustimo tablicu da prva
                // iskoristi koliko god može od dostupnog pomaka.
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (tableWasAtEdgeAtGestureStart == false) {
                    // Gesture je krenuo "u sredini" tablice - progutaj sav
                    // preostali horizontalni pomak, Pager ga ne smije vidjeti.
                    Offset(x = available.x, y = 0f)
                } else {
                    // Gesture je krenuo dok je tablica već bila na rubu -
                    // pusti leftover dalje prema Pageru.
                    Offset.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                // Gesture je gotov - resetiraj odluku za sljedeći, novi swipe.
                tableWasAtEdgeAtGestureStart = null
                return Velocity.Zero
            }
        }
    }

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.35f
    )

    HorizontalPager(
        state = pagerState,
        flingBehavior = flingBehavior,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                TableTier(navController, buttonColors, tableTierViewModel, tableHorizontalScrollState)
            }
            1 -> TierListScreen(navController, tierListViewModel)
        }
    }
}