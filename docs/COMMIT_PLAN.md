# Suggested commit plan (do these yourself, spread over several days)

The brief requires >=10 genuine, incremental commits and checks the history against your video
and report. Make them as you actually work through the project - do not make them all at once.
Suggested order (each is a real, buildable step):

1. Create project, Gradle config, .gitignore, README skeleton
2. Add model classes (PantryItem, Recipe, RecipeIngredient)
3. Add PantryDbHelper with table creation and pantry CRUD
4. Add RecipeSeeder with the recipe collection and recipe queries
5. Add UnitConverter and IngredientNormalizer
6. Implement RecipeMatcher strict matching
7. Add RecipeMatcherTest unit tests
8. Add theme, colours, strings, drawables
9. Pantry list screen + PantryAdapter
10. Add/Edit ingredient screen with validation
11. Suggested Recipes screen + RecipeAdapter, empty state
12. Recipe detail screen
13. Settings screen + bottom navigation
14. "Almost There" tab, expiring-soon banner
15. Bug fixes found while testing; final README

Then: `git remote add origin <your PUBLIC repo url>` and `git push -u origin main`.
