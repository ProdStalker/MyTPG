package com.mytpg.engines.entities;

import android.view.View;

/**
 * Created by stalker-mac on 08.11.16.
 */

public class Tutorial {
    private String mContent = "";
    private String mTitle = "";
    private View mView = null;
    private boolean mIsButtonBottomRight = true;

    public Tutorial(View argView)
    {
        mView = argView;
    }

    public Tutorial(String argTitle, String argContent)
    {
        mContent = argContent;
        mTitle = argTitle;
    }

    public Tutorial(String argTitle, String argContent, View argView)
    {
        mContent = argContent;
        mTitle = argTitle;
        mView = argView;
    }

    public String getContent()
    {
        return mContent;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public View getView()
    {
        return mView;
    }

    public boolean isButtonBottomRight()
    {
        return mIsButtonBottomRight;
    }

    public void setButtonBottomRight(boolean argIsButtonBottomRight)
    {
        mIsButtonBottomRight = argIsButtonBottomRight;
    }

    public void setContent(String argContent)
    {
        if (argContent == null)
        {
            argContent = "";
        }

        mContent = argContent;
    }

    public void setTitle(String argTitle)
    {
        if (argTitle == null)
        {
            argTitle = "";
        }

        mTitle = argTitle;
    }

    public void setView(View argView)
    {
        mView = argView;
    }
}
