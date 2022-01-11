package de.timschubert.mediiva.dataloader.xml;

import android.net.Uri;
import android.util.Xml;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public abstract class MediivaXMLParser
{

    public void parse(InputStream is) throws IOException, XmlPullParserException
    {
        try
        {
            XmlPullParser pullParser = Xml.newPullParser();
            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            pullParser.setInput(is, null);
            pullParser.nextTag();

            readInputStream(pullParser);
        }
        finally
        {
            is.close();
        }
    }

    private void readInputStream(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        onBeginParse();

        pullParser.require(XmlPullParser.START_TAG, null, getRootTag());

        while(pullParser.next() != XmlPullParser.END_TAG)
        {
            if(pullParser.getEventType() != XmlPullParser.START_TAG) continue;

            String name = pullParser.getName();
            onProcessTag(name, pullParser);
        }

        onEndParse();
    }

    protected void skip(XmlPullParser pullParser) throws XmlPullParserException, IOException
    {
        if(pullParser.getEventType() != XmlPullParser.START_TAG) throw new IllegalStateException();

        int depth = 1;
        while(depth != 0)
        {
            switch (pullParser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    protected String readTagAsString(String tagName, XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        pullParser.require(XmlPullParser.START_TAG, null, tagName);
        String string = readText(pullParser);
        pullParser.require(XmlPullParser.END_TAG, null, tagName);

        pullParser.getAttributeValue("", "");

        return string;
    }

    protected int readTagAsInt(String tagName, XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        return Integer.parseInt(readTagAsString(tagName, pullParser));
    }

    protected Uri readTagAsUri(String tagName, XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        return Uri.parse(readTagAsString(tagName, pullParser));
    }

    private String readText(XmlPullParser pullParser) throws IOException, XmlPullParserException
    {
        String result = "";

        if(pullParser.next() == XmlPullParser.TEXT)
        {
            result = pullParser.getText();
            pullParser.nextTag();
        }

        return result;
    }

    @NonNull abstract String getRootTag();
    abstract void onBeginParse();
    abstract void onProcessTag(String tagName, XmlPullParser pullParser);
    abstract void onEndParse();
}
