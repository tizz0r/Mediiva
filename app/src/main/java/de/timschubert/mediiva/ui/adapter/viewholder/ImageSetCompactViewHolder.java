package de.timschubert.mediiva.ui.adapter.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.Helper;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.imageset.ImageSet;

public class ImageSetCompactViewHolder extends ImageSetViewHolder
{

    @NonNull private final Context context;
    @NonNull private final ImageView posterImageView;
    @NonNull private final TextView titleTextView;
    @NonNull private final TextView artistTextView;

    public ImageSetCompactViewHolder(@NonNull View itemView)
    {
        super(itemView);

        context = itemView.getContext();
        posterImageView = itemView.findViewById(R.id.entry_imageset_compact_poster);
        titleTextView = itemView.findViewById(R.id.entry_imageset_compact_title);
        artistTextView = itemView.findViewById(R.id.entry_imageset_compact_artist);
    }

    @Override
    public void setContent(ImageSet imageSet, Map<ImageSet, Bitmap> posters)
    {
        posterImageView.setImageBitmap(posters.get(imageSet));
        titleTextView.setText(imageSet.getTitle());

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            String artistString = Helper.getFriendlyArtistsName(imageSet, context);

            AppExecutors.getInstance().mainThread().execute(() -> artistTextView.setText(artistString));
        });
    }
}
