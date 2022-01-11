package de.timschubert.mediiva.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.ActivityVideoPlayerBinding;
import de.timschubert.mediiva.exo.SmbDataSourceFactory;

public class VideoPlayerActivity extends AppCompatActivity
{

    private ExoPlayer exoPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        ActivityVideoPlayerBinding binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StyledPlayerView playerView = binding.activityVideoPlayerView;

        exoPlayer = new ExoPlayer.Builder(this).build();

        playerView.setPlayer(exoPlayer);

        long movieId = getIntent().getLongExtra("movie_id", -1L);
        AppExecutors.getInstance().diskIO().execute(() -> getMovieFromDatabase(movieId));
    }

    private void getMovieFromDatabase(long movieId)
    {
        Movie movie = AppDatabase.getInstance(this).movieDao().getMovieById(movieId);
        Library library = AppDatabase.getInstance(this).libraryDao().getLibraryById(movie.getLibraryId());
        AppExecutors.getInstance().mainThread().execute(() -> setupPlayer(library, movie));
    }

    private void setupPlayer(Library library, Movie movie)
    {
        MediaItem mediaItem = new MediaItem.Builder().setMediaId(String.valueOf(movie.getId())).setUri(Uri.parse("")).build();
        MediaSource source = new ProgressiveMediaSource.Factory(new SmbDataSourceFactory(library, movie.getMoviePath())).createMediaSource(mediaItem);
        exoPlayer.setMediaSource(source);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(exoPlayer == null) return;

        exoPlayer.setPlayWhenReady(false);
        exoPlayer.stop();
    }
}
