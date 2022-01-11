package de.timschubert.mediiva.data.tag;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

import de.timschubert.mediiva.R;

public class FemaleTag extends GenderTag
{

    @NonNull private final String prefix;
    @NonNull private final String tagName;
    @NonNull private final String genderNameSingular;
    @NonNull private final String genderNamePlural;

    public FemaleTag(@NonNull Context context,
                     @NonNull String tag)
    {
        prefix = context.getString(R.string.tag_female_prefix);
        tagName = tag;
        genderNameSingular = context.getString(R.string.tag_female_gender_singular);
        genderNamePlural = context.getString(R.string.tag_female_gender_plural);
    }

    @NonNull
    @Override
    public String getTagName() { return tagName; }

    @NonNull
    @Override
    public String getTagPrefix() { return prefix; }

    @Override
    public int getColor() { return R.color.tag_background_female; }

    @Override
    public int getIcon() { return R.drawable.ic_female; }

    @NonNull
    @Override
    String getGenderNameSingular() { return genderNameSingular; }

    @NonNull
    @Override
    String getGenderNamePlural() { return genderNamePlural; }

    @NonNull
    public static Tag fromEncodedString(Context context, String value)
    {
        // Format: female:$base64(tag)
        String tagEncoded = value.substring(value.indexOf(":")+1);
        String tag = new String(Base64.decode(tagEncoded, Base64.DEFAULT), StandardCharsets.UTF_8);

        Log.d("mediiva.femaletag", "fromEncodedString value: "+value+"\ttag encoded: "+tagEncoded+"\ttag: "+tag);
        return new FemaleTag(context, tag);
    }

    @NonNull
    public static Tag fromXMLRawString(Context context, String value)
    {
        String tag = value.substring(value.indexOf(":")+1);

        return new FemaleTag(context, tag);
    }
}
