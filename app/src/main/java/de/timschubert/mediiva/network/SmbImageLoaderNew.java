package de.timschubert.mediiva.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.Helper;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.exception.AuthenticationException;
import de.timschubert.mediiva.exception.ConnectionFailedException;
import de.timschubert.mediiva.exception.NoShareConnectionException;

public class SmbImageLoaderNew extends NetworkImageLoader
{

    private static final String TAG = "mediiva.smbimageloadernew";

    private boolean holdConnection;
    private long lastLibraryId;

    private final SMBClient smbClient;
    private final Context context;

    private Connection connection;
    private Session session;
    private DiskShare diskShare;

    private final List<Movie> currentlyLoadingMoviePosters;
    private final List<Movie> currentlyLoadingMovieFanArt;
    private final List<ImageSet> currentlyLoadingImageSetPosters;


    public SmbImageLoaderNew(Context context, Callback callback)
    {
        super(callback);

        this.context = context;
        holdConnection = false;
        lastLibraryId = -1;
        smbClient = new SMBClient();

        currentlyLoadingMoviePosters = new ArrayList<>();
        currentlyLoadingMovieFanArt = new ArrayList<>();
        currentlyLoadingImageSetPosters = new ArrayList<>();
    }

    @Override
    public void requestPoster(@NonNull Movie movie)
    {
        if(currentlyLoadingMoviePosters.contains(movie)) return;
        if(movie.getPoster() == null || movie.getPoster().getImagePath().isEmpty()) return;

        Log.v(TAG, "Requesting poster for movie: "+movie.getTitle());

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            Library movieLibrary =
                    AppDatabase.getInstance(context).libraryDao().getLibraryById(movie.getLibraryId());

            if(movieLibrary == null)
            {
                Log.w(TAG, "No library found for movie: "+movie.getTitle());
                return;
            }

            currentlyLoadingMoviePosters.add(movie);

            loadBitmap(movieLibrary, movie.getPoster().getImagePath(), bitmap ->
            {
                if(bitmap != null)
                {
                    getCallback().onPosterLoaded(movie, bitmap);
                }
                currentlyLoadingMoviePosters.remove(movie);
            });
        });
    }

    @Override
    public void requestFanArt(@NonNull Movie movie)
    {
        if(currentlyLoadingMovieFanArt.contains(movie)) return;
        if(movie.getFanArt() == null ||movie.getFanArt().getImagePath().isEmpty()) return;

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            Library movieLibrary =
                    AppDatabase.getInstance(context).libraryDao().getLibraryById(movie.getLibraryId());

            if(movieLibrary == null)
            {
                Log.w(TAG, "No library found for movie: "+movie.getTitle());
                return;
            }

            currentlyLoadingMovieFanArt.add(movie);

            loadBitmap(movieLibrary, movie.getFanArt().getImagePath(), bitmap ->
            {
                if(bitmap != null)
                {
                    getCallback().onFanArtLoaded(movie, bitmap);
                }
                currentlyLoadingMovieFanArt.remove(movie);
            });
        });
    }

    @Override
    public void requestActorThumb(@NonNull Actor actor, @NonNull ActorInfo actorInfo)
    {
        if(actorInfo.getThumbUri() == null) return;

        loadBitmapFromNetwork(actorInfo.getThumbUri(), bitmap ->
                getCallback().onActorThumbLoaded(actor, actorInfo, bitmap));
    }

    @Override
    public void requestCharacterThumb(@NonNull Character character)
    {
        if(character.getThumbUri() == null) return;

        loadBitmapFromNetwork(character.getThumbUri(), bitmap ->
                getCallback().onCharacterThumbLoaded(character, bitmap));
    }

    @Override
    public void requestArtistThumb(@NonNull Artist artist) {

    }

    @Override
    public void requestPoster(@NonNull ImageSet... imageSets)
    {
        boolean temporaryHoldConnection = holdConnection;
        holdConnection = true;

        for(ImageSet imageSet : imageSets)
        {
            if(currentlyLoadingImageSetPosters.contains(imageSet)) continue;
            if(imageSet.getPoster() == null || imageSet.getPoster().getImagePath().isEmpty()) continue;

            AppExecutors.getInstance().diskIO().execute(() ->
            {
                Library setLibrary =
                        AppDatabase.getInstance(context).libraryDao().getLibraryById(imageSet.getLibraryId());

                currentlyLoadingImageSetPosters.add(imageSet);

                loadBitmap(setLibrary, imageSet.getPoster().getImagePath(), bitmap ->
                {
                    if(bitmap != null)
                    {
                        getCallback().onPosterLoaded(imageSet, bitmap);
                    }
                    currentlyLoadingImageSetPosters.remove(imageSet);
                });
            });
        }

        holdConnection = temporaryHoldConnection;
        AppExecutors.getInstance().networkIO().execute(this::closeIfNeeded);
    }

    @Override
    public void requestPage(@NonNull ImageSet imageSet, @NonNull Page... pages)
    {
        boolean temporaryHoldConnection = holdConnection;
        holdConnection = true;

        for(Page page : pages)
        {
            if(page == null || page.getImagePath() == null || page.getImagePath().isEmpty()) continue;

            AppExecutors.getInstance().diskIO().execute(() ->
            {
                Library setLibrary =
                        AppDatabase.getInstance(context).libraryDao().getLibraryById(imageSet.getLibraryId());

                loadBitmap(setLibrary, page.getImagePath(), bitmap ->
                {
                    if(bitmap != null)
                    {
                        getCallback().onPageLoaded(imageSet, page, bitmap);
                    }
                });
            });
        }

        holdConnection = temporaryHoldConnection;
    }

    @Override
    public void requestHoldConnection()
    {
        holdConnection = true;
        // TODO authenticateIfNeeded(); no longer possible due to missing library info
    }

    @Override
    public void requestCloseConnection()
    {
        holdConnection = false;
        AppExecutors.getInstance().networkIO().execute(this::closeIfNeeded);
    }

    private void loadBitmap(Library library, String path, Consumer<Bitmap> onFinished)
    {
        AppExecutors.getInstance().networkIO().execute(() ->
        {
            authenticateIfNeeded(library);

            try
            {
                File testPosterFile = diskShare.openFile(path,
                        EnumSet.of(AccessMask.GENERIC_READ),
                        null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                        SMB2CreateDisposition.FILE_OPEN, null);

                InputStream fileInputStream = testPosterFile.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(fileInputStream));
                fileInputStream.close();

                onFinished.accept(bitmap);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Unable to get load file as bitmap: "+path);
                onFinished.accept(null);
            }

            closeIfNeeded();
        });
    }

    private void loadBitmapFromNetwork(Uri uri, Consumer<Bitmap> onFinished)
    {
        AppExecutors.getInstance().networkIO().execute(() ->
        {
            try
            {
                Bitmap thumbnail = Helper.loadFromUri(uri);
                onFinished.accept(thumbnail);
            }
            catch (IOException e)
            {
                Log.i(TAG, "Unable to load image from network: "+uri);
            }
        });
    }

    private void authenticateIfNeeded(Library library)
    {
        if(connected(library)) return;

        try
        {
            connection = smbClient.connect(library.getHostname());
        }
        catch (IOException e) { Log.w(TAG, "Unable to connect to smb share host: "+library.getHostname()); }

        try
        {
            AuthenticationContext authenticationContext =
                    new AuthenticationContext(library.getUsername(),
                            library.getPassword().toCharArray(), "");

            session = connection.authenticate(authenticationContext);
        }
        catch (Exception e) { Log.w(TAG, "Unable to authenticate smb share: " + e.toString()); }

        try
        {
            diskShare = (DiskShare) session.connectShare(library.getSmbShare());
            lastLibraryId = library.getId();
        }
        catch (Exception e) { Log.w(TAG, "Unable to connect to share: "+library.getSmbShare()); }
    }

    private void closeIfNeeded()
    {
        if(holdConnection) return;
        forceCloseConnection();
    }

    private void forceCloseConnection()
    {
        try
        {
            if(diskShare != null) diskShare.close();
            if(session != null) session.close();
            if(connection != null) connection.close();
            if(smbClient != null) smbClient.close();
        }
        catch (IOException e)
        {
            Log.w(TAG, "Error closing smb share: "+e.toString());
        }
    }

    private boolean connected(Library library)
    {
        return diskShare != null && diskShare.isConnected() && lastLibraryId == library.getId();
    }
}
