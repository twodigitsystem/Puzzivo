package com.tds.puzzivo.feature.themes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.AccentMagenta
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.design.ThemeCard

@Composable
fun ThemesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemesViewModel = hiltViewModel()
) {
    val themes by viewModel.allThemes.collectAsState()
    val selectedThemeId by viewModel.selectedTheme.collectAsState()
    val unlockedThemeIds by viewModel.unlockedThemes.collectAsState()

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "THEMES & APPEARANCE",
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Info Header Card
                PuzzivoCard(
                    backgroundColor = NavySurface,
                    borderColor = AccentMagenta.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🎨", fontSize = 24.sp)
                            Text(
                                text = "Personalize Your Visuals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Level up, achieve daily win streaks, and unlock new achievements to earn vibrant theme palettes for Puzzivo!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Themes Vertical Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(themes, key = { it.id }) { theme ->
                        val isUnlocked = unlockedThemeIds.contains(theme.id)
                        val isSelected = selectedThemeId == theme.id

                        ThemeCard(
                            theme = theme,
                            isUnlocked = isUnlocked,
                            isSelected = isSelected,
                            onSelect = {
                                if (isUnlocked) {
                                    viewModel.selectTheme(theme.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
