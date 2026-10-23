![Puzzivo app concept icon](a_bright_clean_vector_style_app_icon_logo_illu.png){ width=1.55in }

# PUZZIVO
## Product Requirements Document
### Android Mini-Games & Brain Puzzles

**Version:** 1.0  
**Date:** 21 October 2026  
**Status:** Baseline for MVP implementation  
**Platform:** Android  
**Business model:** Free app supported by advertising  
**Primary build tools:** Android Studio, Kotlin, Jetpack Compose  
**Budget assumption:** Free development tools; does not include Google Play registration or any required external costs

> **Purpose of this document:** Define what Puzzivo v1 should do, how it should behave, how it will be built and tested, and what must be ready before public release. This is a product specification, not a promise of revenue or a guarantee of Google Play approval.

---

\newpage

## Document guide
1. Executive summary
2. Product vision, goals and non-goals
3. Audience and product positioning
4. MVP scope and release boundaries
5. User journeys and navigation
6. Detailed game requirements
7. Shared app requirements
8. Advertising and monetization
9. UX, visual design and accessibility
10. Technical architecture
11. Data, privacy and security
12. Performance and quality requirements
13. Analytics and success metrics
14. Acceptance criteria and test plan
15. Delivery roadmap
16. Launch and store checklist
17. Risks, assumptions and open decisions
18. Official references

# 1. Executive summary

Puzzivo is a lightweight, offline-first Android app that brings four short brain and puzzle games into one polished experience:

1. **Number Merge** — merge equal number tiles on a 4×4 board and beat a high score.
2. **Memory Match** — find matching pairs of hidden cards with as few moves as possible.
3. **Quick Math** — answer timed arithmetic questions and build a score streak.
4. **Word Search** — find a set of hidden words in a letter grid.

The app is designed for solo development with a zero-cost software stack. It will use Kotlin and Jetpack Compose in Android Studio, store progress locally, and require no user account or custom backend for v1. The app is free to play. Monetization will begin with an unobtrusive home-screen banner and optional rewarded ads for clearly described, in-game benefits. No interstitial ads are included in the initial MVP.

The product strategy is to launch a small, reliable collection rather than a large catalogue. A consistent visual style, short sessions, saved progress, daily return hooks, and respectful advertising should make the four games feel like one product.

## 1.1 Product statement

**Puzzivo — Think. Play. Repeat.** Four quick games in one app, playable offline, with optional ads that never block core gameplay.

## 1.2 MVP release definition

Version 1 is ready for public release when all four games are playable end-to-end, basic progress survives app restarts, the app functions offline, ads fail safely, the privacy and store disclosures are accurate, and release quality checks pass. The app must not depend on account creation, server uptime, or network access for core gameplay.

# 2. Product vision, goals and non-goals

## 2.1 Vision

Give users one friendly place to play a few minutes of number, memory, arithmetic, and word puzzles whenever they have a spare moment.

## 2.2 Product goals

- Launch four understandable mini-games in one Android app.
- Make the first playable interaction easy to reach from the home screen.
- Keep core gameplay offline and fast on modest Android phones.
- Save high scores, basic preferences, and daily streak state locally.
- Monetize without forcing players to watch ads to progress.
- Build an architecture where another mini-game can be added without rewriting navigation, storage, or advertising code.
- Learn from early retention and stability data before investing in more features.

## 2.3 Non-goals for v1

- Real-money gambling, cash prizes, crypto, or withdrawable rewards.
- Online multiplayer, chat, public profiles, or user-generated content.
- Accounts, cloud saves, social sign-in, or a custom backend.
- In-app purchases, subscriptions, or paid currencies.
- Push notifications.
- AI-generated content or server-generated puzzle packs.
- Competitive online leaderboards.
- More than the four defined games.
- Ads during a live puzzle or ads required to access a game's basic features.

## 2.4 Product principles

1. **Play first:** users can start a game without sign-up or tutorial walls.
2. **Offline core:** no connection is required to play or save local progress.
3. **Fair ads:** disclose the reward before an ad; never grant a reward merely because an ad was requested.
4. **Small and dependable:** prefer a stable implementation over unnecessary features.
5. **One app, one design system:** shared navigation, colors, typography, buttons, feedback, settings, and ad behavior.
6. **Honest privacy:** disclose what the app and its SDKs collect; do not claim the app collects no data if the ad SDK collects or shares data.

# 3. Audience and product positioning

## 3.1 Intended audience

Casual puzzle players who enjoy short solo games and want a variety of mental challenges in one app. English is the initial app language. The initial content is designed for a general audience, but the final Play Console target-age declaration must accurately reflect the actual product, branding, content, and marketing. Do not select an age group merely to avoid policy requirements. If children are included in the target audience, the Google Play Families and ads requirements must be reviewed before release [R5, R6].

## 3.2 User personas

| Persona | Need | Puzzivo response |
|---|---|---|
| The short-break player | A quick game during a break, with no setup | One-tap access to all games; short sessions; offline support |
| The daily challenger | A reason to return and beat a previous result | High scores, streak display, daily game prompt |
| The casual learner | Variety without installing four separate apps | Four mini-games with shared visual language and controls |
| The low-connectivity user | Reliable play on weak or absent internet | All puzzle logic and starter content bundled locally |

## 3.3 Positioning

Puzzivo is a **compact casual brain-game collection**, not a medical brain-training product. Do not claim that it improves intelligence, prevents cognitive decline, or delivers medically proven benefits unless qualified evidence exists and the claims are reviewed.

# 4. MVP scope and release boundaries

## 4.1 Must-have for v1

- Branded launch and home screen.
- Four games: Number Merge, Memory Match, Quick Math, Word Search.
- Shared game navigation and result screens.
- Local save for best scores, preferences, and basic progress.
- Basic tutorial or first-play instructions for each game.
- Pause/restart/back behavior appropriate to each game.
- Daily return element: a “Today's Pick” card that rotates among the four games using the local calendar date. Completing a round of the selected game marks today's pick complete; no network or account is required.
- Settings: sound toggle, haptics toggle, privacy policy, app version, and support/contact link when available.
- Home-screen banner ad slot that does not overlap game controls.
- Optional rewarded ad for a defined hint/benefit in supported games.
- Consent/privacy flow required by the selected ad SDK and the user's region.
- Offline and ad-failure handling.
- Store icon, screenshots, short/long descriptions, content rating, target audience, data safety declaration, privacy policy, signed Android App Bundle.

## 4.2 Should-have if time allows, without blocking core stability

- Simple onboarding tooltips that appear once.
- Subtle sound effects and optional haptics.
- A small local collection of Word Search packs.
- Dark-theme support if it can be achieved without slowing the first release.

## 4.3 Explicitly deferred to v1.1 or later

- Interstitial ads, after retention and user experience are measured.
- More word packs, difficulty themes, or new mini-games.
- Bengali and Hindi localization.
- Cloud saves, accounts, online leaderboards, social features, push notifications.
- Advanced game statistics and additional analytics SDKs.
- Any purchase-based monetization.

# 5. User journeys and navigation

## 5.1 Primary journey

1. User opens Puzzivo.
2. A lightweight launch screen appears only while essential initialization completes; no app-open ad is shown in v1.
3. Home screen displays logo/name, “Today's Pick,” four game cards, basic play statistics, and a reserved banner-ad area.
4. User taps a game card.
5. A short instruction overlay appears on first play; users can dismiss or skip it.
6. User plays, pauses or restarts as relevant.
7. The result screen presents score or performance, best result, replay, home, and (where relevant) an optional reward/hint ad CTA.
8. The app saves the result and returns the user to the prior screen or home as appropriate.

## 5.2 Navigation map

- **Home**
  - Today's Pick
  - Number Merge
  - Memory Match
  - Quick Math
  - Word Search
  - Settings
- **Each game**
  - Game screen
  - Pause/instructions overlay (where relevant)
  - Result/game-over screen
- **Settings**
  - Sound
  - Haptics
  - Privacy Policy
  - Support/contact
  - App version

System Back should pause or exit a game safely, with a confirmation when leaving would lose an active session. Back from a result returns to the game selection/home without clearing saved results.

## 5.3 Empty, loading and error states

- If an ad is unavailable, hide or disable the ad CTA with a calm message; do not block game play.
- If a puzzle resource fails to load, show a retry action and log a non-sensitive diagnostic where possible.
- If local preference data is missing or invalid, recover with defaults rather than crash.
- If internet is unavailable, games remain fully usable. The banner/rewarded-ad slot may show no ad.

# 6. Detailed game requirements

## 6.1 Game A — Number Merge

**Purpose:** A familiar, replayable number puzzle with a score chase.  
**Orientation:** Portrait preferred; controls must remain usable across common phone sizes.

### Rules

- Board is 4×4.
- New game begins with two tiles, each a low power of two (normally 2, occasionally 4).
- User swipes up/down/left/right to slide all tiles in that direction.
- Adjacent equal tiles merge once per move into a tile with double the value. A newly merged tile cannot merge again during that same move.
- A new tile spawns only after a move changes the board.
- Score increases by the value of each newly created merged tile.
- Best score is saved locally.
- Reaching 2048 triggers a milestone state, but the player may continue.
- Game over occurs when the board is full and no legal merge or move remains.
- Undo is not required in v1.

### UI requirements

- Score and best score visible above the board.
- New Game and restart confirmation.
- First-play instruction overlay explains swipes and merging.
- Clear tile contrast and numbers readable at small screen widths.
- Valid moves respond immediately; invalid swipes do not spawn a tile.
- On game over, show final score, best score update when applicable, replay and home options.

### Rewarded ad placement

Offer “Undo last move” as the rewarded benefit only if a correct, tested last-move snapshot is available. Otherwise omit the ad CTA for this game in v1 rather than implement an unreliable reward.

### Acceptance checks

- Merging two equal tiles produces one doubled tile and correct score.
- A tile merged during the move does not merge again in the same swipe.
- No-op moves do not spawn tiles.
- Game-over detection is correct for a full board with and without available merges.
- Best score survives process termination and relaunch.

## 6.2 Game B — Memory Match

**Purpose:** A quick card-pair memory challenge.

### Rules

- MVP board is 4×4, containing eight matching pairs.
- Cards begin face-down and use an internally shuffled deck.
- User taps one card to reveal it, then a second card.
- If they match, both remain revealed; if not, both stay visible briefly (about 700–1000 ms) and then turn face-down.
- Input is disabled while a mismatch is in the temporary reveal state to prevent extra flips.
- Count each pair attempt as one move.
- A round is complete when all pairs are found.
- Record elapsed time and move count; save personal best using a clearly documented rule (fewest moves, with time as tie-breaker).

### UI requirements

- Move count and timer are legible.
- Cards have sufficient touch size and a visible selected/revealed state.
- Restart requires confirmation during a live round.
- Result shows moves and time, with replay/home buttons.

### Rewarded ad placement

An optional “Reveal a pair” hint may reveal one unmatched pair briefly. The reward must only be granted after the SDK confirms the ad reward. Limit the hint to one use per round in v1.

### Acceptance checks

- A card cannot be matched with itself.
- Matched cards cannot be flipped again.
- Rapid tapping does not cause more than two cards to be revealed at once.
- Mismatch timeout resets the correct two cards only.
- The game ends exactly when all eight pairs are matched.

## 6.3 Game C — Quick Math

**Purpose:** A short timed arithmetic game designed for replay and score improvement.

### Rules

- Standard round length is 60 seconds.
- Each question presents one expression and four answer choices.
- v1 supports addition, subtraction, multiplication, and exact-integer division; division questions must never produce a non-integer correct answer.
- Early questions use small positive integers; difficulty increases gradually as the round progresses.
- Correct answer earns 10 points. An optional consecutive-correct streak bonus may be included only if the scoring rule is shown in the instructions and tested. Recommended v1 default: no multiplier; keep scoring simple.
- Incorrect answer earns zero points; no extra time is deducted.
- After an answer, the next question appears promptly. Timer reaching zero ends the round.
- Pause stops the timer. Resume continues the remaining time.
- Best score is saved locally.

### UI requirements

- Large expression, four clearly separated answers, timer and score.
- Feedback uses text/icon as well as color so correctness is not conveyed by color alone.
- Avoid tiny answer buttons and unintended taps.
- Results show score, best score and replay.

### Rewarded ad placement

Optional “Add 15 seconds” once per round, only if the SDK confirms the reward. The round remains playable when the ad is unavailable or declined.

### Acceptance checks

- Correct option is always among the four choices.
- Wrong choices are unique and do not duplicate the correct answer.
- Division answers are exact integers and division by zero is impossible.
- Timer pauses and resumes accurately and ends the round at zero.
- Ad reward cannot be applied twice to one ad completion.

## 6.4 Game D — Word Search

**Purpose:** Find a list of words in a letter grid.

### Rules

- v1 ships with a minimum of 20 offline puzzle boards bundled with the app.
- Each puzzle uses an 8×8 or 10×10 letter grid, 6–8 target words, and a defined difficulty.
- Words can appear horizontally, vertically, or diagonally. Reverse directions may be used only if selection logic and instructions support them consistently.
- User selects a straight line from a start cell to an end cell by touch/drag.
- On a valid match, the word is marked found and highlighted; its list entry is crossed off or marked complete.
- Invalid selection gives brief, non-disruptive feedback.
- Puzzle completes when all target words are found.
- Progress through the bundled puzzle set is stored locally.

### Content requirements

- Use original or properly licensed word lists and puzzle layouts.
- No offensive, hateful, adult or trademark-dependent word packs.
- The puzzle validator must confirm every listed word exists on the intended board and that the final board is selectable using the supported selection rules.

### Rewarded ad placement

Optional “Reveal a word” hint highlights a target word's start and end cells. One use per puzzle in v1. Reward only after the SDK reward callback.

### Acceptance checks

- Every bundled word exists on the board in the expected direction.
- Gesture selection maps accurately to grid cells across screen sizes.
- The same word cannot be counted twice.
- Completing all words triggers the result state once.
- Puzzle progress and unlocked puzzle index survive app restart.

# 7. Shared app requirements

## 7.1 Home screen

- Display Puzzivo identity and the tagline “Think. Play. Repeat.”
- Show four accessible game cards with name, short descriptor, and play action.
- Show “Today's Pick,” which rotates among the four games by date without a server.
- Show simple aggregate data such as total rounds completed and current streak only if it can be calculated locally and clearly.
- Reserve a non-overlapping area for the banner ad; the ad must not look like a game card or gameplay control.
- On smaller devices, the content scrolls; no primary game is permanently inaccessible.

## 7.2 Daily pick and streak

- Determine the selected game from a stable local date-based rotation.
- Mark the daily pick completed when the user completes at least one round of that day's game.
- Increment the streak only when the next calendar day is completed; preserve the streak if a day is missed only if the rule explicitly says so (v1 default: missed day resets the current streak, while best streak remains recorded).
- Display the rule in plain language. Do not imply that the streak has cash or real-world value.
- Handle date changes safely; no remote clock is available in v1.

## 7.3 Settings

- Sound: on/off.
- Haptics: on/off.
- Privacy Policy: opens a valid public privacy-policy URL.
- Support: email or support URL configured before release; if not configured, hide the item rather than show a dead link.
- App version: displayed for troubleshooting.

## 7.4 State persistence

Persist locally:
- Best score for Number Merge and Quick Math.
- Best move count/time for Memory Match.
- Current Word Search puzzle and solved puzzle IDs.
- Games completed, daily pick status, current/best streak.
- Sound and haptics preferences.

Do not persist unnecessary personal information. If local data is corrupted or the app is upgraded from a prior schema, migration/default handling should prevent a startup crash.

# 8. Advertising and monetization

## 8.1 Business model

Puzzivo is free to download and play. Monetization is through Google AdMob advertising only in v1. Revenue is not guaranteed and must not be represented as a user earning opportunity.

## 8.2 Ad placements for v1

| Placement | Format | Rule |
|---|---|---|
| Home screen bottom area | Adaptive banner | One reserved slot; never overlaps navigation or game controls |
| Memory Match hint | Rewarded | Optional; one reveal-pair reward per round |
| Quick Math hint | Rewarded | Optional; add 15 seconds once per round |
| Word Search hint | Rewarded | Optional; reveal one word once per puzzle |
| Number Merge benefit | Rewarded only if undo is reliable | Otherwise no ad placement in this game for v1 |

No interstitial or app-open ads in v1. Review ad format expansion only after user retention and qualitative feedback are understood.

## 8.3 Reward integrity

- Explain the benefit before the user chooses to watch.
- The user can decline without losing progress or being blocked.
- Grant the reward only from the ad SDK's documented reward callback.
- Do not grant duplicate rewards if a callback fires more than once or the screen is recreated.
- If an ad fails, closes before qualifying, or is unavailable, do not grant the reward; show a calm retry/later state and keep the game playable.
- Rewarded ads may be temporarily unavailable offline.
- Keep debug and release ad IDs separate. Use Google's test ad units/test devices in development; never click live ads during testing [R3, R4].

## 8.4 SDK and consent

- Use the current supported Google Mobile Ads Android SDK track recommended by Google's documentation at implementation time; do not mix legacy and Next-Gen SDK APIs.
- Integrate Google's User Messaging Platform (UMP) where required. Refresh consent information at app launch as the UMP guidance describes, show required consent/privacy options, and do not request ads before the required consent flow is handled [R4].
- Configure ad content rating and filtering to match the app's actual content rating.
- Audit SDK behavior and keep privacy policy and Play Data safety responses aligned with what all included SDKs collect/share [R1, R2, R5].

## 8.5 Revenue planning model (illustrative, not a forecast)

Formula:

`Monthly gross ad revenue = monthly monetized impressions ÷ 1,000 × realized eCPM`

Assuming 30 days, 1.5 monetized impressions per daily active user per day, and illustrative realized eCPMs of ₹20 and ₹100:

| Average DAU | Monthly impressions | At ₹20 eCPM | At ₹100 eCPM |
|---:|---:|---:|---:|
| 100 | 4,500 | ₹90 | ₹450 |
| 1,000 | 45,000 | ₹900 | ₹4,500 |
| 10,000 | 450,000 | ₹9,000 | ₹45,000 |

These figures are scenario math, not observed market rates. Actual revenue depends on geography, ad format, fill, consent choices, engagement, and the number of ads actually served and monetized. Revenue is gross before taxes or other obligations. Do not pay for user acquisition until actual cohort data establishes retention and realistic value per active user.

# 9. UX, visual design and accessibility

## 9.1 Brand and visual direction

- Brand: Puzzivo.
- Tagline: “Think. Play. Repeat.”
- Design direction: deep navy/blue base, bright puzzle accents (yellow, cyan, green, magenta), rounded surfaces, clear numbers and icons, friendly motion.
- The selected concept icon may be used as a starting point. It is not by itself a complete Android launcher icon asset; create adaptive launcher icon foreground/background variants and verify at small sizes before release.
- The wordmark should appear on app-level branding, not be crammed into the small launcher glyph.

## 9.2 Interaction rules

- Use a consistent top-bar/back pattern across game screens.
- Game controls respond immediately, with clear pressed/selected states.
- Do not place ads next to primary game controls or where a tap could be mistaken for an ad click.
- Confirm destructive actions such as restarting an active run.
- Explain rules in plain language, using one-screen first-play guidance.
- Provide visible feedback for answer correctness, matching, word discovery, new records, and game completion.

## 9.3 Accessibility

- Main touch targets should be approximately 48 dp or larger where practical.
- Support system font scaling without clipping key controls or scores.
- Give meaningful content descriptions to interactive icons.
- Maintain adequate contrast for text, tiles, card states, and selection outlines.
- Do not rely on color alone to communicate success/error; also use text, shape or icon cues.
- Respect system reduce-animation preferences where practical and keep motion subtle.
- Check TalkBack reading order on home, settings, result screens and ad-adjacent UI.

# 10. Technical architecture

## 10.1 Recommended stack

| Layer | Choice | Rationale |
|---|---|---|
| IDE/build | Android Studio, Gradle Kotlin DSL | Native Android development |
| Language | Kotlin | Primary app language |
| UI | Jetpack Compose + Material 3 | Efficient screen composition and state-driven UI [R7] |
| App architecture | Single activity, feature screens, ViewModels, unidirectional data flow | Testable state and predictable UI updates [R7] |
| Local persistence | Preferences/DataStore for settings and small aggregate progress; local JSON/assets for puzzles | No backend required |
| Advertising | Current supported Google Mobile Ads Android SDK; rewarded and banner formats | Ad-supported business model [R3, R4] |
| Consent | Google UMP SDK when required | Handle ad consent/privacy messages [R4] |
| Tests | Kotlin unit tests; Compose UI tests; manual device matrix | Protect game logic and interaction behavior |
| Source control | Git | Free local history and backup |

At build start, choose current stable Android Studio, Kotlin, Gradle, Compose BOM, SDK, and ad SDK versions based on their official compatibility guidance. Do not copy old code examples without confirming they match the chosen SDK track.

## 10.2 Android build configuration

- Minimum SDK: API 24 or higher for alignment with the current Google Mobile Ads Android SDK prerequisites [R3]. Recheck at implementation time.
- Compile SDK and target SDK: target Android 16 / API 36 or higher for Google Play submissions after the August 31, 2026 deadline [R8].
- Package ID: choose a permanent reverse-domain identifier before first publication; example placeholder only: `com.yourstudio.puzzivo`.
- Release artifact: signed Android App Bundle (`.aab`).
- Keep app signing keys and AdMob configuration private; do not commit credentials or private signing material to public source control.

## 10.3 Suggested source layout

```text
com.yourstudio.puzzivo/
├── MainActivity.kt
├── app/
│   ├── PuzzivoApp.kt
│   └── AppNavHost.kt
├── core/
│   ├── design/          # Colors, typography, reusable components
│   ├── model/            # Shared result/settings models
│   ├── storage/          # Local preferences and progress repository
│   └── ads/              # Ad manager, callbacks, placement policy
├── feature/
│   ├── home/
│   ├── settings/
│   ├── numbermerge/
│   ├── memorymatch/
│   ├── quickmath/
│   └── wordsearch/
├── data/
│   ├── puzzles/          # Bundled Word Search puzzle JSON/assets
│   └── repository/       # Repository implementations
└── tests/
    ├── game logic unit tests
    └── UI/integration tests
```

This is a suggested organization, not a requirement to create separate Gradle modules at the start. Prefer packages first; split modules only when build complexity justifies it.

## 10.4 State and logic principles

- Keep core game rules in Kotlin classes/functions independent of Compose UI.
- UI emits actions; state holders/ViewModels update game state; screens render immutable state. This follows the state-down/events-up model described in Compose architecture guidance [R7].
- Make RNG injectable/seedable for deterministic tests.
- Keep all ad callbacks behind one ad manager; game feature code requests a named benefit instead of directly manipulating the SDK.
- Game logic and save operations must work without network connectivity.

# 11. Data, privacy and security

## 11.1 Data minimization

Puzzivo v1 does not require user accounts, contacts, location, camera, microphone, photos, or background location. Do not request those permissions. Store game progress locally. Advertising SDKs may process device or ad-related data; the privacy policy and Data safety form must reflect the actual SDK configuration and behavior rather than just the app's own code [R1].

## 11.2 Required privacy work before release

- Publish a privacy policy at a publicly accessible URL and link it from both Play Console and within the app.
- Name Puzzivo and the developer/contact point in the policy.
- Describe data access, collection, use, sharing, security, retention and deletion truthfully.
- Complete the Play Console Data safety form accurately, including SDK data practices.
- Declare ads in Play Console and complete the target audience/content rating sections accurately.
- Ensure the content rating and ad settings align.
- Use the consent/privacy UX required by the chosen advertising SDK and applicable jurisdictions.
- Revisit disclosures whenever an SDK or data practice changes.

Google Play states that a privacy policy is required for every app, including apps that do not access personal and sensitive data, and that the Data safety section must be accurate and consistent with the privacy policy [R1, R2].

## 11.3 Audience and child-safety release gate

Before release, the developer must make an explicit and truthful decision about target age groups. If children are included in the target audience, Google Play Families requirements may apply to app content, data practices, and ad SDKs/formats [R5, R6]. Do not assume a general puzzle app is automatically exempt. Confirm the latest policy and the selected SDK's status before release.

## 11.4 Security

- Do not ship private keys, tokens, signing credentials, or secret server-side values in the client.
- Use HTTPS for external links and the privacy policy.
- Validate local puzzle resources before loading; handle malformed data without crashing.
- Restrict app permissions to those genuinely required.
- Do not implement deceptive buttons, ad-like game controls, or ads that imitate system UI.
- If a future version adds accounts or cloud storage, define account deletion, retention and server security before implementation.

# 12. Performance and quality requirements

These are product targets to verify on representative devices, not guarantees across every Android device.

| Area | Requirement |
|---|---|
| Startup | Home screen should become usable promptly; target under 3 seconds on a mid-range supported test phone after warm-up |
| Input latency | Local game actions should visibly respond within about 100 ms in normal conditions |
| Frame rate | Aim for smooth 60 fps animation on supported mainstream devices; avoid unnecessary effects on puzzle grids |
| Offline | All four core games, local saves and bundled puzzles work in airplane mode |
| Stability | No known blocker/crash in release candidate; target at least 99% crash-free sessions once enough sessions exist to measure |
| Storage | Keep bundled content modest; no large downloads required to start playing |
| Battery | No background polling, persistent service, or unnecessary wake locks |
| Layout | Usable on narrow phones and larger screens; handle system bars, cutouts and font scaling |
| Recovery | App handles ad failure, process recreation, empty storage and invalid puzzle resources gracefully |

# 13. Analytics and success metrics

## 13.1 Measurement approach

Use Google Play Console and AdMob reports first. Do not add a separate analytics SDK in v1 unless there is a clear measurement need and its privacy impact is understood. Game telemetry, if added later, should use a documented event plan and be reflected in privacy disclosures.

## 13.2 Primary product metrics

- Installs and store conversion where available.
- Daily active users (DAU) and monthly active users (MAU).
- Day-1 and Day-7 retention.
- Sessions per active user and rounds per session.
- Completion rates by game.
- Number Merge replay rate and best-score improvement.
- Memory Match completion time/moves distribution.
- Quick Math score distribution and early exits.
- Word Search puzzle completion rate.
- Banner impressions and rewarded-ad show/reward rates.
- Crash-free users/sessions and ANR rate.

## 13.3 Initial targets (hypotheses, not promises)

- Day-1 retention: 30% or better as an initial diagnostic target.
- Day-7 retention: 10% or better as an initial diagnostic target.
- Crash-free sessions: 99% or better once sample size is meaningful.
- Four games can each be started and completed without a network connection.
- No reward can be granted by ad failure or by repeatedly tapping the reward CTA.

Use these as prompts for investigation, not universal success thresholds. A small sample does not support strong conclusions. Do not optimize ads at the expense of retention or user trust.

# 14. Acceptance criteria and test plan

## 14.1 Functional acceptance criteria

- ☐ Home screen clearly presents all four games and Today's Pick.
- ☐ Each game can be launched, played, completed or exited safely, and replayed.
- ☐ Each game's documented rules pass unit tests for normal, boundary, and invalid inputs.
- ☐ Scores/progress/preferences persist across app restarts where specified.
- ☐ Offline mode does not disable core game features.
- ☐ Back/pause/restart flows do not accidentally discard a round without warning.
- ☐ Daily Pick rotates by date and completion/streak rules work across app restart.
- ☐ Banner placement does not overlap controls or cause accidental-click layouts.
- ☐ Rewarded ads can be declined; failures leave the game usable.
- ☐ Rewards are granted only once after the documented reward callback.
- ☐ Debug builds use test ad units; release build configuration uses the intended production IDs and privacy setup.
- ☐ Privacy Policy is accessible from settings and Play Console.
- ☐ All target audience, content rating, Data safety and ads declarations are complete and accurate.
- ☐ Target SDK meets the current Play Console requirement at submission time.

## 14.2 Game logic unit tests

**Number Merge:** merge logic, no-op swipes, spawn rules, score updates, 2048 milestone, move availability and game-over detection. Inject deterministic random values for tests.

**Memory Match:** deck contains pairs, card tap transitions, mismatch timing, matched-card locking, move count, and completion state.

**Quick Math:** generated answer is present, distractors are unique, division is exact, timer pause/resume, zero-time result, and scoring boundaries.

**Word Search:** every target word validates against board and direction, gesture-to-cell conversion, duplicate selections, hint reward state, and completion state.

## 14.3 Manual device test matrix

- Lowest supported Android API device/emulator.
- Current Android version/API 36 device or emulator.
- One low-memory or entry-level phone if available.
- Narrow screen and large screen/font scaling.
- Portrait orientation and system font scale increased.
- Airplane mode, unstable network, ad unavailable, ad dismissed, app background/foreground during gameplay.
- Process death/relaunch during and after a game.
- TalkBack basic navigation on home, settings, results and key gameplay controls.

## 14.4 Advertising test cases

- Ad loads successfully in test mode.
- Ad fails to load; CTA remains safe.
- User declines to watch; progress remains unchanged.
- User watches and earns a reward; reward applies exactly once.
- User closes early without qualifying; no reward.
- Double taps do not create duplicate requests or rewards.
- Consent required: ads are not requested before the required consent flow has completed.
- No live ad is clicked by the developer/testers during testing [R3, R4].

# 15. Delivery roadmap

The schedule depends on programming experience and available hours. Treat the weeks below as a planning sequence, not a fixed commitment.

| Phase | Focus | Exit criteria |
|---|---|---|
| 0. Setup | Create project, Git repo, theme, navigation shell, target SDK | App installs and opens on device/emulator |
| 1. Vertical slice | Build Number Merge from screen to save | One game is playable end-to-end and covered by unit tests |
| 2. Core collection | Add Memory Match, Quick Math and Word Search | Four games work offline with result screens |
| 3. Shared polish | Add instructions, home, Today's Pick, settings, accessibility | Unified product behavior and persistence |
| 4. Ads/privacy | Integrate current ad SDK and consent, test ads, privacy policy draft | All ad states tested and disclosures are accurate |
| 5. Release QA | Test devices, fix crashes, create assets/store listing, produce signed AAB | Release candidate passes checklist |
| 6. Testing and launch | Internal test, closed test if required, production-access application/review | Approved and published release |
| 7. Learn | Monitor stability, retention and ads; collect feedback | Evidence-based v1.1 priority list |

Recommended order: implement and test game logic before connecting ads. Ads should be added after the core game is enjoyable and stable.

# 16. Launch and store checklist

## 16.1 Developer/account costs

The code editor, Kotlin, Jetpack Compose and core development tools can be used without software subscription costs. However, Google Play developer registration is currently listed as a one-time US$25 fee [R9]. A new personal developer account created after November 13, 2023 must meet the current closed testing requirement: at least 12 testers opted in continuously for 14 days before applying for production access [R10]. Check the policy again in Play Console because requirements can change.

The actual total is therefore **not guaranteed to be ₹0**. This PRD's ₹0 budget means no paid software or contracted development, and assumes a computer, test phone, internet, and required publishing costs are handled separately.

## 16.2 Store assets and listing

- Final adaptive launcher icon and Play Store high-resolution icon asset.
- Phone screenshots showing real game screens.
- App name: Puzzivo (working title until final legal/store checks).
- Short description and long description with no misleading earning/health claims.
- Support contact and public privacy policy URL.
- Content rating questionnaire.
- Target audience declaration that reflects actual audience.
- Ads declaration and accurate Data safety form.
- Signed AAB using a secure keystore.
- App tested on current supported Android versions.
- Ensure package ID is final before the first public release.
- Check app name, domain and relevant trademarks. Finding only one Play Store result does not itself establish trademark or commercial clearance.

## 16.3 Play submission requirements

As of the date of this document, Google Play's target API policy says that starting August 31, 2026, new apps and app updates must target Android 16/API 36 or higher [R8]. Confirm the requirement shown by Play Console at the time of submission. If the developer account is subject to the personal-account closed testing rule, plan the 12 testers and uninterrupted 14-day test window [R10].

# 17. Risks, assumptions and open decisions

## 17.1 Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Four games extend development time | Delayed launch or inconsistent quality | Build one complete game first, then reuse shared infrastructure and add others sequentially |
| Low organic discoverability | Few installs and little ad revenue | Create short gameplay clips, improve store listing, share with relevant communities, avoid paid acquisition before retention is known |
| Players don't return | Low monetization potential | Improve puzzle feedback, difficulty curve and daily pick; prioritize retention over ad volume |
| Ad integration changes or SDK incompatibility | Build/release delays | Use current official docs, pin tested versions, keep ad code isolated, use test ads |
| Policy rejection | Launch delay | Complete target audience, ads, content rating, Data safety and privacy review before submission |
| Privacy mismatch due to SDK data collection | Compliance and trust risk | Audit SDK disclosure, privacy policy, consent and Data safety form before each release |
| App-name/trademark conflict | Rebranding cost | Conduct additional name, domain and trademark checks before investing in brand assets |
| Puzzle bugs/repeated content | Poor reviews | Unit tests, deterministic generation, validate every bundled Word Search puzzle |

## 17.2 Assumptions

- One developer builds the first version.
- An existing computer and Android test device are available.
- English is the first language.
- No server is required for v1.
- The app is free to play and uses ads only.
- No user accounts, online leaderboards or in-app purchases are needed.
- Starter Word Search content is included in the app package.

## 17.3 Decisions required before public release

1. Confirm the permanent Android package ID.
2. Finalize target age group truthfully and review the matching ad/Families policy implications.
3. Create and host a privacy policy with a real developer contact.
4. Register the app in AdMob and configure production ad units only after test-mode behavior is correct.
5. Confirm whether Number Merge's undo feature is stable enough to support a rewarded ad; otherwise omit this placement.
6. Verify final name/domain/trademark availability.
7. Finalize the launcher icon assets and store screenshots.
8. Confirm current Play Console target SDK and testing rules at submission.

# 18. Official references

These links are maintained by Google/Android documentation owners. Review them again during implementation and before release because SDK versions and publishing policies can change.

- **[R1] Google Play — User Data policy, Data safety and privacy policy requirements:** https://support.google.com/googleplay/android-developer/answer/10144311
- **[R2] Google Play — Data safety overview:** https://support.google.com/googleplay/android-developer/answer/10787469
- **[R3] Google AdMob — GMA Next-Gen SDK setup for Android:** https://developers.google.com/admob/android/next-gen/quick-start
- **[R4] Google AdMob — Rewarded ads and UMP consent:** https://developers.google.com/admob/android/next-gen/rewarded and https://developers.google.com/admob/android/privacy
- **[R5] Google Play — Families policies:** https://support.google.com/googleplay/android-developer/answer/9893335
- **[R6] Google Play — Target audience/content and ratings:** https://support.google.com/googleplay/android-developer/answer/9859655
- **[R7] Android Developers — Jetpack Compose architecture and state:** https://developer.android.com/develop/ui/compose/architecture
- **[R8] Google Play — Target API level requirements:** https://support.google.com/googleplay/android-developer/answer/11926878
- **[R9] Google Play Console — Developer account registration:** https://support.google.com/googleplay/android-developer/answer/6112435
- **[R10] Google Play Console — Testing requirements for new personal developer accounts:** https://support.google.com/googleplay/android-developer/answer/14151465
