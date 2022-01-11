package de.timschubert.mediiva.ui.adapter.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Map;

import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.movie.Movie;

public class MovieCompactViewHolder extends MovieViewHolder
{

    @NonNull private final ImageView posterImageView;
    @NonNull private final TextView titleTextView;
    @NonNull private final TextView studioTextView;

    @NonNull private final Context context;

    public MovieCompactViewHolder(@NonNull View itemView)
    {
        super(itemView);

        context = itemView.getContext();
        posterImageView = itemView.findViewById(R.id.entry_movie_compact_poster);
        posterImageView.setClipToOutline(true);
        titleTextView = itemView.findViewById(R.id.entry_movie_compact_title);
        studioTextView = itemView.findViewById(R.id.entry_movie_compact_studio);
    }

    @Override
    public void setContent(Movie movie, Map<Movie, Bitmap> posters)
    {
        posterImageView.setImageBitmap(posters.get(movie));

        titleTextView.setText(movie.getTitle());
        studioTextView.setText(movie.getStudio());
    }
}
