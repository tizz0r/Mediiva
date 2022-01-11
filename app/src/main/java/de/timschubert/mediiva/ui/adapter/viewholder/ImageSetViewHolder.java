package de.timschubert.mediiva.ui.adapter.viewholder;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

import de.timschubert.mediiva.data.imageset.ImageSet;

public abstract class ImageSetViewHolder extends RecyclerView.ViewHolder
{
    public ImageSetViewHolder(@NonNull View itemView) { super(itemView); }
    public abstract void setContent(ImageSet imageSet, Map<ImageSet, Bitmap> posters);
}
