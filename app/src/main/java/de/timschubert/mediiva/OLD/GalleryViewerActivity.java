package de.timschubert.mediiva.OLD;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.timschubert.mediiva.AppExecutors;
import de.timschubert.mediiva.R;
import de.timschubert.mediiva.ViewerMain;
import de.timschubert.mediiva.data.AppDatabase;
import de.timschubert.mediiva.data.imageset.Artist;
import de.timschubert.mediiva.data.imageset.Chapter;
import de.timschubert.mediiva.data.imageset.Character;
import de.timschubert.mediiva.data.imageset.ImageSet;
import de.timschubert.mediiva.data.imageset.ImageSetDao;
import de.timschubert.mediiva.data.imageset.Page;
import de.timschubert.mediiva.data.movie.Actor;
import de.timschubert.mediiva.data.movie.ActorInfo;
import de.timschubert.mediiva.data.movie.Movie;
import de.timschubert.mediiva.exception.NoChapterAvailableException;
import de.timschubert.mediiva.exception.NoPageAvailableException;

public class GalleryViewerActivity extends AppCompatActivity
{

    @NonNull private ViewerMain viewerMain;
    @NonNull private List<Bitmap> loadedBitmaps;
    @NonNull private ImageSwitcher imageSwitcher;

    private ImageSet imageSet;

    private TextView pageTextView;
    private TextView chapterTextView;

    //TODO change boolean to something more sophisticated
    private boolean showNextAnim = true;

    private Chapter currentChapter;
    private Page currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallerynew);

        loadedBitmaps = new ArrayList<>();

        viewerMain = ViewerMain.getInstance(this);
        viewerMain.addCallback(new ViewerListener());

        long imageSetId = getIntent().getLongExtra("imageset_id", -1L);

        AppExecutors.getInstance().diskIO().execute(() ->
        {
            viewerMain.keepConnection(true);

            ImageSetDao imageSetDao = AppDatabase.getInstance(this).imageSetDao();
            imageSet = imageSetDao.getImageSetById(imageSetId);

            pageTextView = findViewById(R.id.activity_gallerynew_pagetext);
            chapterTextView = findViewById(R.id.activity_gallerynew_chaptertext);

            imageSwitcher = findViewById(R.id.activity_gallerynew_imageviewswitcher);
            initSwitcher();

            if(savedInstanceState == null)
            {
                requestFirstPage();
            }
            else
            {
                int chapter = savedInstanceState.getInt("key_current_chapter", -1);
                int page = savedInstanceState.getInt("key_current_page", -1);
                if(chapter == -1 || page == -1)
                {
                    requestFirstPage();
                }
                else
                {
                    try
                    {
                        Chapter selectedChapter = imageSet.getChapter(chapter);

                        requestPage(selectedChapter.getPage(page), selectedChapter);
                    } catch (NoPageAvailableException | NoChapterAvailableException e) {
                        requestFirstPage();
                    }
                }
            }

            ImageButton nextPageButton = findViewById(R.id.activity_gallerynew_next);
            nextPageButton.setOnClickListener(view -> onNextClicked());

            ImageButton previousPageButton = findViewById(R.id.activity_gallerynew_before);
            previousPageButton.setOnClickListener(view -> onBeforeClicked());

            ImageButton chaptersButton = findViewById(R.id.activity_gallerynew_chapterskip);
            chaptersButton.setOnClickListener(view -> {

                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);

                for(Chapter chapter : imageSet.getChapters())
                {
                    popupMenu.getMenu().add(0, chapter.getChapter(), chapter.getChapter(), "Chapter "+chapter.getChapter());
                }
                popupMenu.setOnMenuItemClickListener(menuItem ->
                {
                    try
                    {
                        //TODO handle currentImage = null
                    int chapter = menuItem.getItemId();
                    showNextAnim = chapter > currentChapter.getChapter();



                        Page firstImageOfChapter = imageSet.getChapter(chapter).getFirstPage();
                        requestPage(firstImageOfChapter, imageSet.getChapter(chapter));

                        return true;
                    } catch (Exception e)
                    {
                        return false; //TODO verbose
                    }
                });
                popupMenu.show();
            });
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putInt("key_current_chapter", currentChapter.getChapter());
        outState.putInt("key_current_page", currentPage.getPage());
    }

    private void requestFirstPage()
    {
        if(imageSet.getChapters().isEmpty()) return;

        try {
            requestPage(imageSet.getFirstChapter().getFirstPage(), imageSet.getFirstChapter());
        } catch (NoPageAvailableException | NoChapterAvailableException e) {
            e.printStackTrace(); //TODO
        }
    }

    private void onNextClicked()
    {
        try
        {
            requestPage(currentChapter.getNext(currentPage), currentChapter);
        }
        catch (NoPageAvailableException e)
        {
            try
            {
                Log.v("mediiva.gelleryvieweractivity", "End of chapter"+currentChapter.getChapter()+" reached, requesting next chapter");
                Chapter nextChapter = imageSet.getNextChapter(currentChapter);
                Log.v("mediiva.gelleryvieweractivity", "Next chapter: "+nextChapter.getChapter());
                requestPage(nextChapter.getFirstPage(), nextChapter);
            }
            catch (NoChapterAvailableException | NoPageAvailableException ignore) { return; }
        }

        showNextAnim = true;
    }

    private void onBeforeClicked()
    {
        try
        {
            requestPage(currentChapter.getPrevious(currentPage), currentChapter);
        }
        catch (NoPageAvailableException e)
        {
            try
            {
                Chapter previousChapter = imageSet.getPreviousChapter(currentChapter);
                requestPage(previousChapter.getLastPage(), previousChapter);
            }
            catch (NoChapterAvailableException | NoPageAvailableException ignore) { return; }
        }

        showNextAnim = false;
    }

    private void requestPage(Page page, Chapter chapter)
    {
        if(page.equals(currentPage)) return; //TODO verbose

        viewerMain.requestImageLoad(imageSet, page);
        currentPage = page;
        currentChapter = chapter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewerMain.keepConnection(false);
        for(Bitmap bitmap : loadedBitmaps)
        {
            bitmap.recycle();
        }
    }

    private void initSwitcher()
    {
        AppExecutors.getInstance().mainThread().execute(() ->
        {
            Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_right_to_left_in);
            Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_right_to_left_out);

            imageSwitcher.setInAnimation(inAnimation);
            imageSwitcher.setOutAnimation(outAnimation);
            imageSwitcher.setFactory(() ->
            {
                ImageView imageView = new ImageView(GalleryViewerActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return imageView;
            });
        });
    }

    public void setImage(Page page, Bitmap bitmap)
    {
        pageTextView.setText(page.getPage() +" / "+currentChapter.getPages().size());
        chapterTextView.setText("Chapter "+currentChapter.getChapter());

        if(showNextAnim)
        {
            imageSwitcher.setInAnimation(this, R.anim.anim_right_to_left_in);
            imageSwitcher.setOutAnimation(this, R.anim.anim_right_to_left_out);
        }
        else
        {
            imageSwitcher.setInAnimation(this, R.anim.anim_left_to_right_in);
            imageSwitcher.setOutAnimation(this, R.anim.anim_left_to_right_out);
        }

        imageSwitcher.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

        //ImageView imageView = findViewById(R.id.activity_gallerynew_imageview);
        //imageView.setImageBitmap(bitmap);
    }

    private class ViewerListener extends ViewerMain.Callback {
        @Override
        public void onPageLoaded(ImageSet imageSet, Page page, Bitmap pageBitmap)
        {
            runOnUiThread(() -> setImage(page, pageBitmap));
        }
    }
}
