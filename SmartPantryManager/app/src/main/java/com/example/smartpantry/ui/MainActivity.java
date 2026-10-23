package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.PantryDbHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen (launcher). READs all items from SQLite into a RecyclerView,
 * launches Add/Edit via Intents, and DELETEs after a confirmation dialog.
 *
 * Lifecycle: the list is reloaded in onResume() so it is always fresh after returning
 * from Add/Edit or Settings - no manual refresh code needed anywhere else.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    /** Intent extra: id of the item to edit (absent = add mode). */
    public static final String EXTRA_ITEM_ID = "item_id";

    private PantryDbHelper db;
    private PantryAdapter adapter;
    // after
    private View tvEmpty;          // empty-state container (a LinearLayout in the layout)
    private TextView tvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        db = PantryDbHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_pantry);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add) {
                openAdd();
                return true;
            }
            return false;
        });

        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvBanner);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> openAdd());

        NavHelper.setup(this, R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
        // Keep the nav highlight right when returning via the back stack.
        ((com.google.android.material.bottomnavigation.BottomNavigationView)
                findViewById(R.id.bottomNav)).getMenu().findItem(R.id.nav_pantry).setChecked(true);
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        adapter.setItems(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Expiring-soon banner (Settings toggle)
        int count = 0;
        if (AppSettings.expiringAlertsEnabled(this)) {
            long now = System.currentTimeMillis();
            for (PantryItem i : items) {
                if (RecipeMatcher.isExpiringSoon(i, now, AppSettings.EXPIRING_SOON_DAYS)) count++;
            }
        }
        View cardBanner = findViewById(R.id.cardBanner);
        if (count > 0) {
            tvBanner.setText(getString(R.string.banner_expiring, count));
            if (cardBanner != null) cardBanner.setVisibility(View.VISIBLE);
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            if (cardBanner != null) cardBanner.setVisibility(View.GONE);
            tvBanner.setVisibility(View.GONE);
        }
    }

    private void openAdd() {
        startActivity(new Intent(this, AddEditItemActivity.class));
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, item.getId());   // data passed via Intent extra
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(item.getName())
                .setPositiveButton(R.string.yes, (d, w) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
