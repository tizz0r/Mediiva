package de.timschubert.mediiva.exo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.network.SmbReader;

public class SmbDataSource extends BaseDataSource
{

    private Library library;
    private Uri uri;
    private final String path;
    private SMBClient smbClient;
    private InputStream inputStream;
    private long bytesRemaining;

    protected SmbDataSource(DataSpec dataSpec, Library library)
    {
        super(true);

        uri = dataSpec.uri;
        path = dataSpec.uri.toString();
        this.library = library;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws IOException
    {
        smbClient = new SMBClient();
        bytesRemaining = C.LENGTH_UNSET;

        try
        {
            Connection connection = smbClient.connect(library.getHostname());

            AuthenticationContext authenticationContext = new AuthenticationContext(library.getUsername(),
                    library.getPassword().toCharArray(), "");
            Session session = connection.authenticate(authenticationContext);

            DiskShare diskShare = (DiskShare) session.connectShare(library.getSmbShare());

            File videoFile = diskShare.openFile(path,
                    EnumSet.of(AccessMask.GENERIC_READ),
                    null, EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN, null);

            inputStream = videoFile.getInputStream();

            long skipped = inputStream.skip(dataSpec.position);
            if(skipped < dataSpec.position)
            {
                throw new EOFException();
            }

            bytesRemaining = dataSpec.length;
        }
        catch (Exception e)
        {
            // TODO better log
            Log.w("mediiva.smbdatasource", "Can't auth: "+e.getLocalizedMessage());
        }

        return bytesRemaining;
    }

    @NonNull
    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws IOException
    {

        inputStream.close();
        smbClient.close();

        inputStream = null;
        smbClient = null;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException
    {
        if(length == 0) return 0;
        if(bytesRemaining == 0) return C.RESULT_END_OF_INPUT;

        int bytesRead = -1;

        try
        {
            int bytesToRead;
            if(bytesRemaining == C.LENGTH_UNSET)
            {
                bytesToRead = length;
            }
            else
            {
                bytesToRead = Math.min((int)bytesRemaining, length);
            }
            bytesRead = inputStream.read(buffer, offset, bytesToRead);
        }
        catch (IOException e)
        {
            Log.e("mediiva.smbdatasource", "error reading video file: "+e.toString());
        }

        if(bytesRead == -1)
        {
            if(bytesRemaining != C.LENGTH_UNSET) throw new IOException(new EOFException());
            return C.RESULT_END_OF_INPUT;
        }

        if(bytesRemaining != C.LENGTH_UNSET)
        {
            bytesRemaining -= bytesRead;
        }

        return bytesRead;
    }
}
