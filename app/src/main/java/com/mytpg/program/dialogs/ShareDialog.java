package com.mytpg.program.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.mytpg.engines.entities.interfaces.IShareDialogListener;
import com.mytpg.program.R;

/**
 * Created by stalker-mac on 19.10.16.
 */

public class ShareDialog extends AlertDialog {
    private IShareDialogListener mShareDialogListener = null;

    public static ShareDialog newInstance(Context argContext)
    {
        return new ShareDialog(argContext,true,true,true);
    }

    public static ShareDialog newInstance(Context argContext,  boolean argHasTextShare, boolean argHasLinkShare, boolean argHasImageShare)
    {
        return new ShareDialog(argContext, argHasTextShare, argHasLinkShare, argHasImageShare);
    }

    protected ShareDialog(@NonNull Context context, boolean argHasTextShare, boolean argHasLinkShare, boolean argHasImageShare) {
        super(context);

        setTitle(context.getString(R.string.action_share));
        setMessage(context.getString(R.string.which_share_format));
        if (argHasLinkShare) {
            setButton(BUTTON_POSITIVE, context.getString(R.string.link), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mShareDialogListener != null) {
                        mShareDialogListener.onLinkClicked();
                    }
                }
            });
        }
        if (argHasImageShare) {
            setButton(BUTTON_NEUTRAL, context.getString(R.string.image), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mShareDialogListener != null) {
                        mShareDialogListener.onImageClicked();
                    }
                }
            });
        }
        if (argHasTextShare) {
            setButton(BUTTON_NEGATIVE, context.getString(R.string.text), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mShareDialogListener != null) {
                        mShareDialogListener.onTextClicked();
                    }
                }
            });
        }
    }

    public void setShareDialogListener(IShareDialogListener argShareDialogListener)
    {
        this.mShareDialogListener = argShareDialogListener;
    }
}
