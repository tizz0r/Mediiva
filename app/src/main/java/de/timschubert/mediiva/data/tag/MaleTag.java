package de.timschubert.mediiva.data.tag;

import android.content.Context;

import androidx.annotation.NonNull;

import de.timschubert.mediiva.R;

public class MaleTag extends GenderTag
{

    @NonNull private final String prefix;
    @NonNull private final String tagName;
    @NonNull private final String genderNameSingular;
    @NonNull private final String genderNamePlural;

    public MaleTag(@NonNull Context context,
                   @NonNull String tag)
    {
        prefix = context.getString(R.string.tag_male_prefix);
        tagName = tag;
        genderNameSingular = context.getString(R.string.tag_male_gender_singular);
        genderNamePlural = context.getString(R.string.tag_male_gender_plural);
    }

    @NonNull
    @Override
    public String getTagName() { return tagName; }

    @NonNull
    @Override
    public String getTagPrefix() { return prefix; }

    @NonNull
    @Override
    String getGenderNameSingular() { return genderNameSingular; }

    @NonNull
    @Override
    String getGenderNamePlural() { return genderNamePlural; }

    @Override
    public int getColor() {
        return R.color.tag_background_male;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_male;
    }
}
