package de.timschubert.mediiva.network;

import androidx.annotation.NonNull;

import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;

public abstract class NetworkImageLoader extends ImageLoader
{
    public NetworkImageLoader(Callback callback) { super(callback); }

    public abstract void requestHoldConnection();
    public abstract void requestCloseConnection();
}
