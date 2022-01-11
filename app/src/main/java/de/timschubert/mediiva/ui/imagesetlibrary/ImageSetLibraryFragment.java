package de.timschubert.mediiva.ui.imagesetlibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.OLD.GalleryViewerActivity;
import de.timschubert.mediiva.OLD.RecyclerItemClickListener;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.library.LibraryDao;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.FragmentImagesetLibraryBinding;
import de.timschubert.mediiva.ui.adapter.ImageSetAdapter;

public class ImageSetLibraryFragment extends Fragment
{

    private Library library;

    private FragmentImagesetLibraryBinding binding;
    private ImageSetAdapter imageSetAdapter;
    private ViewerMain viewerMain;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentImagesetLibraryBinding.inflate(inflater, container, false);

        viewerMain = ViewerMain.getInstance(getContext());

        PopupMenu viewModeMenu = new PopupMenu(getContext(), binding.fragmentImagesetLibraryViewMode);
        viewModeMenu.getMenuInflater().inflate(R.menu.popup_imageset_library_view_mode, viewModeMenu.getMenu());
        viewModeMenu.setOnMenuItemClickListener(item ->
        {
            if (item.getItemId() == R.id.popup_imageset_library_view_mode_minimal)
            {
                changeViewMode(ImageSetAdapter.Style.MINIMAL);
                return true;
            }
            if(item.getItemId() == R.id.popup_imageset_library_view_mode_compact)
            {
                changeViewMode(ImageSetAdapter.Style.COMPACT);
                return true;
            }
            if(item.getItemId() == R.id.popup_imageset_library_view_mode_descriptive)
            {
                changeViewMode(ImageSetAdapter.Style.DESCRIPTIVE);
                return true;
            }

            return false;
        });

        binding.fragmentImagesetLibraryViewMode.setOnClickListener(view -> viewModeMenu.show());

        viewerMain.addCallback(new ViewerMain.Callback()
        {
            @Override
            public void onImageSetAdded(long imageSetId)
            {
                ImageSetLibraryFragment.this.onImageSetAdded(imageSetId);
            }

            @Override
            public void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap)
            {
                if(imageSetAdapter == null) return;
                AppExecutors.getInstance().mainThread().execute(() ->
                {
                    imageSetAdapter.addPoster(imageSet, posterBitmap);
                });
            }

            @Override public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap) {}
            @Override public void onMovieAdded(Movie movie) {}
            @Override public void onPosterLoaded(Movie movie, Bitmap posterBitmap) {}
            @Override public void onFanArtLoaded(Movie movie, Bitmap fanArt) {}
            @Override public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap) {}
            @Override public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap) {}
            @Override public void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap) {}
        });

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            AppDatabase database = AppDatabase.getInstance(getContext());

            if(getArguments() == null || getArguments().getLong("key_id", -1) == -1)
            {
                Log.e("mediiva.fragmentimagesetlibrary", "No id argument passed!");
                return;
            }

            long libraryId = getArguments().getLong("key_id");

            LibraryDao libraryDao = database.libraryDao();
            library = libraryDao.getLibraryById(libraryId);

            List<ImageSet> imageSets = database.imageSetDao().getImageSets();

            Set<ImageSet> duplicateSets = findDuplicates(imageSets);
            for(ImageSet imageSet : duplicateSets)
            {
                Log.w("mediiva.fragmentimagesetlibrary", "Duplicate set: "+imageSet.getTitle());
                database.imageSetDao().deleteImageSet(imageSet);
            }

            imageSetAdapter = new ImageSetAdapter(imageSets);
            imageSetAdapter.setStyle(ImageSetAdapter.Style.DESCRIPTIVE);

            RecyclerView recyclerView = binding.fragmentImagesetLibraryRecyclerView;
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setFlexWrap(FlexWrap.WRAP);
            layoutManager.setAlignItems(AlignItems.CENTER);
            layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);

            AppExecutors.getInstance().mainThread().execute(() ->
            {
                recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(View view, int position)
                    {
                        Intent intent = new Intent(getContext(), GalleryViewerActivity.class);
                        intent.putExtra("imageset_id", imageSetAdapter.getImageSetIdForPosition(position));
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {}
                }));
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(imageSetAdapter);
            });
        });

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        inflater.inflate(R.menu.options_media_library, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.options_media_library_search_new_files)
        {
            Log.i("mediiva.fragmentimagesetlibrary", "Searching library for new entries..");
            if(library != null) viewerMain.refreshImageSetLibrary(library);
            return true;
        }
        if(item.getItemId() == R.id.options_media_library_recreate_all)
        {
            Log.i("mediiva.fragmentimagesetlibrary", "Removing all entries and repopulating library");
            viewerMain.recreateTestLibraryImageSet(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeViewMode(ImageSetAdapter.Style style)
    {
        imageSetAdapter.setStyle(style);
        // Set the adapter again to avoid mixing layouts
        binding.fragmentImagesetLibraryRecyclerView.setAdapter(imageSetAdapter);
    }

    private Set<ImageSet> findDuplicates(List<ImageSet> imageSets)
    {
        final Set<ImageSet> duplicates = new HashSet<>();
        final Set<String> set1 = new HashSet<>();

        for(ImageSet imageSet : imageSets)
        {
            if(!set1.add(imageSet.getPath()))
            {
                duplicates.add(imageSet);
            }
        }

        return duplicates;
    }

    private void onImageSetAdded(long imageSetId)
    {
        ImageSet imageSet = AppDatabase.getInstance(getContext()).imageSetDao().getImageSetById(imageSetId);

        // TODO imageSetAdapter.addImageSet(imageSet);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}
