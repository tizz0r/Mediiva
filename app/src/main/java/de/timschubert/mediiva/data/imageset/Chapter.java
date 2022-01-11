package de.timschubert.mediiva.data.imageset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.timschubert.mediiva.exception.NoPageAvailableException;

public class Chapter
{
    private final int chapter;
    @Nullable private final Page poster;
    @Nullable private final String customName;
    @NonNull private final List<Page> pages;

    /**
     * Creates a new Chapter instance
     * @param chapter The number of the chapter in the image set
     * @param poster A Page object holding the poster of the image set if available
     * @param customName Custom name for the chapter to be displayed in the app
     */
    public Chapter(int chapter,
                   @Nullable Page poster,
                   @Nullable String customName)
    {
        this.chapter = chapter;
        this.poster = poster;
        this.customName = customName;
        pages = new ArrayList<>();
    }

    /**
     * Adds a page to the chapter and sorts the pages by their page number
     * @param page The page to be added
     */
    public void addPage(@NonNull Page page)
    {
        pages.add(page);
        pages.sort(Comparator.comparingInt(Page::getPage));
    }

    /**
     * Returns the poster or the page with the lowest page index in the chapter if the poster is not set.
     * @return Poster or first image in the chapter
     * @throws NoPageAvailableException If there is no poster or page available
     */
    @NonNull public Page getFirstPage() throws NoPageAvailableException
    {
        if(poster != null) return poster;
        if(pages.isEmpty()) throw new NoPageAvailableException();
        return pages.get(0);
    }

    /**
     * Return the page with the highest page index in the chapter. Might be the poster if there is only one entry.
     * @return Poster or last image in chapter
     * @throws NoPageAvailableException If there is no poster or page available
     */
    @NonNull public Page getLastPage() throws NoPageAvailableException
    {
        if(pages.isEmpty())
        {
            if(poster != null) return poster;
            throw new NoPageAvailableException();
        }

        return pages.get(pages.size()-1);
    }

    /**
     * Returns the next page in the chapter for the given page
     * @param page The page of which you would like the next page
     * @return The next page in the chapter
     * @throws NoPageAvailableException Thrown if the supplied page is not part of the chapter or if there is no next page available
     */
    @NonNull public Page getNext(@Nullable Page page) throws NoPageAvailableException
    {
        // If the provided page is the poster, return the first page in the pages list
        if(Objects.equals(page, poster)) return firstNonPoster();

        if(!pages.contains(page)) throw new NoPageAvailableException();

        if(pages.size() > pages.indexOf(page) + 1) return pages.get(pages.indexOf(page)+1);
        throw new NoPageAvailableException();
    }

    /**
     * Returns the previous page in the chapter for the given page. Might be the poster
     * @param page The page of which you would like the previous page
     * @return The previous page in the chapter or the poster if there are no further previous pages available
     * @throws NoPageAvailableException Thrown if the supplied page is not part of the chapter or if there are no further previous pages available
     */
    @NonNull public Page getPrevious(@Nullable Page page) throws NoPageAvailableException
    {
        if(!pages.contains(page)) throw new NoPageAvailableException();

        if(pages.indexOf(page) == 0 && poster != null) return poster;

        if(pages.indexOf(page) > 0) return pages.get(pages.indexOf(page) - 1);
        throw new NoPageAvailableException();
    }

    /**
     * Return the page with the given page index. If multiple pages have the same page index, the first one is chosen.
     * @param page The page number index, or zero for the poster
     * @throws NoPageAvailableException Throws NoPageAvailableException if the requested page is not part of the chapter
     * @return The requested page or poster
     */
    @NonNull public Page getPage(int page) throws NoPageAvailableException
    {
        // Skip query if poster is requested
        if(page == 0 && poster != null) return poster;

        for(Page imagePage : pages)
        {
            if(imagePage.getPage() == page) return imagePage;
        }

        throw new NoPageAvailableException();
    }

    /**
     * Returns the first page in the chapter which is not the poster
     * @return The first page in the chapter
     * @throws NoPageAvailableException Thrown if there are no non-poster pages in the chapter
     */
    @NonNull
    private Page firstNonPoster() throws NoPageAvailableException
    {
        if(pages.isEmpty()) throw new NoPageAvailableException();

        return pages.get(0);
    }

    public int getChapter() { return chapter; }
    @Nullable public String getCustomName() { return customName; }
    @Nullable public Page getPoster() { return poster; }
    @NonNull public List<Page> getPages() { return pages; }
}
