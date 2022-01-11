package de.timschubert.mediiva.ui.adapter.viewholder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Map;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.movie.Movie;

public class MovieMinimalViewHolder extends MovieViewHolder
{
    @NonNull private final TextView titleTextView;
    @NonNull private final TextView studioTextView;
    @NonNull private final TextView yearTextView;

    public MovieMinimalViewHolder(@NonNull View itemView)
    {
        super(itemView);

        titleTextView = itemView.findViewById(R.id.entry_movie_minimal_title);
        studioTextView = itemView.findViewById(R.id.entry_movie_minimal_studio);
        yearTextView = itemView.findViewById(R.id.entry_movie_minimal_year);
    }

    @Override
    public void setContent(Movie movie, Map<Movie, Bitmap> posters)
    {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy"); //TODO research fix
        yearTextView.setText(yearFormat.format(movie.getPremieredDate())); // TODO possibly hide bad dates

        titleTextView.setText(movie.getTitle());
        studioTextView.setText(movie.getStudio());
    }
}
