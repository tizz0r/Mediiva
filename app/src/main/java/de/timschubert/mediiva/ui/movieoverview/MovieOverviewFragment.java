package de.timschubert.mediiva.ui.movieoverview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.FragmentMovieOverviewBinding;

public class MovieOverviewFragment extends Fragment
{

    private static final String TAG = "mediiva.movieoverviewfragment";

    private FragmentMovieOverviewBinding binding;
    private ViewerMain viewerMain;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = FragmentMovieOverviewBinding.inflate(inflater, container, false);
        viewerMain = ViewerMain.getInstance(requireContext());

        if(getArguments() == null || getArguments().getLong("movie_id", -1) == -1)
        {
            Log.w(TAG, "No movie id argument passed");
            return binding.getRoot();
        }

        long movieId = getArguments().getLong("movie_id", -1L);
        AppExecutors.getInstance().diskIO().execute(() -> getMovieAndContinue(movieId));

        return binding.getRoot();
    }

    private void getMovieAndContinue(long movieId)
    {
        Movie movie = AppDatabase.getInstance(requireContext()).movieDao().getMovieById(movieId);
        if(movie == null)
        {
            Log.w(TAG, "Aborting. Movie id not found in database: \""+movieId+"\"");
            return;
        }

        viewerMain.addCallback(new ViewerMain.Callback()
        {
            @Override
            public void onPosterLoaded(Movie m, Bitmap posterBitmap)
            {
                if(m.getId() != movieId) return;

            }

            @Override
            public void onFanArtLoaded(Movie m, Bitmap fanArt) {
                super.onFanArtLoaded(movie, fanArt);
            }

            @Override
            public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap) {
                super.onActorThumbLoaded(actor, actorInfo, thumbBitmap);
            }

            @Override
            public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap) {
                super.onCharacterThumbLoaded(character, thumbBitmap);
            }
        });
    }

    private void setPoster(Bitmap posterBitmap)
    {

    }

    private void setFanArt(Bitmap fanArtBitmap)
    {

    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }
}
