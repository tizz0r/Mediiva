package de.timschubert.mediiva.data.tag;

import android.util.Base64;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

import de.timschubert.mediiva.Helper;

public abstract class Tag
{
    @NonNull public abstract String getTagName(); // e.g. "high school"
    @NonNull public abstract String getTagPrefix(); // e.g. "location:"
    @ColorRes public abstract int getColor(); // background color for display
    @DrawableRes public abstract int getIcon(); // e.g. marker icon

    @NonNull
    public String getDisplayName()
    {
        return Helper.capitalizeString(getTagName());
    }

    @NonNull
    public String getEncodedString() // e.g. "location:$base64(cafeteria)"
    {
        return getTagPrefix()+ Base64.encodeToString(getTagName().getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }
}