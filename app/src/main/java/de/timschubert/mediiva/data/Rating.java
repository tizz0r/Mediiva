package de.timschubert.mediiva.data;

import androidx.annotation.NonNull;

public class Rating
{
    private final boolean defaultRating;
    private final float rating;
    private final float maxValue;

    public Rating(boolean defaultRating, float rating, float maxValue)
    {
        this.defaultRating = defaultRating;
        this.rating = rating;
        this.maxValue = maxValue;
    }

    public boolean isDefault() { return defaultRating; }
    public float getRating() { return rating; }
    public float getMaxValue() { return maxValue; }
    public float getCalculatedRating() { return rating / maxValue; }
    @NonNull public String getFormattedRating()
    {
        return ""+rating+" / "+maxValue;
    }
}
