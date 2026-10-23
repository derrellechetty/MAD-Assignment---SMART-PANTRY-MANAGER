package com.example.smartpantry.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Pre-loads the recipe collection the first time the database is created.
 * Ingredients are written as "name:quantity:unit". Solids use g, liquids ml/tbsp/tsp,
 * and countable things use pcs (cloves, slices, cans are all counted in pcs).
 */
final class RecipeSeeder {

    private RecipeSeeder() { }

    static void seed(SQLiteDatabase db) {
        add(db, "Scrambled Eggs", 10, "Soft, creamy eggs on the stove.",
                "Crack the eggs into a bowl and whisk with the salt.\n"
                        + "Melt the butter in a pan over low heat.\n"
                        + "Pour in the eggs and stir slowly until just set.\n"
                        + "Serve straight away.",
                "eggs:2:pcs", "butter:10:g", "salt:0.5:tsp");

        add(db, "Cheese Omelette", 12, "A fluffy omelette with melted cheese.",
                "Whisk the eggs with the salt.\n"
                        + "Melt the butter in a non-stick pan.\n"
                        + "Pour in the eggs and cook until the edges set.\n"
                        + "Sprinkle over the cheese, fold in half and cook 1 more minute.",
                "eggs:3:pcs", "cheese:50:g", "butter:10:g", "salt:0.5:tsp");

        add(db, "Tomato Pasta", 25, "Simple pasta with a fresh tomato sauce.",
                "Boil the pasta in salted water until tender.\n"
                        + "Heat the olive oil and fry the crushed garlic for 30 seconds.\n"
                        + "Add the chopped tomatoes and simmer for 10 minutes.\n"
                        + "Toss the drained pasta through the sauce and serve.",
                "pasta:200:g", "tomatoes:4:pcs", "garlic:2:pcs", "olive oil:2:tbsp", "salt:1:tsp");

        add(db, "Egg Fried Rice", 20, "Leftover rice turned into a quick dinner.",
                "Heat the oil in a large pan and fry the chopped onion until soft.\n"
                        + "Push the onion aside, scramble the eggs in the pan.\n"
                        + "Add the cooked rice and soy sauce.\n"
                        + "Stir-fry on high heat for 3-4 minutes and serve.",
                "rice:200:g", "eggs:2:pcs", "onion:1:pcs", "soy sauce:2:tbsp", "vegetable oil:1:tbsp");

        add(db, "Fluffy Pancakes", 25, "Classic breakfast pancakes.",
                "Mix the flour, sugar and baking powder in a bowl.\n"
                        + "Whisk in the milk and egg until you have a smooth batter.\n"
                        + "Melt a little butter in a pan over medium heat.\n"
                        + "Pour in small ladles of batter and flip when bubbles appear.",
                "flour:150:g", "milk:250:ml", "eggs:1:pcs", "sugar:1:tbsp", "butter:20:g",
                "baking powder:1:tsp");

        add(db, "Creamy Mashed Potatoes", 30, "Smooth buttery mash.",
                "Peel and chop the potatoes, then boil in salted water for 15-20 minutes.\n"
                        + "Drain well and return to the pot.\n"
                        + "Add butter and warm milk and mash until smooth.\n"
                        + "Season with the salt.",
                "potatoes:500:g", "butter:30:g", "milk:100:ml", "salt:1:tsp");

        add(db, "Cheese Toastie", 8, "Golden, crunchy and melty.",
                "Butter one side of each slice of bread.\n"
                        + "Place the cheese between two slices, butter sides out.\n"
                        + "Toast in a pan for 3 minutes per side until golden.",
                "bread:4:pcs", "cheese:80:g", "butter:20:g");

        add(db, "French Toast", 15, "Sweet eggy bread.",
                "Whisk the eggs, milk, sugar and cinnamon in a shallow dish.\n"
                        + "Dip each slice of bread so both sides are soaked.\n"
                        + "Fry in melted butter until golden on both sides.",
                "bread:4:pcs", "eggs:2:pcs", "milk:100:ml", "sugar:1:tbsp", "butter:10:g",
                "cinnamon:1:tsp");

        add(db, "Chicken Rice Bowl", 35, "Pan-fried chicken over fluffy rice.",
                "Cook the rice according to the packet.\n"
                        + "Slice the chicken and season with salt.\n"
                        + "Fry the onion and garlic in oil, add the chicken and cook through.\n"
                        + "Serve the chicken over the rice.",
                "chicken breast:300:g", "rice:200:g", "onion:1:pcs", "garlic:2:pcs",
                "vegetable oil:1:tbsp", "salt:1:tsp");

        add(db, "Roasted Tomato Soup", 40, "Warming soup from pantry staples.",
                "Fry the onion and garlic in the olive oil until soft.\n"
                        + "Add the chopped tomatoes and stock and simmer for 20 minutes.\n"
                        + "Blend until smooth and season with the salt.",
                "tomatoes:6:pcs", "onion:1:pcs", "garlic:2:pcs", "vegetable stock:500:ml",
                "olive oil:1:tbsp", "salt:1:tsp");

        add(db, "Spaghetti Aglio e Olio", 20, "Garlic, oil and chilli - the Italian classic.",
                "Cook the spaghetti in salted water.\n"
                        + "Gently fry sliced garlic in the olive oil until pale gold.\n"
                        + "Add the chilli flakes, then toss in the drained spaghetti.\n"
                        + "Season with the salt and serve.",
                "spaghetti:200:g", "garlic:4:pcs", "olive oil:4:tbsp", "chilli flakes:1:tsp",
                "salt:1:tsp");

        add(db, "Easy Guacamole", 10, "Creamy avocado dip.",
                "Halve the avocados and scoop the flesh into a bowl.\n"
                        + "Mash with a fork, then stir in the diced tomato and onion.\n"
                        + "Squeeze in the lime juice and season with the salt.",
                "avocados:2:pcs", "tomatoes:1:pcs", "onion:1:pcs", "lime:1:pcs", "salt:0.5:tsp");

        add(db, "Banana Oat Pancakes", 15, "Three-ingredient healthy pancakes.",
                "Mash the bananas in a bowl.\n"
                        + "Stir in the eggs and oats to make a thick batter.\n"
                        + "Cook spoonfuls in a non-stick pan for 2 minutes per side.",
                "bananas:2:pcs", "eggs:2:pcs", "oats:60:g");

        add(db, "Veggie Stir-Fry", 20, "Crunchy vegetables in a soy-garlic sauce.",
                "Heat the oil in a wok over high heat.\n"
                        + "Add the sliced carrots and broccoli and stir-fry for 3 minutes.\n"
                        + "Add the bell pepper and garlic and cook 2 more minutes.\n"
                        + "Pour in the soy sauce, toss and serve.",
                "carrots:2:pcs", "bell pepper:1:pcs", "broccoli:200:g", "soy sauce:2:tbsp",
                "garlic:2:pcs", "vegetable oil:1:tbsp");

        add(db, "Cheesy Baked Potatoes", 60, "Crispy skins with a melted cheese topping.",
                "Heat the oven to 200 C and prick the potatoes with a fork.\n"
                        + "Rub with butter and salt and bake for 45-50 minutes.\n"
                        + "Split open, top with cheese and return to the oven for 5 minutes.",
                "potatoes:4:pcs", "cheese:100:g", "butter:20:g", "salt:1:tsp");

        add(db, "Egg Fried Noodles", 15, "Fast noodles with spring onion.",
                "Cook the noodles and drain.\n"
                        + "Fry the chopped green onions in the oil for 1 minute.\n"
                        + "Scramble in the eggs, add the noodles and soy sauce and toss well.",
                "noodles:200:g", "eggs:2:pcs", "soy sauce:2:tbsp", "green onions:2:pcs",
                "vegetable oil:1:tbsp");

        add(db, "Tuna Pasta", 20, "A storecupboard supper.",
                "Cook the pasta in salted water.\n"
                        + "Warm the olive oil with the garlic, then flake in the tuna.\n"
                        + "Toss with the pasta and a squeeze of lemon juice.",
                "pasta:200:g", "tuna:1:pcs", "olive oil:1:tbsp", "garlic:1:pcs", "lemon:1:pcs");

        add(db, "Banana Oatmeal", 10, "Warm porridge sweetened with banana and honey.",
                "Simmer the oats and milk for 5 minutes, stirring often.\n"
                        + "Slice the banana on top and drizzle with honey.",
                "oats:80:g", "milk:250:ml", "bananas:1:pcs", "honey:1:tbsp");

        add(db, "Hearty Lentil Soup", 45, "Filling, cheap and good for leftovers.",
                "Fry the diced onion, carrots and garlic until soft.\n"
                        + "Add the lentils, cumin and stock.\n"
                        + "Simmer for 30 minutes until the lentils are tender.",
                "lentils:200:g", "carrots:2:pcs", "onion:1:pcs", "garlic:2:pcs",
                "vegetable stock:750:ml", "cumin:1:tsp");

        add(db, "Black Bean Quesadilla", 15, "Crispy tortillas with beans and cheese.",
                "Mash the black beans roughly with a fork.\n"
                        + "Spread over one tortilla, top with cheese and the second tortilla.\n"
                        + "Fry for 2-3 minutes per side until crisp and melted.",
                "tortillas:2:pcs", "black beans:150:g", "cheese:100:g");

        add(db, "Quick Hummus", 10, "Smooth chickpea dip.",
                "Drain the chickpeas.\n"
                        + "Blend with the tahini, lemon juice, garlic and olive oil.\n"
                        + "Add a splash of water until smooth.",
                "chickpeas:400:g", "tahini:2:tbsp", "lemon:1:pcs", "garlic:1:pcs",
                "olive oil:3:tbsp");
    }

    private static void add(SQLiteDatabase db, String name, int minutes, String description,
                            String steps, String... ingredients) {
        ContentValues r = new ContentValues();
        r.put(PantryDbHelper.COL_R_NAME, name);
        r.put(PantryDbHelper.COL_R_DESC, description);
        r.put(PantryDbHelper.COL_R_STEPS, steps);
        r.put(PantryDbHelper.COL_R_MINUTES, minutes);
        long recipeId = db.insert(PantryDbHelper.TABLE_RECIPES, null, r);

        for (String spec : ingredients) {
            String[] p = spec.split(":");
            ContentValues c = new ContentValues();
            c.put(PantryDbHelper.COL_RI_RECIPE_ID, recipeId);
            c.put(PantryDbHelper.COL_RI_NAME, p[0]);
            c.put(PantryDbHelper.COL_RI_QTY, Double.parseDouble(p[1]));
            c.put(PantryDbHelper.COL_RI_UNIT, p[2]);
            db.insert(PantryDbHelper.TABLE_RECIPE_INGREDIENTS, null, c);
        }
    }
}
