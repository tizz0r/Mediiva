package de.timschubert.mediiva.network;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.List;

import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;

public abstract class ImageLoader
{
    private final Callback callback;

    public ImageLoader(Callback callback)
    {
        this.callback = callback;
    }

    public abstract void requestPoster(@NonNull ImageSet... imageSet);
    public abstract void requestPoster(@NonNull Movie movie);
    public abstract void requestFanArt(@NonNull Movie movie);
    public abstract void requestActorThumb(@NonNull Actor actor, @NonNull ActorInfo actorInfo);
    public abstract void requestCharacterThumb(@NonNull Character character);
    public abstract void requestArtistThumb(@NonNull Artist artist);
    public abstract void requestPage(@NonNull ImageSet imageSet, @NonNull Page... page);

    public Callback getCallback() { return callback; }

    public interface Callback
    {
        void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap);
        void onPosterLoaded(Movie movie, Bitmap posterBitmap);
        void onFanArtLoaded(Movie movie, Bitmap fanArt);
        void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap);
        void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap);
        void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap);
        void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap);
    }
}
