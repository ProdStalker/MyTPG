package com.mytpg.program;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.multidex.MultiDex;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mytpg.engines.settings.AppSettings;
import com.mytpg.engines.settings.DataSettings;
import com.mytpg.program.core.App;
import com.mytpg.program.dialogs.DisruptionsDialogFragment;
import com.mytpg.program.dialogs.LinesDialogFragment;
import com.mytpg.program.fragments.BustedFragment;
import com.mytpg.program.fragments.DayDeparturesFragment;
import com.mytpg.program.fragments.DepartureAlarmsFragment;
import com.mytpg.program.fragments.DirectionsFragment;
import com.mytpg.program.fragments.DisruptionsFragment;
import com.mytpg.program.fragments.FavoriteStopsFragment;
import com.mytpg.program.fragments.LinesFragment;
import com.mytpg.program.fragments.MapsFragment;
import com.mytpg.program.fragments.NextDeparturesFragment;
import com.mytpg.program.fragments.OfflineDayDeparturesFragment;
import com.mytpg.program.fragments.ProximityFragment;
import com.mytpg.program.fragments.SettingsFragment;
import com.mytpg.program.fragments.ShowDirectionsFragment;
import com.mytpg.program.fragments.StopsFragment;
import com.mytpg.program.fragments.ThermometerFragment;
import com.mytpg.program.fragments.TicketsFragment;
import com.mytpg.program.fragments.core.BaseFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final static String ARG_NAV_CURRENT_NAME = "currentNav";
    private final static String ARG_CURRENT_SEARCH = "currentSearch";
    public final static String ARG_FRAGMENT_WANTED = "fragmentWanted";

    public void openSettings() {
        mNavigationView.setCheckedItem(R.id.nav_settings);
        changeFragment(new SettingsFragment(), true);
    }

    public enum DialogFragmentName {Disruptions, Lines, Stops}

    private FloatingActionButton m_fab = null;
    private Toolbar mToolbar = null;
    private Menu mMenu = null;
    private NavigationView mNavigationView = null;

    private int mCurrentNav = R.id.nav_stops;
    private Fragment mCurrentFragment = null;
    private String mCurrentSearch = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public App getApp() {
        return ((App) getApplicationContext());
    }

    public void changeFragment(Fragment argFragment) {
        changeFragment(argFragment, false);
    }

    /*private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, 1234);
    }


    /**
     * Handle the results from the voice recognition activity.
     */
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            List<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            Log.d("MATCHES", matches.toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    } */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getCurrentFragment() != null) {
            getCurrentFragment().onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }
        mCurrentSearch = savedInstanceState.getString(ARG_CURRENT_SEARCH);
        updateCurrentNav(savedInstanceState.getInt(ARG_NAV_CURRENT_NAME));
        if (getSupportFragmentManager().findFragmentById(R.id.content_fragment) == null)
        {
            navigationItemChoose(mCurrentNav);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_NAV_CURRENT_NAME, mCurrentNav);
        if (mCurrentSearch != null && !mCurrentSearch.isEmpty()) {
            outState.putString(ARG_CURRENT_SEARCH, mCurrentSearch);
        }
    }

    public void openMapDirection(Location argFromLoc, Location argToLoc) {
        String url = String.format("http://maps.google.com/maps?saddr=%1$.5f,%2$.5f&daddr=%3$.5f,%4$.5f", argFromLoc.getLatitude(),
                                                                                                          argFromLoc.getLongitude(),
                                                                                                          argToLoc.getLatitude(),
                                                                                                          argToLoc.getLongitude());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
            Uri.parse(url));

        try {
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.action_view)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
        }

    }



    public void openSmsDefault(String ArgNumber, String ArgText)
    {
        Uri uri = Uri.parse("smsto:" + ArgNumber);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("sms_body", ArgText);
        try {
            startActivity(it);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
        }
    }

    public void openUrl(String argUrl)
    {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(argUrl.trim()));
        try {
            startActivity(Intent.createChooser(i, getResources().getText(R.string.action_view)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
        }
    }

    public void shareLink(String argTitle, String argUrl)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        try {

            i.putExtra(Intent.EXTRA_SUBJECT, argTitle);
            i.putExtra(Intent.EXTRA_TEXT, Uri.parse(argUrl).toString());
            i.setType("text/plain");

            try {
                startActivity(Intent.createChooser(i, getResources().getText(R.string.action_share)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Log.d("ERREUR", "Steve erreur");
            e.printStackTrace();
        }
    }

    public void shareText(String argTitle, String argText)
    {
        argText = "Créé par My TPG sur Android\n" + argText;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, argTitle);
        i.putExtra(Intent.EXTRA_TEXT, argText);
        i.setType("text/plain");
        try {
            startActivity(Intent.createChooser(i, getResources().getText(R.string.action_share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_app_available), Toast.LENGTH_SHORT).show();
        }
    }

    public void changeFragment(Fragment argFragment, boolean argInBackStack)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment frag = fragmentManager.findFragmentByTag(argFragment.getClass().getName());

        if (frag != null && argFragment != null && frag.getClass().equals(argFragment.getClass()) && getRealCurrentFragment().getClass().equals(frag.getClass()))
        {
           return;
        }

        fragmentTransaction.replace(R.id.content_fragment, argFragment, argFragment.getClass().getName());

        if (argInBackStack)
        {
            fragmentTransaction.addToBackStack(argFragment.getClass().getSimpleName());
        }

        if (!argInBackStack)
        {
            for(int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                fragmentManager.popBackStack();
            }
        }

        fragmentTransaction.commit();


        if (argFragment instanceof BaseFragment) {
            BaseFragment baseFragment = (BaseFragment) argFragment;
            baseFragment.setFab(m_fab);
            baseFragment.updateToolbarMenu();
            baseFragment.updateFab();
            updateSuperLayout(baseFragment);
        }
    }

    private Fragment getRealCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment frag = fragmentManager.findFragmentById(R.id.content_fragment);

        return frag;
    }

    public void updateSuperLayout()
    {
        updateSuperLayout(getCurrentFragment());
    }

    private void updateSuperLayout(BaseFragment argFragment) {

        int left = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);
        int top = (int)getResources().getDimension(R.dimen.activity_vertical_margin);
        int right = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);
        int bottom = (int)getResources().getDimension(R.dimen.activity_vertical_margin);

        if (argFragment != null && argFragment.needToBeFullViewport())
        {
            left = top = right = bottom = 0;
        }

        RelativeLayout superRelLay = (RelativeLayout)findViewById(R.id.superRelLay);
        superRelLay.setPadding(left,top,right,bottom);
    }

    public void hideActionsToolbarMenu() {
        changeToolbarMenu(R.id.search,false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeLanguage(false);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        m_fab = (FloatingActionButton) findViewById(R.id.fab);
        m_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentFragment() != null)
                {
                    getCurrentFragment().fabClicked();
                }
            }
        });
        /*m_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCurrentFragment() != null)
                {
                    getCurrentFragment().fabClicked();
                }
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        m_fab.setVisibility(View.GONE);

        Intent intent = getIntent();
        String action = intent.getAction();
       // String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action)) {
            if (handleViewUri(intent)) {
                return;
            }
        }
        else
        {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                final String FragmentNameWanted = bundle.getString(ARG_FRAGMENT_WANTED, "");

                if (!FragmentNameWanted.isEmpty()) {
                    if (FragmentNameWanted.equalsIgnoreCase(getString(R.string.menu_thermometer))) {
                        Bundle newBundle = ThermometerFragment.createBundle(bundle.getInt(ThermometerFragment.ARG_DEPARTURE_CODE));
                        if (newBundle != null) {
                            ThermometerFragment tf = new ThermometerFragment();
                            tf.setArguments(newBundle);
                            changeFragment(tf, false);
                            return;
                        }
                    }
                    else if (FragmentNameWanted.equalsIgnoreCase(getString(R.string.menu_next_departures))){
                        Bundle newBundle = NextDeparturesFragment.createBundle(bundle.getString(NextDeparturesFragment.ARG_MNEMO), bundle.getStringArrayList(NextDeparturesFragment.ARG_FILTER));
                        if (newBundle != null)
                        {
                            NextDeparturesFragment ndf = new NextDeparturesFragment();
                            ndf.setArguments(newBundle);
                            changeFragment(ndf, false);
                            return;
                        }
                    }
                    else if (FragmentNameWanted.equalsIgnoreCase(getString(R.string.menu_tickets))){

                        TicketsFragment tf = new TicketsFragment();
                        changeFragment(tf, false);
                        return;
                    }
                }
            }
        }

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
        if (frag != null)
        {
            changeFragment(frag);
        }
        else {
            Log.d("CURRENT NAV", String.valueOf(mCurrentNav));

            mNavigationView.setCheckedItem(mCurrentNav);
            navigationItemChoose(mCurrentNav);
        }

    }

    public void changeLanguage(boolean argWithRecreate) {
        App app = getApp();
        if (app == null)
        {
            return;
        }

        String currentLanguage = Locale.getDefault().getLanguage();
        boolean found = false;
        String languageToLoad  = app.getSharedPreferences().getString(AppSettings.PREF_LANGUAGE, "");
        if (languageToLoad.isEmpty())
        {
            String[] languages = getResources().getStringArray(R.array.pref_languages_values);
            String systemLanguage = Locale.getDefault().getLanguage();

            for (String language : languages)
            {
                 if (systemLanguage.equalsIgnoreCase(language))
                 {
                     languageToLoad = systemLanguage;
                     found = true;
                     break;
                 }
            }
            if (found) {
                app.getSharedPreferences().edit().putString(AppSettings.PREF_LANGUAGE, systemLanguage);
            }
        }
        if (!languageToLoad.isEmpty() && !currentLanguage.equalsIgnoreCase(languageToLoad)) {
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

            if (argWithRecreate) {
                recreate();
            }
        }
    }

    private boolean handleViewUri(Intent intent) {
        Uri uri = intent.getData();

        if (uri.getPath().contains(DataSettings.URL_MOBILE_TPG_ENDPOINT_DEPARTURE))
        {
            Bundle bundle = NextDeparturesFragment.createBundleFromUrl(uri,this);
            if (bundle == null)
            {
                return false;
            }

            mNavigationView.setCheckedItem(R.id.nav_stops);
            NextDeparturesFragment ndf = new NextDeparturesFragment();
            ndf.setArguments(bundle);
            changeFragment(ndf);
            return true;
        }
        else if (uri.getPath().contains(DataSettings.URL_MOBILE_TPG_ENDPOINT_THERMOMETER))
        {
            Bundle bundle = ThermometerFragment.createBundleFromUrl(uri);
            if (bundle == null)
            {
                return false;
            }

            mNavigationView.setCheckedItem(R.id.nav_stops);
            ThermometerFragment tf = new ThermometerFragment();
            tf.setArguments(bundle);
            changeFragment(tf);
            return true;
        }
        else if (uri.getPath().contains(DataSettings.URL_MOBILE_TPG_ENDPOINT_TIMETABLE))
        {
            Bundle bundle = DayDeparturesFragment.createBundleFromUrl(uri, this);
            if (bundle == null)
            {
                return false;
            }

            DayDeparturesFragment ddf = DayDeparturesFragment.newInstance();
            ddf.setArguments(bundle);
            changeFragment(ddf);
            return true;
        }
        else if (uri.getPath().contains(DataSettings.URL_TPG_ENDPOINT_DIRECTIONS))
        {
            Bundle bundle = ShowDirectionsFragment.createBundleFromUrl(uri, this);
            if (bundle == null)
            {
                return false;
            }

            ShowDirectionsFragment sdf = new ShowDirectionsFragment();
            sdf.setArguments(bundle);
            changeFragment(sdf);
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        try {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.core, menu);

        this.mMenu = menu;

        return true;
    }

    public void initializeSearchView()
    {
        MenuItem searchItem = mMenu.findItem(R.id.search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        if (searchView == null)
        {
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchView.clearFocus();
                mCurrentSearch = query;
                if (!getCurrentFragment().canDynamicSearch())
                {
                    getCurrentFragment().search(query);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (getCurrentFragment().canDynamicSearch() || newText.length() == 0) {
                    getCurrentFragment().search(newText);
                }
                mCurrentSearch = newText;
                return false;
            }
        });

        if (mCurrentSearch != null && !mCurrentSearch.isEmpty())
        {
            searchView.setQuery(mCurrentSearch,true);
        }
    }

    public BaseFragment getCurrentFragment()
    {
        Fragment frag = getRealCurrentFragment();
        if (frag != null && frag instanceof  BaseFragment)
        {
            return (BaseFragment)frag;
        }

        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDialogFragment(DialogFragmentName argDialogFragmentName, Bundle argBundle) {
        DialogFragment newFragment = null;

        switch (argDialogFragmentName)
        {
            case Lines:
                newFragment = new LinesDialogFragment();
            break;
            case Disruptions:
                newFragment = new DisruptionsDialogFragment();
            break;
        }
        if (newFragment == null)
        {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }


        //ft.addToBackStack(null);

        // Create and show the dialog.
        newFragment.setArguments(argBundle);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (mToolbar != null)
        {
            mToolbar.setTitle(title);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        return navigationItemChoose(id);
    }
    
    public boolean navigationItemChoose(int argId)
    {
        if (argId== R.id.nav_stops) {
            changeFragment(new StopsFragment());
            mToolbar.setTitle(getString(R.string.menu_stops));
        } else if (argId== R.id.nav_lines) {
            changeFragment(new LinesFragment());
            mToolbar.setTitle(getString(R.string.menu_lines));
        }
        else if (argId== R.id.nav_disruptions)
        {
            changeFragment(new DisruptionsFragment(),true);
            mToolbar.setTitle(getString(R.string.menu_disruptions));
        }
        else if (argId== R.id.nav_offline_day_departures) {
            changeFragment(new OfflineDayDeparturesFragment());
            mToolbar.setTitle(getString(R.string.menu_offline_day_departures));
        } else if (argId== R.id.nav_proximity) {
            changeFragment(new ProximityFragment());
            mToolbar.setTitle(getString(R.string.menu_proximity));

        } else if (argId== R.id.nav_favorites_stops) {
            changeFragment(new FavoriteStopsFragment());
            mToolbar.setTitle(getString(R.string.menu_favorites_stops));
        }
        else if (argId == R.id.nav_departures_alarms)
        {
            changeFragment(new DepartureAlarmsFragment(),true);
            mToolbar.setTitle(getString(R.string.menu_departures_alarms));
        }
        else if (argId == R.id.nav_tickets)
        {
            changeFragment(new TicketsFragment(),true);
            mToolbar.setTitle(getString(R.string.menu_tickets));
        }
        else if (argId == R.id.nav_map)
        {
            MapsFragment mapsFragment = MapsFragment.newInstance();
            //mapsFragment.setArguments(MapsFragment.createBundle("l:3"));
            changeFragment(mapsFragment);


            mToolbar.setTitle(getString(R.string.menu_map));
        }
        else if (argId == R.id.nav_directions)
        {
            changeFragment(new DirectionsFragment());
        }
        else if (argId == R.id.nav_settings)
        {
            openSettings();
        }
        else if (argId == R.id.nav_share)
        {
            share();
        }
        else if (argId == R.id.nav_busted)
        {
            changeFragment(new BustedFragment());
        }
       /* else if (argId == R.id.nav_show_directions)
        {
            changeFragment(new ShowDirectionsFragment());
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        updateCurrentNav(argId);

        return true;
    }

    public void updateCurrentNav(int argCurrentNav)
    {
        mCurrentNav = argCurrentNav;
    }

    private void share() {
        shareLink("", AppSettings.URL_PLAY_STORE);
    }

    public void changeMenu(int argId, boolean argIsVisible)
    {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);

        if (navView == null)
        {
            return;
        }

        Menu menu = navView.getMenu();

        if (menu == null)
        {
            return;
        }

        MenuItem menuItem = menu.findItem(argId);
        if (menuItem == null)
        {
            return;
        }

        menuItem.setVisible(argIsVisible);
    }

    public void changeToolbarMenu(int argId, boolean argIsVisible)
    {
        if (mToolbar == null)
        {
            return;
        }

        View view = mToolbar.findViewById(argId);
        if (view == null)
        {
            return;
        }

        if (argIsVisible)
        {
            view.setVisibility(View.VISIBLE);
        }
        else
        {
            view.setVisibility(View.GONE);
        }
    }

    public void changeToolbarMenuDrawable(int argId, int argResourceId)
    {
        if (mToolbar == null)
        {
            return;
        }

        View view = mToolbar.findViewById(argId);
        if (view == null)
        {
            return;
        }

        ActionMenuItemView item = (ActionMenuItemView)view;
        if (item == null)
        {
            return;
        }

        Drawable icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = getResources().getDrawable(argResourceId, null);
        }
        else
        {
            icon = getResources().getDrawable(argResourceId);
        }

        if (icon != null)
        {
            item.setIcon(icon);
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
}
