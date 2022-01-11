package de.timschubert.mediiva.dataloader;

import de.timschubert.mediiva.data.library.Library;

public abstract class LibraryLoader<T>
{
    private final Callback<T> callback;

    public LibraryLoader(Callback<T> callback) { this.callback = callback; }

    protected Callback<T> getCallback() { return callback; }

    public abstract void searchForNewItemsAsync(Library library);

    public interface Callback<T>
    {
        void onItemAdded(T item);
    }
}
