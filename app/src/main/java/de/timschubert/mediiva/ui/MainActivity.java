package de.timschubert.mediiva.ui;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.library.LibraryDao;
import de.timschubert.mediiva.databinding.ActivityMainBinding;
import de.timschubert.mediiva.ui.home.HomeFragment;
import de.timschubert.mediiva.ui.imagesetlibrary.ImageSetLibraryFragment;
import de.timschubert.mediiva.ui.libraries.LibrariesFragment;
import de.timschubert.mediiva.ui.movielibrary.MovieLibraryFragment;

public class MainActivity extends AppCompatActivity
{

    private ActivityMainBinding binding;
    private MyDrawerSlider drawerSlider;
    private List<Library> allLibraries;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        allLibraries = new ArrayList<>();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.activityMainAppBarMain.appBarMainToolbar);

        drawerSlider = new MyDrawerSlider(this,
                binding.activityMainDrawerLayout,
                binding.activityMainAppBarMain.appBarMainToolbar,
                binding.activityMainDrawerSliderView,
                new MyDrawerSlider.Callback()
                {
                    @Override
                    public void onHomePressed() {
                        changeFragment(new HomeFragment(), false);
                    }

                    @Override
                    public void onLibrariesPressed() {
                        changeFragment(new LibrariesFragment(), true);
                    }

                    @Override
                    public void onMediaLibraryPressed(long id) {
                        onDrawerLibraryClicked(id);
                    }
        });

        long restoredId = -1;
        if(savedInstanceState != null) restoredId = savedInstanceState.getLong("key_menu_id", -1);
        drawerSlider.queryLibrariesAsync(this, restoredId);

        AppExecutors.getInstance().diskIO().execute(this::queryExistingLibraries);
    }

    private void queryExistingLibraries()
    {
        LibraryDao libraryDao = AppDatabase.getInstance(this).libraryDao();
        allLibraries = libraryDao.getLibraries();
    }

    private void onDrawerLibraryClicked(long id)
    {
        for(Library library : allLibraries)
        {
            if(id == library.getId())
            {
                Fragment libraryFragment = library.getType() == Library.Type.MOVIE ? new MovieLibraryFragment() : new ImageSetLibraryFragment();
                Bundle args = new Bundle();
                args.putLong("key_id", library.getId());
                libraryFragment.setArguments(args);
                changeFragment(libraryFragment, true);
                return;
            }
        }
    }

    private void changeFragment(Fragment fragment, boolean addToBackStack)
    {
        if(fragment == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_main_nav_host, fragment);
        //if(addToBackStack){ transaction.addToBackStack(null); } TODO
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong("key_menu_id", drawerSlider.getLastPressedId()); //TODO
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu_library, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.toolbar_menu_library_search).getActionView();
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                ViewerMain.getInstance(MainActivity.this).onSearchQueryChange(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.toolbar_menu_library_search)
        {
            AutoTransition transition = new AutoTransition();
            transition.setDuration(100);

            TransitionManager.beginDelayedTransition(binding.activityMainAppBarMain.appBarMainToolbar, transition);
            item.expandActionView();
            return true;
        }

        return drawerSlider.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerSlider.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerSlider.onPostCreate();
    }
}
