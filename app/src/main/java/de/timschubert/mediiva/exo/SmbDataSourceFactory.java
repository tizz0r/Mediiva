package de.timschubert.mediiva.exo;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import de.timschubert.mediiva.data.library.Library;

public class SmbDataSourceFactory implements DataSource.Factory
{

    private final Library library;
    private final String path;

    public SmbDataSourceFactory(Library library, String path)
    {
        this.library = library;
        this.path = path;
    }

    @NonNull
    @Override
    public DataSource createDataSource()
    {
        DataSpec dataSpec = new DataSpec(Uri.parse(path));
        return new SmbDataSource(dataSpec, library);
    }
}
