# Game Integration Guide

This guide shows how to integrate all universal features into each game.

## Step 1: Update ViewModel to extend BaseGameViewModel

```kotlin
@HiltViewModel
class YourGameViewModel @Inject constructor(
    private val repository: PreferencesRepository,
    private val adManager: AdManager,
    private val soundAndHapticsManager: SoundAndHapticsManager,
    private val savedStateHandle: SavedStateHandle,
    networkManager: NetworkManager,
    statisticsManager: StatisticsManager,
    achievementManager: AchievementManager,
    progressionManager: ProgressionManager
) : BaseGameViewModel(
    gameType = GameType.YOUR_GAME,
    networkManager = networkManager,
    statisticsManager = statisticsManager,
    achievementManager = achievementManager,
    progressionManager = progressionManager
), ViewModel() {
    
    // Your existing game logic...
    
    // When game ends, call:
    private fun onGameComplete() {
        if (!completionRecorded) {
            completionRecorded = true
            viewModelScope.launch {
                recordGameResult(
                    won = true,  // or false if lost
                    score = currentScore,
                    timeInSeconds = gameTimeInSeconds,
                    movesOrAttempts = moveCount,
                    hintsUsed = hintsUsedCount
                )
            }
        }
    }
}
```

## Step 2: Add UI Monitors to Screen

```kotlin
@Composable
fun YourGameScreen(
    onBackClick: () -> Unit,
    viewModel: YourGameViewModel = hiltViewModel()
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    
    // Show offline dialog when network lost
    OfflineMonitor(isOnline = isOnline)
    
    // Show achievement unlocked dialog
    AchievementMonitor(achievementFlow = viewModel.achievementUnlocked)
    
    // Show level up dialog
    LevelUpMonitor(levelUpFlow = viewModel.levelUp)
    
    // Rest of your UI...
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Game") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show current difficulty
                    DifficultyBadge(difficulty = selectedDifficulty)
                }
            )
        }
    ) { padding ->
        // Your game content...
    }
}
```

## Step 3: Add Difficulty Selection Before Game Starts

```kotlin
// In your game screen, add a difficulty selection dialog
var showDifficultyDialog by remember { mutableStateOf(false) }

if (showDifficultyDialog) {
    Dialog(onDismissRequest = { showDifficultyDialog = false }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DifficultySelector(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = { difficulty ->
                        viewModel.setDifficulty(difficulty)
                        showDifficultyDialog = false
                        viewModel.startNewGame()
                    }
                )
            }
        }
    }
}
```

## Step 4: Track Custom Achievements

```kotlin
// In your ViewModel, track custom achievements:
private suspend fun checkCustomAchievements() {
    // Example: Merge a specific number
    if (mergedNumber == 2048) {
        trackAchievementProgress("merge_2048", 1)
    }
    
    // Example: Complete without hints
    if (hintsUsed == 0) {
        trackAchievementProgress("no_hints_master", 1)
    }
    
    // Example: Win in under 60 seconds
    if (gameTime < 60 && won) {
        trackAchievementProgress("speed_runner", 1)
    }
}
```

## Required Changes Per Game:

### 1. NumberMerge
- [x] Extend BaseGameViewModel
- [ ] Add OfflineMonitor, AchievementMonitor, LevelUpMonitor
- [ ] Add difficulty selection at game start
- [ ] Call recordGameResult on game over
- [ ] Track merge 2048 achievement

### 2. MemoryMatch
- [x] Extend BaseGameViewModel
- [ ] Add monitors
- [ ] Add difficulty (affects number of cards)
- [ ] Call recordGameResult on match complete
- [ ] Track perfect match achievement

### 3. QuickMath
- [x] Extend BaseGameViewModel
- [ ] Add monitors
- [ ] Add difficulty (affects problem difficulty)
- [ ] Call recordGameResult on time up
- [ ] Track answer streak achievement

### 4. WordSearch
- [x] Extend BaseGameViewModel
- [ ] Add monitors
- [ ] Add difficulty (affects grid size)
- [ ] Call recordGameResult on puzzle complete
- [ ] Track find all words achievement

### 5-11. Other Games
- [ ] Same pattern for all remaining games

## Testing Checklist

For each integrated game:
- [ ] Game starts with difficulty selection
- [ ] Offline dialog shows when internet lost
- [ ] Achievement unlocks show dialog
- [ ] Level up shows dialog
- [ ] XP and coins awarded on win
- [ ] Statistics recorded in database
- [ ] Leaderboard entry created if signed in
- [ ] Difficulty multiplier applies to score/rewards
