package de.timschubert.mediiva.ui.movieoverview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorDao;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.FragmentMovieOverviewBinding;
import de.timschubert.mediiva.ui.VideoPlayerActivity;
import de.timschubert.mediiva.ui.adapter.ActorAdapter;
import de.timschubert.mediiva.ui.adapter.CharacterAdapter;
import de.timschubert.mediiva.ui.adapter.TagViewAdapter;

public class MovieOverviewFragment extends Fragment
{

    private static final String TAG = "mediiva.movieoverviewfragment";

    private FragmentMovieOverviewBinding binding;
    private ViewerMain viewerMain;
    private ActorAdapter actorAdapter;
    private CharacterAdapter characterAdapter;

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

        viewerMain.requestPosterLoad(movie);
        viewerMain.requestFanArtLoad(movie);
        viewerMain.addCallback(new ViewerMain.Callback()
        {
            @Override
            public void onPosterLoaded(Movie m, Bitmap posterBitmap)
            {
                if(m.getId() != movieId) return;
                runOnUiThread(() -> setPoster(posterBitmap));
            }

            @Override
            public void onFanArtLoaded(Movie m, Bitmap fanArt)
            {
                if(m.getId() != movieId) return;
                runOnUiThread(() -> setFanArt(fanArt));
            }

            @Override
            public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap)
            {
                if(actorAdapter == null)
                {
                    Log.w(TAG, "Actor thumb loaded before adapter was initialized");
                    return;
                }
                runOnUiThread(() -> actorAdapter.addThumbnail(actor, actorInfo, thumbBitmap));
            }

            @Override
            public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap)
            {
                if(characterAdapter == null)
                {
                    Log.w(TAG, "Character thumb loaded before adapter was initialized");
                    return;
                }
                runOnUiThread(() -> characterAdapter.addThumbnail(character, thumbBitmap));
            }
        });

        runOnUiThread(() -> setContent(movie));
    }

    private void setContent(@NonNull Movie movie)
    {
        try {
            Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(movie.getTitle()); // TODO
        }
        catch (NullPointerException ignore) {}

        boolean hasSeries = movie.getSeries() != null && !movie.getSeries().isEmpty();
        boolean hasStudio = movie.getStudio() != null && !movie.getStudio().isEmpty();
        boolean hasDirector = movie.getDirector() != null && !movie.getDirector().isEmpty();
        boolean hasPremiered = movie.getPremieredDate() != null;
        boolean hasRuntime = movie.getRuntimeMinutes() > 0;

        binding.activityMovieOverviewTitle.setText(movie.getTitle());
        binding.activityMovieOverviewOriginalTitle.setText(movie.getOriginalTitle());
        binding.activityMovieOverviewSeries.setText(movie.getSeries());
        binding.activityMovieOverviewStudio.setText(movie.getStudio());
        binding.activityMovieOverviewDirector.setText(movie.getDirector());

        binding.activityMovieOverviewSeriesLayout.setVisibility(hasSeries ? View.VISIBLE : View.GONE);
        binding.activityMovieOverviewStudioLayout.setVisibility(hasStudio ? View.VISIBLE : View.GONE);
        binding.activityMovieOverviewDirectorLayout.setVisibility(hasDirector ? View.VISIBLE : View.GONE);

        if(hasPremiered)
        {
            String date = DateFormat.getMediumDateFormat(requireContext()).format(movie.getPremieredDate());
            binding.activityMovieOverviewPremiered.setText(date);
        }
        binding.activityMovieOverviewPremieredDesc.setVisibility(hasPremiered ? View.VISIBLE : View.GONE);
        binding.activityMovieOverviewRuntime.setText(getString(R.string.runtime_placeholder, movie.getRuntimeMinutes()));
        binding.activityMovieOverviewRuntimeLayout.setVisibility(hasRuntime ? View.VISIBLE : View.GONE);

        TagViewAdapter tagAdapterSimple = new TagViewAdapter(movie.getTags(), getString(R.string.tag_simple_prefix));
        TagViewAdapter tagAdapterLocation = new TagViewAdapter(movie.getTags(), getString(R.string.tag_location_prefix));
        TagViewAdapter tagAdapterHermaphrodite = new TagViewAdapter(movie.getTags(), getString(R.string.tag_hermaphrodite_prefix));
        TagViewAdapter tagAdapterFemale = new TagViewAdapter(movie.getTags(), getString(R.string.tag_female_prefix));
        TagViewAdapter tagAdapterMale = new TagViewAdapter(movie.getTags(), getString(R.string.tag_male_prefix));
        binding.activityMovieOverviewTagsSimpleList.setAdapter(tagAdapterSimple);
        binding.activityMovieOverviewTagsLocationList.setAdapter(tagAdapterLocation);
        binding.activityMovieOverviewTagsHermaphroditeList.setAdapter(tagAdapterHermaphrodite);
        binding.activityMovieOverviewTagsFemaleList.setAdapter(tagAdapterFemale);
        binding.activityMovieOverviewTagsMaleList.setAdapter(tagAdapterMale);

        binding.activityMovieOverviewPoster.setOnClickListener(v ->
        {
            Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
            intent.putExtra("movie_id", movie.getId()); //TODO string resource
            startActivity(intent);
        });

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            ActorDao actorDao = AppDatabase.getInstance(requireContext()).actorDao();
            List<Actor> actors = new ArrayList<>();

            for(long actorId : movie.getActorIds())
            {
                Actor movieActor = actorDao.getActorById(actorId);
                if(movieActor != null) actors.add(movieActor);
            }

            actorAdapter = new ActorAdapter(actors, movie.getActorInfo(), requireContext());

            CharacterDao characterDao = AppDatabase.getInstance(requireContext()).characterDao();
            List<Character> characters = new ArrayList<>();

            for(long characterId : movie.getCharacterIds())
            {
                Character movieCharacter = characterDao.getCharacterById(characterId);
                if(movieCharacter != null) characters.add(movieCharacter);
            }

            characterAdapter = new CharacterAdapter(characters, requireContext());

            AppExecutors.getInstance().mainThread().execute(() ->
            {
                binding.activityMovieOverviewActorsList.setAdapter(actorAdapter);
                binding.activityMovieOverviewCharactersList.setAdapter(characterAdapter);
            });
        });
    }

    private void runOnUiThread(Runnable runnable)
    {
        if(getActivity() == null) return;
        getActivity().runOnUiThread(runnable);
    }

    private void setPoster(Bitmap posterBitmap)
    {
        if(binding == null) return;
        binding.activityMovieOverviewPoster.setClipToOutline(true);
        binding.activityMovieOverviewPoster.setImageBitmap(posterBitmap);
    }

    private void setFanArt(Bitmap fanArtBitmap)
    {
        if(binding == null) return;
        binding.activityMovieOverviewFanart.setImageBitmap(fanArtBitmap);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        viewerMain.keepConnection(false);
        binding = null;
    }
}
