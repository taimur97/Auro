package com.architjn.acjmusicplayer.ui.layouts.activity;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.architjn.acjmusicplayer.R;
import com.architjn.acjmusicplayer.service.PlayerService;
import com.architjn.acjmusicplayer.task.ColorChangeAnimation;
import com.architjn.acjmusicplayer.ui.layouts.fragments.AlbumsListFragment;
import com.architjn.acjmusicplayer.ui.layouts.fragments.ArtistListFragment;
import com.architjn.acjmusicplayer.ui.layouts.fragments.PlaylistListFragment;
import com.architjn.acjmusicplayer.ui.layouts.fragments.SearchViewFragment;
import com.architjn.acjmusicplayer.ui.layouts.fragments.SongsListFragment;
import com.architjn.acjmusicplayer.ui.widget.OnSwipeListener;
import com.architjn.acjmusicplayer.ui.widget.SwipeInterface;
import com.architjn.acjmusicplayer.utils.PlayerDBHandler;

/**
 * Created by architjn on 27/11/15.
 */
public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity-TAG";
    private LinearLayout smallPlayer;
    private FragmentName lastExpanded;
    private int lastItem;

    public enum FragmentName {
        Albums, Songs, Artists, Playlists, Search
    }

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private int currentItem = -1;
    private SongsListFragment songFragment;
    private AlbumsListFragment albumFragment;
    private ArtistListFragment artistFragment;
    private PlaylistListFragment playlistFragment;

    public FragmentName currentFragment;


    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlayerActivity.ACTION_RECIEVE_SONG:
                    if (intent.getLongExtra("songId", 0) != -1) {
                        updatePlayer(intent.getStringExtra("songName"), intent.getLongExtra("albumId", 0));
                        if (smallPlayer.getVisibility() != View.VISIBLE)
                            smallPlayer.setVisibility(View.VISIBLE);
                    } else {
                        if (smallPlayer.getVisibility() != View.GONE)
                            smallPlayer.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        initDrawer();
        setPlayer();
        songFragment = new SongsListFragment();
        fragmentSwitcher(songFragment, 0, FragmentName.Songs, true);
        sendBroadcast(new Intent(PlayerService.ACTION_GET_SONG));
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerActivity.ACTION_RECIEVE_SONG);
        registerReceiver(br, filter);
    }

    private void setPlayer() {
        smallPlayer = (LinearLayout) findViewById(R.id.small_panel);
        smallPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPlayer();
            }
        });
        findViewById(R.id.small_panel).setOnTouchListener(
                new OnSwipeListener(
                        new SwipeInterface() {
                            @Override
                            public void bottom2top(View v) {
                                launchPlayer();
                            }

                            @Override
                            public void left2right(View v) {
                                sendBroadcast(new Intent(PlayerService.ACTION_PREV_SONG));
                            }

                            @Override
                            public void right2left(View v) {

                                sendBroadcast(new Intent(PlayerService.ACTION_NEXT_SONG));
                            }

                            @Override
                            public void top2bottom(View v) {
                                launchPlayer();
                            }
                        }));
    }

    public void updatePlayer(String name, long albumId) {
        ((TextView) findViewById(R.id.mini_player_song_name)).setText(name);
        new ColorChangeAnimation(this, smallPlayer, new PlayerDBHandler(this).setAlbumArt(albumId)) {
            @Override
            public void onColorFetched(Integer colorPrimary) {
            }
        }.execute();
    }

    private void launchPlayer() {
        startActivity(new Intent(MainActivity.this, PlayerActivity.class));
    }

    private void initDrawer() {
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        String[] leftSliderData = {getResources().getString(R.string.songs),
                getResources().getString(R.string.albums),
                getResources().getString(R.string.artists),
                getResources().getString(R.string.playlist)};
        navigationDrawerAdapter = new ArrayAdapter<String>(
                MainActivity.this, R.layout.drawer_list_item, leftSliderData);
        drawerList.setAdapter(navigationDrawerAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        if (songFragment == null)
                            songFragment = new SongsListFragment();
                        fragmentSwitcher(songFragment, i,
                                FragmentName.Songs, true);
                        break;
                    case 1:
                        if (albumFragment == null)
                            albumFragment = new AlbumsListFragment();
                        fragmentSwitcher(albumFragment, i,
                                FragmentName.Albums, true);
                        break;
                    case 2:
                        if (artistFragment == null)
                            artistFragment = new ArtistListFragment();
                        fragmentSwitcher(artistFragment, i,
                                FragmentName.Artists, true);
                        break;
                    case 3:
                        if (playlistFragment == null)
                            playlistFragment = new PlaylistListFragment();
                        fragmentSwitcher(playlistFragment, i,
                                FragmentName.Playlists, true);
                        break;
                }
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, 0, 0) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fragmentSwitcher(Fragment fragment, int itemId,
                                  FragmentName fname, boolean slideAnimate) {
        currentFragment = fname;
        if (currentItem == itemId) {
            // Don't allow re-selection of the currently active item
            return;
        }
        currentItem = itemId;
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(String.valueOf(fname));

        if (slideAnimate)
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, 0)
                    .replace(R.id.main_fragment_holder, fragment)
                    .commit();
        else
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, 0)
                    .replace(R.id.main_fragment_holder, fragment)
                    .commit();

        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }
            }, 200);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        setSearchView(menu);
        return true;
    }

    private void setSearchView(Menu menu) {
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        final SearchViewFragment searchViewFragment = new SearchViewFragment();
        searchViewFragment.setSearchView(searchView);
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                lastExpanded = currentFragment;
                lastItem = currentItem;
                fragmentSwitcher(searchViewFragment, -1, FragmentName.Search, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                fragmentSwitcher(getFragmentFromName(lastExpanded), lastItem, lastExpanded, false);
                return true;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    private void changeToolbarColorLight(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.BLACK);
                return;
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeToolbarColorLight(viewGroup.getChildAt(i));
                }
            }
        }
    }

    public void killActivity() {
        super.onBackPressed();
    }

    public Fragment getFragmentFromName(FragmentName name) {
        switch (name) {
            case Songs:
                return songFragment;
            case Albums:
                return albumFragment;
            case Artists:
                return artistFragment;
            case Playlists:
                return playlistFragment;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        switch (currentFragment) {
            case Songs:
                songFragment.onBackPress();
                break;
            case Albums:
                albumFragment.onBackPress();
                break;
            case Artists:
                artistFragment.onBackPress();
                break;
            case Playlists:
                playlistFragment.onBackPress();
                break;
        }
    }
}
