package com.mytpg.engines.entities.managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mytpg.engines.R;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.tools.ColorTools;
import com.mytpg.engines.tools.SizeTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stalker-mac on 08.11.16.
 */

public class TutorialManager {
    private Context mContext = null;
    private int mCurrentCounter = 0;
    private ShowcaseView mShowcaseView = null;
    private List<Tutorial> mTutorials = new ArrayList<>();

    public TutorialManager(Context argContext)
    {
        mContext = argContext;
        init();
    }

    public ShowcaseView getShowcaseView()
    {
        return mShowcaseView;
    }

    public List<Tutorial> getTutorials()
    {
        return mTutorials;
    }

    private void init()
    {
        mCurrentCounter = 0;
    }

    public void launchTutorials()
    {
        if (mTutorials.isEmpty())
        {
            return;
        }

        init();
        showTuto();

        mShowcaseView.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentCounter++;
                if (mCurrentCounter >= mTutorials.size())
                {
                    mShowcaseView.hide();
                    mShowcaseView = null;
                    return;
                }

                showTuto();
            }
        });
    }

    private void showTuto() {
        if (mCurrentCounter < 0 || mCurrentCounter >= getTutorials().size())
        {
            return;
        }

        if (mCurrentCounter == 0)
        {
            setShowcaseView(null);
        }
        Tutorial tuto = getTutorials().get(mCurrentCounter);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      /*  layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);*/
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (tuto.isButtonBottomRight())
        {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        else
        {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }

        int margin = SizeTools.dpToPx(mContext,16);
        layoutParams.setMargins(margin,margin,margin,margin);

        mShowcaseView.setButtonPosition(layoutParams);

        if (tuto.getView() == null)
        {
            mShowcaseView.setShowcase(Target.NONE, true);
        }
        else {
            mShowcaseView.setShowcase(new ViewTarget(tuto.getView()), true);
        }
        mShowcaseView.setContentTitle(tuto.getTitle());
        mShowcaseView.setContentText(tuto.getContent());

        if (mCurrentCounter < mTutorials.size() -1)
        {
            mShowcaseView.setButtonText(mContext.getString(R.string.next));
        }
        else
        {
            mShowcaseView.setButtonText(mContext.getString(android.R.string.ok));
        }



        if (!mShowcaseView.isShowing())
        {
            mShowcaseView.show();
        }
    }

    public void setShowcaseView(ShowcaseView argShowcaseView)
    {
        if (argShowcaseView == null)
        {
            argShowcaseView = new ShowcaseView.Builder((Activity)mContext).withHoloShowcase().build();
        }

        mShowcaseView = argShowcaseView;

        mShowcaseView.setBackgroundColor(ColorTools.adjustAlpha(Color.BLACK,0.6f));
    }

    public void setTutorials(List<Tutorial> argTutorials)
    {
        if (argTutorials == null)
        {
            argTutorials = new ArrayList<>();
        }

        mTutorials = argTutorials;
    }

    public void addTutorial(Tutorial argTutorial) {
        if (argTutorial == null)
        {
            return;
        }

        if (mTutorials == null)
        {
            mTutorials = new ArrayList<>();
        }

        mTutorials.add(argTutorial);
    }

    public void setContext(Context argContext) {
        mContext = argContext;
    }
}
