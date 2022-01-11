package de.timschubert.mediiva.ui.adapter.viewholder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.Helper;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.imageset.ImageSet;

public class ImageSetMinimalViewHolder extends ImageSetViewHolder
{
    @NonNull private final ConstraintLayout layout;
    @NonNull private final TextView artistTextView;
    @NonNull private final TextView titleTextView;
    @NonNull private final TextView yearTextView;

    @NonNull private final Context context;

    public ImageSetMinimalViewHolder(@NonNull View itemView)
    {
        super(itemView);
        context = itemView.getContext();

        layout = itemView.findViewById(R.id.entry_imageset_minimal_layout);
        artistTextView = itemView.findViewById(R.id.entry_imageset_minimal_artist);
        titleTextView = itemView.findViewById(R.id.entry_imageset_minimal_title);
        yearTextView = itemView.findViewById(R.id.entry_imageset_minimal_year);
    }

    @Override
    public void setContent(ImageSet imageSet, Map<ImageSet, Bitmap> posters)
    {
        @ColorRes int backgroundColor = R.color.imageset_background_default;

        switch (imageSet.getType())
        {
            default:
            case IMAGESET:
                break;
            case COMIC:
                backgroundColor = R.color.imageset_background_comic;
                break;
            case MANGA:
                backgroundColor = R.color.imageset_background_manga;
                break;
            case DOUJINSHI:
                backgroundColor = R.color.imageset_background_doujinshi;
                break;
            case ARTISTCG:
                backgroundColor = R.color.imageset_background_artistcg;
                break;
            case GAMECG:
                backgroundColor = R.color.imageset_background_gamecg;
                break;
        }

        ColorStateList colorState =
                ColorStateList.valueOf(ContextCompat.getColor(context, backgroundColor));
        layout.setBackgroundTintList(colorState);

        titleTextView.setText(imageSet.getTitle());

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy"); //TODO research fix
        yearTextView.setText(yearFormat.format(imageSet.getPremieredDate())); // TODO possibly hide bad dates

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            String artistString = Helper.getFriendlyArtistsName(imageSet, context);

            AppExecutors.getInstance().mainThread().execute(() -> artistTextView.setText(artistString));
        });
    }
}
