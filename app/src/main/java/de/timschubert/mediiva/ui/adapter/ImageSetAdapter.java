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
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.ui.adapter.viewholder.ImageSetCompactViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.ImageSetDescriptiveViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.ImageSetMinimalViewHolder;
import de.timschubert.mediiva.ui.adapter.viewholder.ImageSetViewHolder;

public class ImageSetAdapter extends RecyclerView.Adapter<ImageSetViewHolder>
{

    @NonNull private final List<ImageSet> imageSets;
    @NonNull private final Map<ImageSet, Bitmap> posters;
    @NonNull private Style style;

    public ImageSetAdapter(@NonNull List<ImageSet> imageSets)
    {
        this.imageSets = imageSets;
        posters = new HashMap<>();
        style = Style.COMPACT;
    }

    public void setStyle(@NonNull Style style)
    {
        this.style = style;
    }
    public long getImageSetIdForPosition(int pos) { return imageSets.get(pos).getId(); }

    public void addPoster(ImageSet imageSet, Bitmap poster)
    {
        posters.put(imageSet, poster);
        notifyItemChanged(imageSets.indexOf(imageSet));
    }

    @NonNull
    @Override
    public ImageSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        switch (style)
        {
            default:
            case MINIMAL:
                View viewMinimal = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_imageset_minimal, parent, false);
                return new ImageSetMinimalViewHolder(viewMinimal);
            case COMPACT:
                View viewCompact = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_imageset_compact, parent, false);
                return new ImageSetCompactViewHolder(viewCompact);
            case DESCRIPTIVE:
                View viewDescriptive = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_imageset_descriptive, parent, false);
                return new ImageSetDescriptiveViewHolder(viewDescriptive);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSetViewHolder holder, int position)
    {
        loadPoster(imageSets.get(position), holder.itemView.getContext());
        holder.setContent(imageSets.get(position), posters); //TODO change possibly
    }

    private void loadPoster(ImageSet imageSet, Context context)
    {
        if(!posters.containsKey(imageSet))
        {
            ViewerMain.getInstance(context).requestPosterLoad(imageSet);
        }
    }

    @Override
    public int getItemCount() { return imageSets.size(); }

    public enum Style
    {
        DESCRIPTIVE, COMPACT, MINIMAL
    }
}
