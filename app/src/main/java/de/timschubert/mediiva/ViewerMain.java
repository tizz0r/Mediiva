package de.timschubert.mediiva;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.CharacterDao;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.ImageSetDao;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorDao;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.data.movie.MovieDao;
import de.timschubert.mediiva.dataloader.LibraryLoader;
import de.timschubert.mediiva.network.ImageLoader;
import de.timschubert.mediiva.network.NetworkImageLoader;
import de.timschubert.mediiva.network.SmbImageLoaderNew;
import de.timschubert.mediiva.network.SmbMovieLibraryLoader;
import de.timschubert.mediiva.network.SmbReader;

public class ViewerMain
{

    private static ViewerMain instance;


    private final LibraryLoader<Movie> testMovieLoader;

    private final SmbReader smbReader;
    private final NetworkImageLoader smbImageLoader;
    private final List<Callback> callbacks; //TODO reference callbacks with ids

    private ViewerMain(Context context)
    {
        callbacks = new ArrayList<>();

        testMovieLoader = new SmbMovieLibraryLoader(item ->
        {
            for(Callback callback : callbacks) callback.onMovieAdded(item);
        }, context);

        smbImageLoader = new SmbImageLoaderNew(context, new ImageLoader.Callback()
        {
            @Override
            public void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap)
            {
                for(Callback callback : callbacks) callback.onPosterLoaded(imageSet, posterBitmap);
            }

            @Override
            public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap)
            {
                for(Callback callback : callbacks) callback.onPageLoaded(imageSet, page, pageBitmap);
            }

            @Override
            public void onPosterLoaded(Movie movie, Bitmap posterBitmap) {
                for(Callback callback : callbacks) callback.onPosterLoaded(movie, posterBitmap);
            }

            @Override
            public void onFanArtLoaded(Movie movie, Bitmap fanArt) {
                for(Callback callback : callbacks) callback.onFanArtLoaded(movie, fanArt);
            }

            @Override
            public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap) {
                for(Callback callback : callbacks) callback.onActorThumbLoaded(actor, actorInfo, thumbBitmap);
            }

            @Override
            public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap) {
                for(Callback callback : callbacks) callback.onCharacterThumbLoaded(character, thumbBitmap);
            }

            @Override
            public void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap) {
                for(Callback callback : callbacks) callback.onArtistThumbLoaded(artist, thumbBitmap);
            }
        });

        smbReader = new SmbReader(context, new SmbReader.Callback()
        {
            @Override
            public void onLoadImageSet(long imageSetId)
            {
                for(Callback callback : callbacks) callback.onImageSetAdded(imageSetId);
            }

            @Override
            public void onLoadPoster(ImageSet imageSet, Bitmap poster)
            {
                // TODO remove
            }

            @Override
            public void onLoadPage(ImageSet imageSet, Page page, Bitmap bitmap)
            {
                // TODO remove
            }
        });
    }

    public void addCallback(Callback callback)
    {
        callbacks.add(callback);
    }

    public void refreshImageSetLibrary(Library library)
    {
        smbReader.searchForImageSets(library);
    }

    public void refreshMovieLibrary(Library library)
    {
        testMovieLoader.searchForNewItemsAsync(library);
    }

    public void recreateTestLibraryImageSet(Context context)
    {
        AppExecutors.getInstance().diskIO().execute(() ->
        {
            ImageSetDao imageSetDao = AppDatabase.getInstance(context).imageSetDao();

            for(ImageSet imageSet : imageSetDao.getImageSets())
            {
                imageSetDao.deleteImageSet(imageSet);
            }

            //TODO refreshTestLibraryImageSet();
        });
    }

    public void recreateTestLibraryMovie(Context context)
    {
        AppExecutors.getInstance().diskIO().execute(() ->
        {
            MovieDao movieDao = AppDatabase.getInstance(context).movieDao();
            for(Movie movie : movieDao.getMovies())
            {
                movieDao.deleteMovie(movie);
            }

            CharacterDao characterDao = AppDatabase.getInstance(context).characterDao();
            for(Character character : characterDao.getCharacters())
            {
                characterDao.deleteCharacter(character);
            }

            ActorDao actorDao = AppDatabase.getInstance(context).actorDao();
            for(Actor actor : actorDao.getActors())
            {
                actorDao.deleteActor(actor);
            }

            //TODO refreshTestLibraryMovie();
        });
    }

    public void requestPosterLoad(ImageSet imageSet)
    {
        smbImageLoader.requestPoster(imageSet);
    }
    public void requestPosterLoad(Movie movie)
    {
        smbImageLoader.requestPoster(movie);
    }
    public void requestFanArtLoad(Movie movie)
    {
        smbImageLoader.requestFanArt(movie);
    }
    public void requestActorThumbLoad(Actor actor, ActorInfo actorInfo) { smbImageLoader.requestActorThumb(actor, actorInfo); }

    public void requestCharacterThumbLoad(Character character) { smbImageLoader.requestCharacterThumb(character); }

    public void requestImageLoad(ImageSet imageSet, Page page)
    {
        smbImageLoader.requestPage(imageSet, page);
    }

    public void keepConnection(boolean keep)
    {
        if(keep) smbImageLoader.requestHoldConnection();
        else smbImageLoader.requestCloseConnection();
    }

    public void onSearchQueryChange(String query)
    {
        for(Callback callback : callbacks) callback.onSearchQueryChange(query);
    }

    public static ViewerMain getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new ViewerMain(context);
        }

        return instance;
    }

    public static abstract class Callback
    {
        public void onImageSetAdded(long imageSetId) {}
        public void onPosterLoaded(ImageSet imageSet, Bitmap posterBitmap) {}
        public void onPosterLoaded(Movie movie, Bitmap posterBitmap) {}
        public void onFanArtLoaded(Movie movie, Bitmap fanArt) {}
        public void onActorThumbLoaded(Actor actor, ActorInfo actorInfo, Bitmap thumbBitmap) {}
        public void onCharacterThumbLoaded(Character character, Bitmap thumbBitmap) {}
        public void onArtistThumbLoaded(Artist artist, Bitmap thumbBitmap) {}
        public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap) {}
        public void onMovieAdded(Movie movie) {}

        public void onSearchQueryChange(String query) {}
    }
}
