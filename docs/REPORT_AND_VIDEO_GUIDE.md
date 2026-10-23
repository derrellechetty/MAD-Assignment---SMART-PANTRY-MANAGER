# Report, screenshots and video checklist (things only you can do)

## Screenshots to capture from the running app
Empty pantry | Add form | each validation error (empty name, bad quantity) | pantry list with items |
expiring/expired colouring | edit screen | delete confirmation | Suggested (empty message) |
Suggested with results | Almost There tab | Recipe detail | Settings | after-restart persistence.

## Key code snippets (pick 3-5)
1. `RecipeMatcher.strictMatches / findMissing` - the core rule
2. `IngredientNormalizer.normalize` + `UnitConverter.toBase` - robustness
3. `PantryDbHelper.onCreate` + `insertItem/updateItem/deleteItem` - SQLite CRUD
4. `PantryAdapter.onBindViewHolder` - RecyclerView/ViewHolder
5. `AddEditItemActivity.validate` - input validation

## Challenge ideas (use only ones that genuinely happened to you)
Plural/singular mismatches; unit conversion between kg/g and cups/ml; list not refreshing after
returning from the edit screen (fixed by reloading in onResume); bottom-nav back stack.

## Video (5-7 min, narrated)
~1 min GitHub history | 2-3 min live CRUD + add/remove one ingredient to show a recipe
appear/disappear + close and reopen app | 2-3 min explain three concepts with code open |
30-60 s why SQLite. Export MP4 (H.264), keep the ZIP under 50 MB.

Submission name: Studentnumber_Surname_MobileAppDev700_Assignment.zip
