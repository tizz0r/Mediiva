package de.timschubert.mediiva.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.ui.adapter.viewholder.MovieCompactViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.MovieDescriptiveViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.MovieMinimalViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.MovieViewHolder;

public class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder>
{

    @NonNull private final List<Movie> movies;
    @NonNull private final Map<Movie, Bitmap> posters;
    @NonNull private Style style;

    public MovieAdapter(@NonNull List<Movie> movies)
    {
        this.movies = movies;
        posters = new HashMap<>();
        style = Style.DESCRIPTIVE;
    }

    public void setStyle(@NonNull Style style)
    {
        this.style = style;
    }
    public long getMovieIdForPosition(int pos) { return movies.get(pos).getId(); }

    public void addMovie(Movie movie)
    {
        movies.add(movie);
        notifyItemChanged(movies.size()-1);
    }

    public void addPoster(Movie movie, Bitmap poster)
    {
        posters.put(movie, poster);
        notifyItemChanged(movies.indexOf(movie));
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (style)
        {
            default:
            case DESCRIPTIVE:
                View descriptiveView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_imageset_descriptive, parent, false);
                return new MovieDescriptiveViewHolder(descriptiveView);
            case COMPACT:
                View compactView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_movie_compact, parent, false);
                return new MovieCompactViewHolder(compactView);
            case MINIMAL:
                View minimalView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.entry_movie_minimal, parent, false);
                return new MovieMinimalViewHolder(minimalView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position)
    {
        loadPoster(movies.get(position), holder.itemView.getContext());

        holder.setContent(movies.get(position), posters); //TODO change possibly
    }

    private void loadPoster(Movie movie, Context context)
    {
        if(!posters.containsKey(movie))
        {
            ViewerMain.getInstance(context).requestPosterLoad(movie);
        }
    }

    @Override
    public long getItemId(int position) { return movies.get(position).getId(); }

    @Override
    public int getItemCount() { return movies.size(); }

    public enum Style
    {
        DESCRIPTIVE, COMPACT, MINIMAL
    }
}
