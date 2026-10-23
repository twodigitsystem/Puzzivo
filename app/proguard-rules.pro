# Puzzivo Proguard / R8 Keep Rules
# Note: Google Mobile Ads (AdMob) & UMP SDKs bundle consumer keep rules automatically in their AARs.

# Keep local data models used in state & preference persistence
-keep class com.tds.puzzivo.core.model.GameResult { *; }
-keep class com.tds.puzzivo.core.model.DailyPick { *; }
-keep class com.tds.puzzivo.core.storage.UserPreferences { *; }
