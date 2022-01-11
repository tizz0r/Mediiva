package de.timschubert.mediiva.ui.adapter.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.ui.adapter.TagViewAdapter;

public class MovieDescriptiveViewHolder extends MovieViewHolder
{

    @NonNull private final ImageView posterImageView;
    @NonNull private final ImageView localeImageView;
    @NonNull private final TextView titleTextView;
    @NonNull private final TextView artistTextView;
    @NonNull private final TextView seriesTextView;
    @NonNull private final TextView seriesDescTextView;
    @NonNull private final TextView groupTextView;
    @NonNull private final TextView groupDescTextView;
    @NonNull private final TextView yearTextView;
    @NonNull private final TextView pagesTextView;
    @NonNull private final RecyclerView tagsRecyclerView;

    @NonNull private final Context context;

    public MovieDescriptiveViewHolder(@NonNull View itemView)
    {
        super(itemView);

        context = itemView.getContext();

        posterImageView = itemView.findViewById(R.id.entry_imageset_descriptive_poster);
        localeImageView = itemView.findViewById(R.id.entry_imageset_descriptive_locale);
        titleTextView = itemView.findViewById(R.id.entry_imageset_descriptive_title);
        artistTextView = itemView.findViewById(R.id.entry_imageset_descriptive_artists);
        seriesTextView = itemView.findViewById(R.id.entry_imageset_descriptive_series);
        seriesDescTextView = itemView.findViewById(R.id.entry_imageset_descriptive_series_desc);
        groupTextView = itemView.findViewById(R.id.entry_imageset_descriptive_group);
        groupDescTextView = itemView.findViewById(R.id.entry_imageset_descriptive_group_desc);
        yearTextView = itemView.findViewById(R.id.entry_imageset_descriptive_year);
        pagesTextView = itemView.findViewById(R.id.entry_imageset_descriptive_pages);
        tagsRecyclerView = itemView.findViewById(R.id.entry_imageset_descriptive_tags);
    }

    @Override
    public void setContent(Movie movie, Map<Movie, Bitmap> posters)
    {
        localeImageView.setImageBitmap(null); // TODO get locale

        posterImageView.setImageBitmap(posters.get(movie));
        posterImageView.setClipToOutline(true);

        titleTextView.setText(movie.getTitle());
        artistTextView.setText(movie.getStudio());

        String series = movie.getSeries();
        boolean seriesExists = series != null && !series.isEmpty();
        seriesTextView.setText(series);
        seriesTextView.setVisibility(seriesExists ? View.VISIBLE : View.GONE);
        seriesDescTextView.setVisibility(seriesExists ? View.VISIBLE : View.GONE);

        String director = movie.getDirector();
        boolean directorExists = director != null && !director.isEmpty();
        groupTextView.setText(director);
        groupTextView.setVisibility(directorExists ? View.VISIBLE : View.GONE);
        groupDescTextView.setVisibility(directorExists ? View.VISIBLE : View.GONE);

        if(movie.getPremieredDate() != null)
        {
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy"); //TODO research fix
            yearTextView.setText(yearFormat.format(movie.getPremieredDate())); // TODO possibly hide bad dates
            yearTextView.setVisibility(View.VISIBLE);
        }
        else
        {
            yearTextView.setVisibility(View.GONE);
        }

        pagesTextView.setText(null);

        tagsRecyclerView.setAdapter(new TagViewAdapter(movie.getTags(), 9));
    }
}
