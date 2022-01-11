package de.timschubert.mediiva.ui.libraries;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.library.Library;
import de.timschubert.mediiva.data.library.LibraryDao;
import de.timschubert.mediiva.databinding.FragmentLibrariesOverviewBinding;
import de.timschubert.mediiva.ui.adapter.LibraryAdapter;
import de.timschubert.mediiva.ui.dialog.NewLibraryDialog;

public class LibrariesFragment extends Fragment
{

    private FragmentLibrariesOverviewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = FragmentLibrariesOverviewBinding.inflate(inflater, container, false);

        try {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Libraries"); // TODO
        }
        catch (NullPointerException ignore) {}

        binding.fragmentLibrariesOverviewFab.setOnClickListener(view ->
        {
            NewLibraryDialog newLibraryFragment = new NewLibraryDialog();
            newLibraryFragment.show(getChildFragmentManager(), "");
            newLibraryFragment.setCallback(new NewLibraryCallback());
        });

        AppExecutors.getInstance().diskIO().execute(this::queryLibraries);
        return binding.getRoot();
    }

    /**
     * Queries available libraries from the Room database and calls setupRecyclerView(). Do not run on ui thread
     */
    private void queryLibraries()
    {
        LibraryDao libraryDao = AppDatabase.getInstance(getContext()).libraryDao();
        List<Library> libraries = libraryDao.getLibraries();
        AppExecutors.getInstance().mainThread().execute(() -> setupRecyclerView(libraries));
    }

    /**
     * Adds all already existing libraries to the view. Execute on ui thread
     * @param allLibraries Libraries to be added
     */
    private void setupRecyclerView(List<Library> allLibraries)
    {
        LibraryAdapter libraryAdapter = new LibraryAdapter(allLibraries);
        binding.fragmentLibrariesOverviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.fragmentLibrariesOverviewRecyclerView.setAdapter(libraryAdapter);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    private class NewLibraryCallback implements NewLibraryDialog.Callback
    {
        @Override
        public void onSend(Library.Type type,
                           String name,
                           String hostname,
                           String smbShare,
                           String path,
                           String username,
                           String password)
        {
            AppExecutors.getInstance().diskIO().execute(() ->
            {
                LibraryDao libraryDao = AppDatabase.getInstance(requireContext()).libraryDao();

                Library newLibrary = new Library(name, hostname, smbShare, username, password,
                        path, type);

                if(libraryDao.libraryOnSamePathExists(hostname, smbShare, path, type))
                {
                    // TODO show toast or something with string resource
                    AppExecutors.getInstance().mainThread().execute(() -> Toast.makeText(requireContext(), "This library already exists", Toast.LENGTH_SHORT).show());
                    return;
                }

                AppExecutors.getInstance().mainThread().execute(() -> Toast.makeText(requireContext(), "New library created", Toast.LENGTH_SHORT).show());
                libraryDao.insertLibrary(newLibrary);
                // TODO ViewerMain.getInstance(requireContext()).onNewLibrary();
            });
        }
    }
}
