package de.timschubert.mediiva.ui.adapter.viewholder;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

import de.timschubert.mediiva.data.movie.Movie;

public abstract class MovieViewHolder extends RecyclerView.ViewHolder
{
    public MovieViewHolder(@NonNull View itemView) { super(itemView); }
    public abstract void setContent(Movie movie, Map<Movie, Bitmap> posters);
}
