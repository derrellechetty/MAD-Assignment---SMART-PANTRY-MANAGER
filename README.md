# Smart Pantry Manager

An Android app (Java) that helps reduce food waste. The user records the ingredients they
already have at home and the app suggests **only** the recipes they can cook right now -
no shopping trip required.

Mobile App Development 700 - Practical Assignment.

## Features
- Pantry CRUD: add, edit, delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list (RecyclerView + custom adapter) with expired / expiring-soon colouring
- 21 recipes pre-loaded into the database on first run
- **Suggested Recipes** screen using the strict-matching rule (see below)
- Optional "Almost there" tab (recipes missing exactly one ingredient), kept separate
- Recipe detail screen with tick/cross per ingredient and numbered method
- Settings: expiring-soon banner, ignore expired items, default unit
- Friendly empty states, input validation, bottom navigation bar
- No Google Maps, no location/GPS permissions

## Strict-matching rule
A recipe is suggested only if **every** ingredient is in the pantry in at least the required
quantity. Implemented in `logic/RecipeMatcher.java` (plain Java, unit-tested) with:
- name normalisation: case, plurals, filler words, synonyms (`tomato` = `Tomatoes`) - `IngredientNormalizer`
- unit conversion within mass / volume / count families (`1 kg` covers `500 g`) - `UnitConverter`
- duplicate pantry rows for the same ingredient are summed
- expired items optionally ignored

## Database choice: SQLite (SQLiteOpenHelper)
- Works fully offline, no accounts, backend or network permission needed
- The data (pantry + recipes + ingredients) is relational, which suits SQL tables and a foreign key
- Covered directly in the module's persistent-data chapter; the file lives in app-private storage so data survives restarts

Tables: `pantry_items`, `recipes`, `recipe_ingredients` (FK -> recipes, cascade delete).

## Project structure
```
app/src/main/java/com/example/smartpantry/
  model/   PantryItem, Recipe, RecipeIngredient
  data/    PantryDbHelper (CRUD), RecipeSeeder (21 recipes)
  logic/   RecipeMatcher, IngredientNormalizer, UnitConverter
  ui/      MainActivity, AddEditItemActivity, SuggestedRecipesActivity,
           RecipeDetailActivity, SettingsActivity, PantryAdapter, RecipeAdapter,
           NavHelper, AppSettings
app/src/test/  RecipeMatcherTest (19 JUnit tests)
```

## Setup / run
1. Install Android Studio (Hedgehog or newer) with Android SDK 34.
2. `File > Open` and select this folder; let Gradle sync (Android Studio creates the Gradle wrapper jar if missing).
3. Create an emulator (API 24+) or plug in a device with USB debugging on.
4. Press **Run**. Recipes are seeded automatically the first time.
5. Run the logic tests: right-click `app/src/test` > *Run Tests*, or `./gradlew test`.

## Try the strict rule
Add `eggs 2 pcs`, `butter 10 g`, `salt 1 tsp` -> *Scrambled Eggs* appears.
Delete the butter -> it disappears. Add it back -> it returns.
