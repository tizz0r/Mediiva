package de.timschubert.mediiva.ui.adapter.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.Helper;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.data.imageset.Chapter;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.ui.adapter.TagViewAdapter;

public class ImageSetDescriptiveViewHolder extends ImageSetViewHolder
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

    public ImageSetDescriptiveViewHolder(@NonNull View itemView)
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
    public void setContent(ImageSet imageSet, Map<ImageSet, Bitmap> posters)
    {
        localeImageView.setImageBitmap(null); // TODO get locale

        posterImageView.setImageBitmap(posters.get(imageSet));
        posterImageView.setClipToOutline(true);

        titleTextView.setText(imageSet.getTitle());

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            String friendlyArtistText = Helper.getFriendlyArtistsName(imageSet, context);

            AppExecutors.getInstance().mainThread().execute(() ->
                    artistTextView.setText(friendlyArtistText));
        });

        String series = imageSet.getSeries();
        boolean seriesExists = series != null && !series.isEmpty();
        seriesTextView.setText(series);
        seriesTextView.setVisibility(seriesExists ? View.VISIBLE : View.GONE);
        seriesDescTextView.setVisibility(seriesExists ? View.VISIBLE : View.GONE);

        String group = imageSet.getGroup();
        boolean groupExists = group != null && !group.isEmpty();
        groupTextView.setText(group);
        groupTextView.setVisibility(groupExists ? View.VISIBLE : View.GONE);
        groupDescTextView.setVisibility(groupExists ? View.VISIBLE : View.GONE);

        if(imageSet.getPremieredDate() != null)
        {
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
            yearTextView.setText(yearFormat.format(imageSet.getPremieredDate())); // TODO possibly hide bad dates
        }

        int pagesTotal = 0;
        for(Chapter chapter : imageSet.getChapters()) { pagesTotal += chapter.getPages().size(); }
        pagesTextView.setText(context.getString(R.string.imageset_page_chapter_count, pagesTotal, imageSet.getChapters().size()));

        tagsRecyclerView.setAdapter(new TagViewAdapter(imageSet.getTags(), 9));
    }
}
