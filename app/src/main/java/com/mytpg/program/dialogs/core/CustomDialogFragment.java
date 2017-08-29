package com.mytpg.program.dialogs.core;


import android.support.v4.app.DialogFragment;

import com.mytpg.program.MainActivity;
import com.mytpg.program.core.App;

/**
 * Created by stalker-mac on 20.11.14.
 */
public class CustomDialogFragment extends DialogFragment {

    public App getApp()
    {
        App app = null;

        if (getMainActivity() != null)
        {
            app = getMainActivity().getApp();
        }

        return app;
    }

    public MainActivity getMainActivity()
    {
        MainActivity mainActivity = null;

        if (getActivity() instanceof MainActivity)
        {
            mainActivity = (MainActivity) getActivity();
        }

        return mainActivity;
    }
}
