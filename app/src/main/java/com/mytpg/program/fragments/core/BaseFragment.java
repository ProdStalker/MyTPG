package com.mytpg.program.fragments.core;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.mytpg.engines.entities.core.EntityWithName;
import com.mytpg.engines.entities.managers.TutorialManager;
import com.mytpg.engines.settings.RequestCodeSettings;
import com.mytpg.program.MainActivity;
import com.mytpg.program.Manifest;
import com.mytpg.program.R;
import com.mytpg.program.core.App;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by stalker-mac on 16.08.16.
 */
public abstract class BaseFragment extends Fragment {
    protected FloatingActionButton m_fab = null;
    protected ProgressDialog mPD = null;
    protected String mWantsSearchText = "";
    protected SwipeRefreshLayout mSwipeRefreshLayout = null;
    protected TutorialManager mTutorialManager = null;

    public BaseFragment()
    {
        super();
    }

    public BaseFragment(String argSearchText)
    {
        super();
        mWantsSearchText = argSearchText;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mTutorialManager == null)
        {
            mTutorialManager = new TutorialManager(context);
        }
        else
        {
            mTutorialManager.setContext(context);
        }
        updateToolbarMenu();
        updateFab();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getMainActivity() != null)
        {
            getMainActivity().updateSuperLayout();
            updateFabVisibility(fabNeeded());
            updateTitle();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateTitle();
    }

    private void updateTitle() {
        setTitle(getTitle());
    }

    protected boolean fabNeeded()
    {
        return false;
    }

    public String getTitle()
    {
        return getString(R.string.app_name);
    }

    protected App getApp()
    {
        if (getContext() != null)
        {
            if (getContext().getApplicationContext() instanceof App)
            {
                return (App)getContext().getApplicationContext();
            }

        }

        return null;
    }

    protected MainActivity getMainActivity()
    {
        if (getActivity() != null)
        {
            return (MainActivity)getActivity();
        }

        return null;
    }

    public abstract void search(String argSearchText);

    public void setFab(FloatingActionButton argFab)
    {
        this.m_fab = argFab;
    }

    public void showPD()
    {
        dismissPD();
        if (getActivity() == null)
        {
            return;
        }

        mPD = new ProgressDialog(getActivity());
        mPD.setMessage(getString(R.string.loading));
        mPD.setCancelable(false);
        mPD.show();
    }

    public void dismissPD()
    {
        if (mPD != null)
        {
            if (mPD.isShowing()) {
                mPD.dismiss();
            }
            mPD = null;
        }
    }

    public void updateFab()
    {
        if (m_fab != null) {
            m_fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissPD();
        dismissSwipeRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissPD();
        dismissSwipeRefresh();
    }

    public void dismissSwipeRefresh() {
        if (mSwipeRefreshLayout != null)
        {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void updateToolbarMenu()
    {
        if (getMainActivity() != null)
        {
            getMainActivity().hideActionsToolbarMenu();
        }
    }

    public void setTitle(String argTitle) {
        getActivity().setTitle(argTitle);
    }

    protected void changeFragment(Fragment argBaseFragment, boolean argCanBackStack) {
        if (getMainActivity() != null)
        {
            getMainActivity().changeFragment(argBaseFragment, argCanBackStack);
        }
    }

    public boolean needToBeFullViewport() {
        return true;
    }

    public boolean canDynamicSearch()
    {
        return true;
    }

    public void fabClicked()
    {

    }

    public void updateFabDrawable(int argResourceId)
    {
        if (m_fab == null)
        {
            return;
        }

        m_fab.setImageResource(argResourceId);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void updateFabVisibility(boolean argIsVisible)
    {
        if (m_fab == null)
        {
            return;
        }

        if (argIsVisible)
        {
            m_fab.setVisibility(View.VISIBLE);
        }
        else
        {
            m_fab.setVisibility(View.GONE);
        }
    }

    protected boolean askLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION))
            {
                AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.permlab_proximityLoc))
                        .setMessage(getString(R.string.permdesc_proximityLoc))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        RequestCodeSettings.REQ_PERMISSION_FINE_LOCATION);
                            }
                        })
                        .setCancelable(false)
                        .create();

                ad.show();
            }
            else
            {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        RequestCodeSettings.REQ_PERMISSION_FINE_LOCATION);
            }

        }
        else
        {
            return true;
        }

        return false;
    }

    protected void shareImage()
    {
        View rootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = getScreenShot(rootView);
        Date now = new Date();
        String fileName = DateFormat.format("yyyy-MM-dd_hh:mm:ss", now) + ".png";

        store(bitmap, fileName, true);
    }

    protected void shareImage(File file)
    {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My TPG");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Créé par My TPG sur Android");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case RequestCodeSettings.REQ_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    try {
                        shareImage();
                    }
                    catch (SecurityException se)
                    {
                        se.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),getString(R.string.unable_loading),Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    public void store(Bitmap bm, String fileName,boolean isShareToo){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.permlab_externalStorage))
                            .setMessage(getString(R.string.permdesc_externalStorage))
                            .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            RequestCodeSettings.REQ_PERMISSION_WRITE_EXTERNAL_STORAGE);
                                }
                            })
                            .setCancelable(false)
                            .create();

                    ad.show();
                }
                else
                {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            RequestCodeSettings.REQ_PERMISSION_WRITE_EXTERNAL_STORAGE);
                }

                return;
            }
        }



        final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/My TPG";
        File dir = new File(dirPath);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            if (isShareToo) {
                shareImage(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getScreenShot(View view) {
        View screenView = view;
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public List<? extends EntityWithName> getAutoSuggestItems() {
        return new ArrayList<>();
    }
}
