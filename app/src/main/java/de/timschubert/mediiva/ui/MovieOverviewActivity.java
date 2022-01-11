package de.timschubert.mediiva.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorDao;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.FragmentMovieOverviewBinding;
import de.timschubert.mediiva.ui.adapter.ActorAdapter;
import de.timschubert.mediiva.ui.adapter.CharacterAdapter;
import de.timschubert.mediiva.ui.adapter.TagViewAdapter;

public class MovieOverviewActivity extends AppCompatActivity
{
    private FragmentMovieOverviewBinding binding;
    private ViewerMain viewerMain;
    private ActorAdapter actorAdapter;
    private CharacterAdapter characterAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = FragmentMovieOverviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            long movieId = getIntent().getLongExtra("movie_id", -1L);
            Movie movie = AppDatabase.getInstance(this).movieDao().getMovieById(movieId);


            viewerMain = ViewerMain.getInstance(this);
            viewerMain.requestPosterLoad(movie);
            viewerMain.requestFanArtLoad(movie);
            viewerMain.addCallback(new ViewerMain.Callback()
            {
                @Override public void onPosterLoaded(Movie m, Bitmap posterBitmap)
                {
                    if(movie.getId() == m.getId())
                    {
                        runOnUiThread(() -> setPoster(posterBitmap));
                    }
                }

                @Override
                public void onFanArtLoaded(Movie m, Bitmap fanArt)
                {
                    if(movie.getId() == m.getId())
                    {
                        runOnUiThread(() -> setFanArt(fanArt));
                    }
                }

                @Override
                public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap)
                {
                    runOnUiThread(() -> actorAdapter.addThumbnail(actor, actorInfo, thumbBitmap));
                }

                @Override
                public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap)
                {
                    runOnUiThread(() -> characterAdapter.addThumbnail(character, thumbBitmap));
                }

                @Override public void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap) {}
                @Override public void onImageSetAdded(long imageSetId) {}
                @Override public void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap) {}
                @Override public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap) {}
                @Override public void onMovieAdded(Movie movie) {}
            });

            setContent(movie);
        });
    }

    private void setContent(Movie movie)
    {
        if(movie == null)
        {
            Log.w("mediiva.movieoverviewactivity", "setContent movie is null");
            return;
        }

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
            String date = DateFormat.getMediumDateFormat(this).format(movie.getPremieredDate());
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
            Intent intent = new Intent(MovieOverviewActivity.this, VideoPlayerActivity.class);
            intent.putExtra("movie_id", movie.getId());
            startActivity(intent);
        });

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            ActorDao actorDao = AppDatabase.getInstance(this).actorDao();
            List<Actor> actors = new ArrayList<>();

            for(long actorId : movie.getActorIds())
            {
                Actor movieActor = actorDao.getActorById(actorId);
                if(movieActor != null) actors.add(movieActor);
            }

            actorAdapter = new ActorAdapter(actors, movie.getActorInfo(), this);

            CharacterDao characterDao = AppDatabase.getInstance(this).characterDao();
            List<Character> characters = new ArrayList<>();

            for(long characterId : movie.getCharacterIds())
            {
                Character movieCharacter = characterDao.getCharacterById(characterId);
                if(movieCharacter != null) characters.add(movieCharacter);
            }

            characterAdapter = new CharacterAdapter(characters, this);

            AppExecutors.getInstance().mainThread().execute(() ->
            {
                binding.activityMovieOverviewActorsList.setAdapter(actorAdapter);
                binding.activityMovieOverviewCharactersList.setAdapter(characterAdapter);
            });
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        viewerMain.keepConnection(false);
    }

    private void setPoster(Bitmap poster)
    {
        binding.activityMovieOverviewPoster.setClipToOutline(true);
        binding.activityMovieOverviewPoster.setImageBitmap(poster);
    }

    private void setFanArt(Bitmap fanArt)
    {
        binding.activityMovieOverviewFanart.setImageBitmap(fanArt);
    }
}
