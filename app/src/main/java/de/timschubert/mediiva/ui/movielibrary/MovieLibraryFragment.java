package de.timschubert.mediiva.ui.movielibrary;

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

import java.util.List;

import de.timschubert.mediiva.AppExecutors;
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
import de.timschubert.mediiva.data.movie.MovieDao;
import de.timschubert.mediiva.databinding.FragmentImagesetLibraryBinding;
import de.timschubert.mediiva.ui.MovieOverviewActivity;
import de.timschubert.mediiva.ui.adapter.MovieAdapter;

public class MovieLibraryFragment extends Fragment
{
    private Library library;

    private FragmentImagesetLibraryBinding binding;
    private MovieAdapter movieAdapter;
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
                changeViewMode(MovieAdapter.Style.MINIMAL);
                return true;
            }
            if(item.getItemId() == R.id.popup_imageset_library_view_mode_compact)
            {
                changeViewMode(MovieAdapter.Style.COMPACT);
                return true;
            }
            if(item.getItemId() == R.id.popup_imageset_library_view_mode_descriptive)
            {
                changeViewMode(MovieAdapter.Style.DESCRIPTIVE);
                return true;
            }

            return false;
        });

        binding.fragmentImagesetLibraryViewMode.setOnClickListener(view -> viewModeMenu.show());

        viewerMain.addCallback(new ViewerMain.Callback()
        {
            @Override public void onImageSetAdded(long imageSetId) {}
            @Override public void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap) {}
            @Override public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap) {}
            @Override public void onFanArtLoaded(Movie movie, Bitmap fanArt) {}
            @Override public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap) {}
            @Override public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap) {}
            @Override public void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap) {}

            @Override
            public void onPosterLoaded(Movie movie, Bitmap posterBitmap)
            {
                if(movieAdapter == null) return;
                AppExecutors.getInstance().mainThread().execute(() ->
                        movieAdapter.addPoster(movie, posterBitmap));
            }

            @Override
            public void onMovieAdded(Movie movie)
            {
                MovieLibraryFragment.this.onMovieAdded(movie);
            }
        });

        AppExecutors.getInstance().diskIO().execute(this::readMoviesAndContinueSetupAsync);

        return binding.getRoot();
    }

    private void readMoviesAndContinueSetupAsync()
    {
        if(getArguments() == null || getArguments().getLong("key_id", -1) == -1)
        {
            Log.e("mediiva.fragmentmovielibrary", "No id argument passed!");
            return;
        }

        long libraryId = getArguments().getLong("key_id");

        LibraryDao libraryDao = AppDatabase.getInstance(getContext()).libraryDao();
        library = libraryDao.getLibraryById(libraryId);

        MovieDao movieDao = AppDatabase.getInstance(getContext()).movieDao();
        List<Movie> movies = movieDao.getMoviesByLibraryId(libraryId);

        AppExecutors.getInstance().mainThread().execute(() -> setupAdapter(movies));
    }

    private void setupAdapter(List<Movie> movies)
    {
        movieAdapter = new MovieAdapter(movies);
        movieAdapter.setHasStableIds(true);
        RecyclerView recyclerView = binding.fragmentImagesetLibraryRecyclerView;
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.CENTER);
        layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                Intent intent = new Intent(getContext(), MovieOverviewActivity.class);
                intent.putExtra("movie_id", movieAdapter.getMovieIdForPosition(position));
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {}
        }));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(movieAdapter);
    }

    private void changeViewMode(MovieAdapter.Style style)
    {
        movieAdapter.setStyle(style);
        // Set the adapter again to avoid mixing layouts
        binding.fragmentImagesetLibraryRecyclerView.setAdapter(movieAdapter);
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
            Log.i("mediiva.fragmentmovielibrary", "Searching library for new entries..");
            if(library != null) viewerMain.refreshMovieLibrary(library);
            return true;
        }
        if(item.getItemId() == R.id.options_media_library_recreate_all)
        {
            Log.i("mediiva.fragmentmovielibrary", "Removing entries and repopulating library..");
            //TODO
            viewerMain.recreateTestLibraryMovie(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMovieAdded(Movie movie)
    {
        Log.e("mediiva.fragmentmovielibrary", "New movie: "+movie.getTitle());

        AppExecutors.getInstance().mainThread().execute(() -> movieAdapter.addMovie(movie));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
