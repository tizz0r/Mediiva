package de.timschubert.mediiva.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView;

import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.library.LibraryDao;

public class MyDrawerSlider
{

    private static final long HOME_ID = 483242429L; //TODO resource
    private static final long LIBRARIES_ID = 3982749832L; //TODO resource

    private long lastPressedId;
    private final ActionBarDrawerToggle drawerToggle;
    private final MaterialDrawerSliderView sliderView;
    private final Callback callback;

    public MyDrawerSlider(@NonNull AppCompatActivity activity,
                          @NonNull DrawerLayout drawerLayout,
                          @NonNull Toolbar toolbar,
                          @NonNull MaterialDrawerSliderView sliderView,
                          @NonNull Callback callback)
    {
        this.callback = callback;

        drawerToggle = new ActionBarDrawerToggle(activity,
                drawerLayout,
                toolbar,
                com.mikepenz.materialdrawer.R.string.material_drawer_open,
                com.mikepenz.materialdrawer.R.string.material_drawer_close);

        if(activity.getSupportActionBar() != null)
        {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setHomeButtonEnabled(true);
        }

        lastPressedId = -1;

        PrimaryDrawerItem homeItem = new PrimaryDrawerItem();
        homeItem.setName(new StringHolder("Home")); //TODO
        homeItem.setIcon(new ImageHolder(R.drawable.ic_home));
        homeItem.setIdentifier(HOME_ID); //TODO

        PrimaryDrawerItem librariesItem = new PrimaryDrawerItem();
        librariesItem.setName(new StringHolder("Libraries")); //TODO
        librariesItem.setIcon(new ImageHolder(R.drawable.ic_bookmark));
        librariesItem.setIdentifier(LIBRARIES_ID); //TODO

        this.sliderView = sliderView;
        sliderView.getItemAdapter().add(homeItem, librariesItem);
        sliderView.setOnDrawerItemClickListener((view, drawerItem, integer) ->
        {
            onDrawerItemClicked(drawerItem.getIdentifier());
            return false;
        });
    }

    public void queryLibrariesAsync(Context context, long restoredId)
    {
        AppExecutors.getInstance().diskIO().execute(() ->
        {
            LibraryDao libraryDao = AppDatabase.getInstance(context).libraryDao();
            List<Library> allLibraries = libraryDao.getLibraries();
            AppExecutors.getInstance().mainThread().execute(() ->
                    addLibrariesToUi(allLibraries, restoredId));
        });
    }

    private void addLibrariesToUi(List<Library> allLibraries, long restoredId)
    {
        if(allLibraries.isEmpty()) return;
        sliderView.getItemAdapter().add(new DividerDrawerItem());

        for(Library library: allLibraries)
        {
            PrimaryDrawerItem libraryItem = new PrimaryDrawerItem();
            libraryItem.setName(new StringHolder(library.getName()));
            libraryItem.setDescription(new StringHolder(library.getSmbShare()));
            libraryItem.setIdentifier(library.getId());

            switch (library.getType())
            {
                default:
                case IMAGE_SET:
                    libraryItem.setIcon(new ImageHolder(R.drawable.ic_imageset));
                    break;
                case MOVIE:
                    libraryItem.setIcon(new ImageHolder(R.drawable.ic_movie));
            }

            sliderView.getItemAdapter().add(libraryItem);
        }

        lastPressedId = restoredId;
        if(restoredId != -1) sliderView.setSelection(restoredId, false);
        else sliderView.setSelection(HOME_ID, true);
    }

    private void onDrawerItemClicked(long itemId)
    {
        lastPressedId = itemId;
        if(itemId == HOME_ID) callback.onHomePressed();
        else if(itemId == LIBRARIES_ID) callback.onLibrariesPressed();
        else callback.onMediaLibraryPressed(itemId);
    }

    public void onPostCreate() { drawerToggle.syncState(); }
    public void onConfigurationChanged(Configuration c) { drawerToggle.onConfigurationChanged(c); }
    public boolean onOptionsItemSelected(MenuItem i) { return drawerToggle.onOptionsItemSelected(i); }

    public long getLastPressedId() { return lastPressedId; }

    public interface Callback
    {
        void onHomePressed();
        void onLibrariesPressed();
        void onMediaLibraryPressed(long id);
    }
}
