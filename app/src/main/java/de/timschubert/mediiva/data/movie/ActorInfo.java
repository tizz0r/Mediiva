package de.timschubert.mediiva.data.movie;

import android.net.Uri;

import androidx.annotation.Nullable;

public class ActorInfo
{
    @Nullable private final String role;
    @Nullable private final Uri thumbUri;

    public ActorInfo(@Nullable String role,
                     @Nullable Uri thumbUri)
    {
        this.role = role;
        this.thumbUri = thumbUri;
    }

    @Nullable public String getRole() { return role; }
    @Nullable public Uri getThumbUri() { return thumbUri; }
}
