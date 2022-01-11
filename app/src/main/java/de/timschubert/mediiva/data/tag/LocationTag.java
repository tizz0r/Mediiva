package de.timschubert.mediiva.data.tag;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

import de.timschubert.mediiva.R;

public class LocationTag extends Tag
{

    @NonNull private final String prefix;
    @NonNull private final String tagName;

    public LocationTag(@NonNull Context context,
                       @NonNull String tag)
    {
        prefix = context.getString(R.string.tag_location_prefix);
        tagName = tag;
    }

    @NonNull
    @Override
    public String getTagName()
    {
        return tagName;
    }

    @NonNull
    @Override
    public String getTagPrefix() { return prefix; }

    @Override
    public int getColor() { return R.color.tag_background_location; }

    @Override
    public int getIcon() {
        return R.drawable.ic_outline_location;
    }
}
