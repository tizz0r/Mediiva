package de.timschubert.mediiva.data;

import androidx.annotation.NonNull;

public class UniqueId
{
    private final boolean defaultId;
    @NonNull private final String type;
    @NonNull private final String id;

    public UniqueId(boolean defaultId,
                    @NonNull String type,
                    @NonNull String id)
    {
        this.defaultId = defaultId;
        this.type = type;
        this.id = id;
    }

    public boolean isDefault() { return defaultId; }
    @NonNull public String getType() { return type; }
    @NonNull public String getId() { return id; }
}
