package de.timschubert.mediiva.data.imageset;

import androidx.annotation.NonNull;

public class Page
{
    private final int pageNumber;
    @NonNull private final String imagePath;

    /**
     * Creates a new Page instance
     * @param pageNumber The number of the page in a given chapter
     * @param imagePath String pointing to the path of the image file
     */
    public Page(int pageNumber,
                @NonNull String imagePath)
    {
        this.pageNumber = pageNumber;
        this.imagePath = imagePath;
    }

    public int getPage() { return pageNumber; }
    @NonNull public String getImagePath() { return imagePath; }
}
