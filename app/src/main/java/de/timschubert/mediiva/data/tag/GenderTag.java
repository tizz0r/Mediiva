package de.timschubert.mediiva.data.tag;

import androidx.annotation.NonNull;

import java.util.Locale;

public abstract class GenderTag extends Tag
{
    @NonNull abstract String getGenderNameSingular();
    @NonNull abstract String getGenderNamePlural();

    @NonNull
    @Override
    public String getDisplayName()
    {
        String tagName = getTagName().toLowerCase(Locale.ROOT);

        if(!tagName.startsWith("amount") || tagName.length() <= 6) return super.getDisplayName();

        int amount = 0;
        try
        {
            amount = Integer.parseInt(tagName.substring(6));
        }
        catch (NumberFormatException ignore) {}

        if(amount <= 0) return super.getDisplayName();

        return amount + " " + (amount > 1 ? getGenderNamePlural() : getGenderNameSingular());
    }
}
