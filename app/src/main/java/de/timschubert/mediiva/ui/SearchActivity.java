package de.timschubert.mediiva.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity
{

    ActivitySearchBinding binding;

    private List<Movie> allMovies = new ArrayList<>();
    private List<ImageSet> allImageSets = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.activitySearchToolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadAllSearchableMedia()
    {
        AppDatabase database = AppDatabase.getInstance(this);

        allMovies = database.movieDao().getMovies();
        allImageSets = database.imageSetDao().getImageSets();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
