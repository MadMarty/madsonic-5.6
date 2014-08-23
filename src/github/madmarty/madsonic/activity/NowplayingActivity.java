package github.madmarty.madsonic.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import github.madmarty.madsonic.R; 
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.service.DownloadFile;
import github.madmarty.madsonic.service.MusicService;
import github.madmarty.madsonic.service.MusicServiceFactory;
import github.madmarty.madsonic.util.BackgroundTask;
import github.madmarty.madsonic.util.Constants;
import github.madmarty.madsonic.util.EntryAdapter;
import github.madmarty.madsonic.util.TabActivityBackgroundTask;
import github.madmarty.madsonic.util.Util;

/**
 * @author Madevil
 */ 
public final class NowplayingActivity extends SubsonicTabActivity {

	private Button selectButton;
	private Button playNowButton;
	private Button playLastButton;
	private Button deleteButton;

	private View emptyView;

	private EntryAdapter entryAdapter;
	private DragSortListView entryList;
	private List<MusicDirectory.Entry> entries;

	private DragSortController dragSortController;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle); 
		setContentView(R.layout.nowplaying);

		String theme = Util.getTheme(getBaseContext());

		if ("Madsonic Flawless".equals(theme) || "Madsonic Flawless Fullscreen".equals(theme)) {
			mainBar = findViewById(R.id.button_bar);
			mainBar.setBackgroundResource(R.drawable.menubar_button_normal_green);
		} 
		if ("Madsonic Pink".equals(theme) || "Madsonic Pink Fullscreen".equals(theme)) {
			mainBar = findViewById(R.id.button_bar);
			mainBar.setBackgroundResource(R.drawable.menubar_button_normal_pink);
		} 
		if ("Madsonic Black".equals(theme) || "Madsonic Black Fullscreen".equals(theme) ) {
			mainBar = findViewById(R.id.button_bar);
			mainBar.setBackgroundResource(R.drawable.menubar_button_normal_black);
		}         
		if ("Madsonic Light".equals(theme) || "Madsonic Light Fullscreen".equals(theme) || "Madsonic Emerald".equals(theme) || "Madsonic Emerald Fullscreen".equals(theme)) {
			mainBar = findViewById(R.id.button_bar);
			mainBar.setBackgroundResource(R.drawable.menubar_button_light);
		} 

	 	selectButton = (Button) findViewById(R.id.select_album_select);
		playNowButton = (Button) findViewById(R.id.select_album_play_now);
		playLastButton = (Button) findViewById(R.id.select_album_play_last);
		deleteButton = (Button) findViewById(R.id.select_album_delete);

		emptyView = findViewById(R.id.select_album_empty);

		entryList = (DragSortListView) findViewById(R.id.select_song_entries);

		dragSortController = new DragSortController(entryList);
		dragSortController.setSortEnabled(true);
		dragSortController.setDragInitMode(0);
		dragSortController.setDragHandleId(R.id.drag_handle);
		dragSortController.setRemoveEnabled(false);

		entryList.setDragEnabled(false);
		entryList.setFloatViewManager(dragSortController);
		entryList.setOnTouchListener(dragSortController);
		entryList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// Button: refresh
		ImageButton refreshButton = (ImageButton) findViewById(R.id.action_button_3);
		refreshButton.setImageResource(R.drawable.action_refresh);
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				refresh();
			}
		});  

		// Button 2: search
		ImageButton actionSearchButton = (ImageButton) findViewById(R.id.action_button_2);
		SharedPreferences prefs = Util.getPreferences(this);
		if(prefs.getBoolean(Constants.PREFERENCES_KEY_SEARCH_ENABLED, true)) {
			actionSearchButton.setVisibility(View.GONE);
		} else {
			actionSearchButton.setVisibility(View.VISIBLE);
		}
		actionSearchButton.setImageResource(R.drawable.action_search);
		actionSearchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSearchRequested();
			}
		});  

		// Button 3: Settings
		ImageButton actionSettingsButton = (ImageButton)findViewById(R.id.action_button_1);
		actionSettingsButton.setVisibility(View.GONE);



		selectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectAllOrNone();
			}
		});
		playNowButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences myPreference=PreferenceManager.getDefaultSharedPreferences(NowplayingActivity.this);
				boolean jump2player = myPreference.getBoolean("jump2Player", false);
				download(false, false, true, false, false, jump2player);
				selectAll(false, false);
			}
		});

		playLastButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SharedPreferences myPreference=PreferenceManager.getDefaultSharedPreferences(NowplayingActivity.this);
				boolean jump2player = myPreference.getBoolean("jump2Player", false);
				download(true, false, false, false, false, jump2player);
				selectAll(false, false);
			}
		});

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				delete();
				selectAll(false, false);
			}
		});        

		entryList.setOnItemClickListener(new DragSortListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0) {
					MusicDirectory.Entry entry = (MusicDirectory.Entry) parent.getItemAtPosition(position);
					if (entry == null) {
						return;
					}
					if (entry.isDirectory()) {
						Intent intent = new Intent(NowplayingActivity.this, SelectAlbumActivity.class);
						intent.putExtra(Constants.INTENT_EXTRA_NAME_ID, entry.getId());
						intent.putExtra(Constants.INTENT_EXTRA_NAME_NAME, entry.getTitle());
						Util.startActivityWithoutTransition(NowplayingActivity.this, intent);
					} else if (entry.isVideo()) {
						
						if (entry.getData() != null && entry.getData().length() > 10) {
							playYouTubeVideo(entry);
						} else {
							playVideo(entry);
						}
						
					} else {
						enableButtons();
					}					
				}
			}
		});

		entryList.setDropListener(new DragSortListView.DropListener() {
			@Override
			public void drop(int from, int to) {
				int max = entries.size();
				if(to >= max) {
					to = max - 1;
				}
				else if(to < 0) {
					to = 0;
				}
				entries.add(to, entries.remove(from));
				entryAdapter.notifyDataSetChanged();
			}
		});

		load();

		enableButtons();
	}

	private void enableButtons() {
		if (getDownloadService() == null) {
			return;
		}

		List<MusicDirectory.Entry> selection = getSelectedSongs();
		boolean enabled = !selection.isEmpty();
		boolean deleteEnabled = false;

		for (MusicDirectory.Entry song : selection) {
			DownloadFile downloadFile = getDownloadService().forSong(song);
			if (downloadFile.isCompleteFileAvailable()) {
				deleteEnabled = true;
			}
		}

		selectButton.setVisibility(View.VISIBLE);
		playNowButton.setVisibility(View.VISIBLE);
		playLastButton.setVisibility(View.VISIBLE);
		deleteButton.setVisibility(View.VISIBLE);

		playNowButton.setEnabled(enabled);
		playLastButton.setEnabled(enabled);
		deleteButton.setEnabled(deleteEnabled);
	}

	private void refresh() {
		finish();
		Intent intent = getIntent();
		intent.putExtra(Constants.INTENT_EXTRA_NAME_REFRESH, true);
		Util.startActivityWithoutTransition(this, intent);
	}

	private List<MusicDirectory.Entry> getSelectedSongs() {
		List<MusicDirectory.Entry> songs = new ArrayList<MusicDirectory.Entry>(10);
		int count = entryList.getCount();
		for (int i = 0; i < count; i++) {
			if (entryList.isItemChecked(i)) {
				MusicDirectory.Entry entry = getEntryAtPosition(i);
				if (entry != null) {
					songs.add(entry);
				}
			}
		}
		return songs;
	}

	private void selectAllOrNone() {
		boolean someUnselected = false;
		int count = entryList.getCount();
		for (int i = 0; i < count; i++) {
			MusicDirectory.Entry entry = getEntryAtPosition(i);
			if (!entryList.isItemChecked(i) && entry != null) {
				someUnselected = true;
				break;
			}
		}
		selectAll(someUnselected, true);
	}

	private void selectAll(boolean selected, boolean toast) {
		int count = entryList.getCount();
		int selectedCount = 0;
		for (int i = 0; i < count; i++) {
			MusicDirectory.Entry entry = getEntryAtPosition(i);
			if (entry != null && !entry.isDirectory() && !entry.isVideo()) {
				entryList.setItemChecked(i, selected);
				selectedCount++;
			}
		}

		// Display toast: N tracks selected / N tracks unselected
		if (toast) {
			int toastResId = selected ? R.string.select_album_n_selected
					: R.string.select_album_n_unselected;
			Util.toast(this, getString(toastResId, selectedCount));
		}

		enableButtons();
	}

	private void delete() {
		if (getDownloadService() != null) {
			getDownloadService().delete(getSelectedSongs());
		}
	}

	private MusicDirectory.Entry getEntryAtPosition(int i) {
		Object item = entryList.getItemAtPosition(i);
		return item instanceof MusicDirectory.Entry ? (MusicDirectory.Entry) item : null;
	}

	private void download(final boolean append, final boolean save, final boolean autoplay, final boolean playNext, final boolean shuffle) {
		SharedPreferences myPreference=PreferenceManager.getDefaultSharedPreferences(NowplayingActivity.this);
		boolean jump2player = myPreference.getBoolean("jump2Player", false);
		download(append, save, autoplay, playNext, shuffle, jump2player);
	}

	private void download(final boolean append, final boolean save, final boolean autoplay, final boolean playNext, final boolean shuffle, final boolean jump2Player) {
		if (getDownloadService() == null) {
			return;
		}

		List<MusicDirectory.Entry> songs = getSelectedSongs();
		if (!append) {
			getDownloadService().clear();
		}

		warnIfNetworkOrStorageUnavailable();
		getDownloadService().download(songs, save, autoplay, playNext, shuffle);
		String playlistName = getIntent().getStringExtra(Constants.INTENT_EXTRA_NAME_PLAYLIST_NAME);
		if (playlistName != null) {
			getDownloadService().setSuggestedPlaylistName(playlistName,null);
		}
		if (autoplay) {
			if (jump2Player) {
				Util.startActivityWithoutTransition(NowplayingActivity.this, DownloadActivity.class);
			}

		} else if (save) {
			Util.toast(NowplayingActivity.this,
					getResources().getQuantityString(R.plurals.select_album_n_songs_downloading, songs.size(), songs.size()));
		} else if (append) {
			Util.toast(NowplayingActivity.this,
					getResources().getQuantityString(R.plurals.select_album_n_songs_added, songs.size(), songs.size()));
		}
	}

	@Override
	protected void onPostCreate(Bundle bundle) {
		super.onPostCreate(bundle);
	}

	private synchronized void load() {
		BackgroundTask<MusicDirectory> task = new TabActivityBackgroundTask<MusicDirectory>(this, true) {

			@Override
			protected MusicDirectory doInBackground() throws Throwable {
				MusicService musicService = MusicServiceFactory.getMusicService(NowplayingActivity.this);
				return musicService.getNowplayingSongs(NowplayingActivity.this, this);
			}

			@Override
			protected void done(MusicDirectory result) {

				entries = result.getChildren();
				entryList.setAdapter(entryAdapter = new EntryAdapter(NowplayingActivity.this, getImageLoader(), entries, true, true));

				if (result == null || result.getChildren().isEmpty()) {
					emptyView.setVisibility(View.VISIBLE);
					selectButton.setVisibility(View.GONE);
					playNowButton.setVisibility(View.GONE);
					playLastButton.setVisibility(View.GONE);
					deleteButton.setVisibility(View.GONE);
				} else {
					emptyView.setVisibility(View.GONE);
				}
			}
		};

		task.execute();
	}

}