package com.example.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite storage for the whole app (SQLiteOpenHelper).
 *
 * Tables
 *   pantry_items        - what the user owns (full CRUD from the UI)
 *   recipes             - seeded on first run
 *   recipe_ingredients  - one row per ingredient line, FK -> recipes (cascade delete)
 *
 * The database file lives in the app's private storage, so data survives closing and
 * re-opening the app (persistent storage requirement).
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // pantry_items
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QTY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_millis";

    // recipes
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_DESC = "description";
    public static final String COL_R_STEPS = "steps";
    public static final String COL_R_MINUTES = "prep_minutes";

    // recipe_ingredients
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static PantryDbHelper instance;

    /** Single shared helper so we never open two connections to the same file. */
    public static synchronized PantryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PantryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PantryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_R_NAME + " TEXT NOT NULL, "
                + COL_R_DESC + " TEXT, "
                + COL_R_STEPS + " TEXT NOT NULL, "
                + COL_R_MINUTES + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RI_RECIPE_ID + " INTEGER NOT NULL REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE, "
                + COL_RI_NAME + " TEXT NOT NULL, "
                + COL_RI_QTY + " REAL NOT NULL, "
                + COL_RI_UNIT + " TEXT NOT NULL)");

        // First run: pre-load the recipe collection (all-or-nothing in one transaction).
        db.beginTransaction();
        try {
            RecipeSeeder.seed(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 only. A real upgrade path would ALTER tables here.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // =================================================================== CREATE

    /** Inserts a pantry item and returns its new row id (-1 on failure). */
    public long insertItem(PantryItem item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    // ===================================================================== READ

    /** All pantry items, A-Z. */
    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) items.add(itemFromCursor(c));
        }
        return items;
    }

    /** One pantry item or null if it does not exist. */
    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? itemFromCursor(c) : null;
        }
    }

    // =================================================================== UPDATE

    /** Updates an existing pantry item; returns number of rows changed. */
    public int updateItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item), COL_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    // =================================================================== DELETE

    public int deleteItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int deleteAllItems() {
        return getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    // ================================================================== RECIPES

    /** Every recipe with its ingredients attached (2 queries, no N+1). */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Map<Long, Recipe> byId = new HashMap<>();
        List<Recipe> recipes = new ArrayList<>();

        try (Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_R_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                Recipe r = recipeFromCursor(c);
                recipes.add(r);
                byId.put(r.getId(), r);
            }
        }
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, null, null, null, null,
                COL_ID + " ASC")) {
            while (c.moveToNext()) {
                Recipe r = byId.get(c.getLong(c.getColumnIndexOrThrow(COL_RI_RECIPE_ID)));
                if (r != null) r.getIngredients().add(ingredientFromCursor(c));
            }
        }
        return recipes;
    }

    /** One recipe with ingredients, or null. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Recipe recipe;
        try (Cursor c = db.query(TABLE_RECIPES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            recipe = recipeFromCursor(c);
        }
        try (Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, COL_ID + " ASC")) {
            while (c.moveToNext()) recipe.getIngredients().add(ingredientFromCursor(c));
        }
        return recipe;
    }

    // ================================================================== HELPERS

    private static ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put(COL_NAME, item.getName());
        v.put(COL_QTY, item.getQuantity());
        v.put(COL_UNIT, item.getUnit());
        v.put(COL_EXPIRY, item.getExpiryMillis());
        return v;
    }

    private static PantryItem itemFromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_UNIT)),
                c.getLong(c.getColumnIndexOrThrow(COL_EXPIRY)));
    }

    private static Recipe recipeFromCursor(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_R_DESC)),
                c.getString(c.getColumnIndexOrThrow(COL_R_STEPS)),
                c.getInt(c.getColumnIndexOrThrow(COL_R_MINUTES)));
    }

    private static RecipeIngredient ingredientFromCursor(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT)));
    }
}
