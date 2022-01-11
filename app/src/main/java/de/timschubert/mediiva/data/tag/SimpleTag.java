package de.timschubert.mediiva.data.tag;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

import de.timschubert.mediiva.R;

public class SimpleTag extends Tag
{

    @NonNull private final String prefix;
    @NonNull private final String name;

    public SimpleTag(@NonNull Context context,
                     @NonNull String tag)
    {
        prefix = context.getString(R.string.tag_simple_prefix);
        name = tag;
    }

    @NonNull
    @Override
    public String getTagName() { return name; }

    @NonNull
    @Override
    public String getTagPrefix() { return prefix; }

    @Override
    public int getColor() { return R.color.tag_background_simple; }

    @Override
    public int getIcon() { return -1; }

    @NonNull
    public static Tag fromEncodedString(Context context, String value)
    {
        // Format: simple:$base64(tag)
        String tagEncoded = value.substring(value.indexOf(":")+1);
        String tag = new String(Base64.decode(tagEncoded, Base64.DEFAULT),StandardCharsets.UTF_8);

        Log.d("mediiva.simpletag", "fromEncodedString value: "+value+"\ttag encoded: "+tagEncoded+"\ttag: "+tag);
        return new SimpleTag(context, tag);
    }

    @NonNull
    public static Tag fromXMLRawString(Context context, String value)
    {
        String[] simplePrefixes = context.getResources().getStringArray(R.array.tag_simple_lookout_prefixes);
        String tag = value;

        for(String prefix : simplePrefixes)
        {
            if(!tag.startsWith(prefix)) continue;

            tag = value.substring(value.indexOf(":")+1);
            break;
        }

        return new SimpleTag(context, tag);
    }
}
