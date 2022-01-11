package de.timschubert.mediiva.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.EnumSet;
import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.Helper;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.exception.AuthenticationException;
import de.timschubert.mediiva.exception.ConnectionFailedException;
import de.timschubert.mediiva.exception.NoShareConnectionException;

public class SmbImageLoader extends NetworkImageLoader
{

    private static final String HOSTNAME = "SmbReader.HOSTNAME"; //TODO
    private static final String SHARE = "SmbReader.SHARE"; // TODO
    private static final String USERNAME = "SmbReader.USERNAME"; //TODO
    private static final String PASSWORD = "SmbReader.PASSWORD"; //TODO

    private boolean holdConnection;

    private final AuthenticationContext authenticationContext;
    private final SMBClient smbClient;

    private Connection connection;
    private Session session;
    private DiskShare diskShare;

    public SmbImageLoader(Callback callback)
    {
        super(callback);

        smbClient = new SMBClient();
        authenticationContext = new AuthenticationContext(USERNAME,
                PASSWORD.toCharArray(), "");

        holdConnection = false;
    }

    @Override
    public void requestPoster(@NonNull ImageSet... imageSet) {

    }

    private void authenticate() throws ConnectionFailedException, AuthenticationException, NoShareConnectionException
    {
        try
        {
            connection = smbClient.connect(HOSTNAME);
        }
        catch (IOException e) { throw new ConnectionFailedException(); }

        try
        {
            session = connection.authenticate(authenticationContext);
        }
        catch (Exception e) { throw new AuthenticationException(); }

        try
        {
            diskShare = (DiskShare) session.connectShare(SHARE);
        }
        catch (Exception e) { throw new NoShareConnectionException(); }
    }

    private void closeConnection() throws IOException
    {
        diskShare.close();
        session.close();
        connection.close();
    }

    private void authenticateIfNecessary()
    {

    }

    private boolean connected()
    {
        return diskShare != null && diskShare.isConnected();
    }

    //@Override
    public void requestPoster(@NonNull ImageSet imageSet)
    {
        if(imageSet.getPoster().getImagePath() == null || imageSet.getPoster().getImagePath().isEmpty()) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            if(!connected()) {
                try {
                    authenticate();
                } catch (ConnectionFailedException | AuthenticationException | NoShareConnectionException e) {
                    e.printStackTrace(); //TODO
                    return;
                }
            }

            Bitmap poster = loadImage(imageSet.getPoster().getImagePath());
            getCallback().onPosterLoaded(imageSet, poster);

            if(!holdConnection) {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO
                }
            }
        });
    }

    @Override
    public void requestPoster(@NonNull Movie movie)
    {
        if(movie.getPoster() == null || movie.getPoster().getImagePath().isEmpty()) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            if(!connected()) {
                try {
                    authenticate();
                } catch (ConnectionFailedException | AuthenticationException | NoShareConnectionException e) {
                    e.printStackTrace(); //TODO
                    return;
                }
            }

            Bitmap poster = loadImage(movie.getPoster().getImagePath());
            getCallback().onPosterLoaded(movie, poster);

            if(!holdConnection) {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO
                }
            }
        });
    }

    @Override
    public void requestFanArt(@NonNull Movie movie)
    {
        if(movie.getFanArt() == null || movie.getFanArt().getImagePath().isEmpty()) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            if(!connected()) {
                try {
                    authenticate();
                } catch (ConnectionFailedException | AuthenticationException | NoShareConnectionException e) {
                    e.printStackTrace(); //TODO
                    return;
                }
            }

            Bitmap fanArt = loadImage(movie.getFanArt().getImagePath());
            getCallback().onFanArtLoaded(movie, fanArt);

            if(!holdConnection) {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO
                }
            }
        });
    }

    @Override
    public void requestActorThumb(@NonNull Actor actor, @NonNull ActorInfo actorInfo)
    {
        if(actorInfo.getThumbUri() == null) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            try
            {
                Bitmap thumbnail = Helper.loadFromUri(actorInfo.getThumbUri());
                getCallback().onActorThumbLoaded(actor, actorInfo, thumbnail);
            }
            catch (IOException e)
            {
                Log.i("mediiva.smbimageloader", "Unable to load thumb for actor: "+actor.getName());
            }
        });
    }

    @Override
    public void requestCharacterThumb(@NonNull Character character)
    {
        if(character.getThumbUri() == null) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            try
            {
                Bitmap thumbnail = Helper.loadFromUri(character.getThumbUri());
                getCallback().onCharacterThumbLoaded(character, thumbnail);
            }
            catch (IOException e)
            {
                Log.i("mediiva.smbimageloader", "Unable to load thumb for character: "+character.getName());
            }
        });
    }

    @Override
    public void requestArtistThumb(@NonNull Artist artist)
    {
        if(artist.getThumbUri() == null) return;

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            try
            {
                Bitmap thumbnail = Helper.loadFromUri(artist.getThumbUri());
                getCallback().onArtistThumbLoaded(artist, thumbnail);
            }
            catch (IOException e)
            {
                Log.i("mediiva.smbimageloader", "Unable to load thumb for artist: "+artist.getName());
            }
        });
    }

    @Override
    public void requestPage(@NonNull ImageSet imageSet, @NonNull Page... page) {

    }

    private Bitmap loadImage(String path)
    {
        try
        {
            File testPosterFile = diskShare.openFile(path,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN, null);

            try
            {
                InputStream testPosterInputStream = testPosterFile.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(testPosterInputStream));
                testPosterInputStream.close();

                return bitmap;
            }
            catch (Exception e)
            {
                Log.w("mediiva.test", "Couldn't get poster: "+path+" "+e.getLocalizedMessage());
            }
        }
        catch (Exception e)
        {
            Log.e("mediiva.smbimageloader", "Error reading poster for: "+path+" "+e.getLocalizedMessage());
        }

        return null;
    }

    //@Override
    public void requestPage(@NonNull ImageSet imageSet, @NonNull Page page)
    {
        Log.v("mediiva.smbimageloader", "Requesting page "+page.getPage()+" for image set: "+imageSet.getTitle());

        AppExecutors.getInstance().networkIO().execute(() ->
        {
            if(!connected()) {
                try {
                    authenticate();
                } catch (ConnectionFailedException | AuthenticationException | NoShareConnectionException e) {
                    e.printStackTrace(); //TODO
                    return;
                }
            }

            File imageFile = diskShare.openFile(page.getImagePath(),
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN, null);

            try
            {
                InputStream imageInputStream = imageFile.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(imageInputStream));
                imageInputStream.close();

                getCallback().onPageLoaded(imageSet, page, bitmap);
            }
            catch (Exception e)
            {
                Log.w("smbviewer.smbget", "Couldn't get image: "+page.getImagePath()+" "+e.getLocalizedMessage());
            }

            if(!holdConnection) {
                try {
                    closeConnection();
                } catch (IOException e) {
                    e.printStackTrace(); //TODO
                }
            }
        });
    }

    @Override
    public void requestHoldConnection()
    {
        try {
            authenticate();
        } catch (ConnectionFailedException | AuthenticationException | NoShareConnectionException e)
        {
            e.printStackTrace(); // TODO better handling
        }
        holdConnection = true;
    }

    @Override
    public void requestCloseConnection()
    {
        AppExecutors.getInstance().networkIO().execute(() ->
        {
            try {
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace(); // TODO better handling
            }
        });

        holdConnection = false;
    }
}
