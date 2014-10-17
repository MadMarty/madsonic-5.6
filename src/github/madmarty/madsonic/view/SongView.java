/*
 This file is part of Subsonic.

 Subsonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Subsonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2009 (C) Sindre Mehus
 */
package github.madmarty.madsonic.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import github.madmarty.madsonic.R;
import github.madmarty.madsonic.activity.DownloadActivity;
import github.madmarty.madsonic.activity.MainActivity;
import github.madmarty.madsonic.activity.NowplayingActivity;
import github.madmarty.madsonic.activity.SubsonicTabActivity;
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.domain.Version;
import github.madmarty.madsonic.domain.MusicDirectory.Entry;
import github.madmarty.madsonic.domain.PodcastEpisode;
import github.madmarty.madsonic.service.DownloadService;
import github.madmarty.madsonic.service.DownloadServiceImpl;
import github.madmarty.madsonic.service.DownloadFile;
import github.madmarty.madsonic.service.MusicService;
import github.madmarty.madsonic.service.MusicServiceFactory;
import github.madmarty.madsonic.util.ImageLoader;
import github.madmarty.madsonic.util.Logger;
import github.madmarty.madsonic.util.SilentBackgroundTask;
import github.madmarty.madsonic.util.Util;
import github.madmarty.madsonic.util.VideoPlayerType;
import github.madmarty.madsonic.view.UpdateView;

import java.io.File;

/**
 * Used to display songs in a {@code ListView}.
 *
 * @author Sindre Mehus
 */
public class SongView extends UpdateView implements Checkable {

	private static final Logger LOG = new Logger(SongView.class);

	private MusicDirectory.Entry song;

	private ImageView userAvatar;
	
	private CheckedTextView checkedTextView;
	private TextView titleTextView;
	private TextView artistTextView;
	private TextView durationTextView;
	private TextView statusTextView;
	private TextView rankTextView;
	private ImageButton starButton;
	private ImageView moreButton;
	private ImageView dragHandle;

	private long revision = -1;

	private DownloadFile downloadFile;
	private DownloadService downloadService;

	private boolean loaded = false;
	private boolean playing = false;
	private boolean isSaved = false;
	private boolean isPined = false;
	private boolean isStarred = false;
	private boolean isWorkDone = false;
	private boolean partialFileExists = false;

	private int pinnedImage = 0;

	private File partialFile;


	public SongView(Context context) {
		super(context);
		
		if (context instanceof NowplayingActivity) {
			LayoutInflater.from(context).inflate(R.layout.nowplaying_list_item, this, true);
		} else {
			LayoutInflater.from(context).inflate(R.layout.song_list_item, this, true);
		}

		userAvatar = (ImageView) findViewById(R.id.nowplaying_avatar);
		
		checkedTextView = (CheckedTextView) findViewById(R.id.song_check);
		titleTextView = (TextView) findViewById(R.id.song_title);
		artistTextView = (TextView) findViewById(R.id.song_artist);
		durationTextView = (TextView) findViewById(R.id.song_duration);
		statusTextView = (TextView) findViewById(R.id.song_status);
		rankTextView = (TextView) findViewById(R.id.song_rank);
		starButton = (ImageButton) findViewById(R.id.song_star);
		starButton.setVisibility(Util.isOffline(getContext()) ? View.GONE : View.VISIBLE);

		starButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SubsonicTabActivity activity = (SubsonicTabActivity) getContext();
				activity.toggleStarredInBackground(song, starButton);
			}
		});
	}

	public void setObjectImpl(final Object obj1, Object obj2, Object obj3) {
		
		this.song = (MusicDirectory.Entry) obj1;
		boolean checkable = (Boolean) obj2;
		boolean dragable = (Boolean) obj3;
		
		final MusicDirectory.Entry x = (MusicDirectory.Entry) obj1;

		if (context instanceof NowplayingActivity) {
			
			NowplayingActivity activity = (NowplayingActivity) getContext();
			activity.runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
					ImageLoader imageLoader = NowplayingActivity.getStaticImageLoader(context);
					if (imageLoader != null) {
						imageLoader.loadAvatarImage(userAvatar, x.getUsername(), false, 50, false);
					}
			    }
			});
		}
		
		StringBuilder artist = new StringBuilder(40);

		String bitRate = null;
		if (song.getBitRate() != null) {
			bitRate = String.format(getContext().getString(R.string.song_details_kbps), song.getBitRate());
		}

		VideoPlayerType videoPlayer = Util.getVideoPlayerType(getContext());
		String fileFormat;

		if (song.getTranscodedSuffix() == null || song.getTranscodedSuffix().equals(song.getSuffix())
				|| (song.isVideo() && videoPlayer != VideoPlayerType.FLASH)) {
			fileFormat = song.getSuffix();
		} else {
			fileFormat = String.format("%s > %s", song.getSuffix(), song.getTranscodedSuffix());
		}

		if(!song.isVideo()) {
			if(song instanceof PodcastEpisode) {
				String date = ((PodcastEpisode)song).getDate();
				if(date != null) {
					int index = date.indexOf(" ");
					artist.append(date.substring(0, index != -1 ? index : date.length()));
				}
			}
			else if(song.getArtist() != null) {
				if (Util.shouldDisplayBitrateWithArtist(getContext())) {
					artist.append(song.getArtist());
					artist.append(" (").append(String.format(getContext().getString(R.string.song_details_all), bitRate == null ? "" : bitRate + " ", fileFormat))
					.append(")");
				} else { 
					artist.append(song.getArtist());
				}  
			}
		}

		if (Util.shouldDisplayRankWithSong(getContext())) {
			if (song.getRank() > 0) {
				rankTextView.setVisibility(View.VISIBLE);
				if (song.getRank() < 10) {
					rankTextView.setText("0" + String.valueOf(song.getRank()));
				} else {
					rankTextView.setText(String.valueOf(song.getRank()));
				}
			} else {
				rankTextView.setVisibility(View.GONE);
			}
		} else {
			rankTextView.setVisibility(View.GONE);
		}

		starButton.setImageResource(song.isStarred() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
		starButton.setFocusable(false);

		if(song instanceof PodcastEpisode) {
			starButton.setVisibility(View.GONE );
		}

		if (dragable) {
			dragHandle = (ImageView) findViewById(R.id.drag_handle);
			dragHandle.setVisibility(View.VISIBLE );
		}


		moreButton = (ImageView) findViewById(R.id.song_more);
		moreButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				v.showContextMenu();
			}
		});

		String title = song.getTitle();
		Integer track = song.getTrack();
		if(track != null && Util.getDisplayTrack(context)) {
			title = String.format("%02d", track) + " " + title;
		}

		titleTextView.setText(title);
		artistTextView.setText(artist);

		durationTextView.setText(Util.formatDuration(song.getDuration()));

		if (Util.shouldDisplayDurationWithSong(getContext())) {
			durationTextView.setVisibility(View.VISIBLE);
		} else {
			durationTextView.setVisibility(View.GONE);

		}
		checkedTextView.setVisibility(checkable && !song.isVideo() ? View.VISIBLE : View.GONE);

		revision = -1;
		loaded = false;
	}

	@Override
	protected void updateBackground() {
		if (downloadService == null) {
			downloadService = DownloadServiceImpl.getInstance();
			if(downloadService == null) {
				return;
			}
		}

		long newRevision = downloadService.getDownloadListUpdateRevision();
		if(revision != newRevision || downloadFile == null) {
			downloadFile = downloadService.forSong(song);
			revision = newRevision;
		}

		isWorkDone = downloadFile.isWorkDone();
		isSaved = downloadFile.isSaved();
		partialFile = downloadFile.getPartialFile();
		partialFileExists = partialFile.exists();
		isStarred = song.isStarred();

		// Check if needs to load metadata: check against all fields that we know are null in offline mode
		if(song.getBitRate() == null && song.getDuration() == null && song.getDiscNumber() == null && isWorkDone) {
			song.loadMetadata(downloadFile.getCompleteFile());
			loaded = true;
		}
	}

	@Override
	protected void update() {

		if(loaded) {
			setObjectImpl(song, checkedTextView.getVisibility() == View.VISIBLE, false);
		}

		if (downloadService == null) {
			return;
		}

		if(song.isStarred()) {
			if(!starred) {
				starButton.setVisibility(View.VISIBLE);
				starButton.setImageResource(android.R.drawable.btn_star_big_on);
				starred = true;
			}
		} else {
			if(starred) {
				starButton.setVisibility(View.VISIBLE);
				starButton.setImageResource(android.R.drawable.btn_star_big_off);
				starButton.setFocusable(false);
				starred = false;
			}
		}

		if (song instanceof PodcastEpisode) {
			starButton.setVisibility(View.GONE );
		}

		if (Util.shouldDisplayRankWithSong(getContext())) {
			if (song.getRank() > 0) {
				rankTextView.setVisibility(View.VISIBLE);
				if (song.getRank() < 10) {
					rankTextView.setText("0" + String.valueOf(song.getRank()));
				} else {
					rankTextView.setText(String.valueOf(song.getRank()));
				}
			} else {
				rankTextView.setVisibility(View.GONE);
			} 
		} else {
			rankTextView.setVisibility(View.GONE);
		}


		DownloadFile downloadFile = downloadService.forSong(song);
		File partialFile = downloadFile.getPartialFile();

		if (downloadFile.isWorkDone()) {
			int pinnedImage = isSaved ? R.drawable.download_pinned : R.drawable.download_cached;
			if(pinnedImage != this.pinnedImage) {
				moreButton.setImageResource(pinnedImage);
				this.pinnedImage = pinnedImage;
			} else if(this.pinnedImage != R.drawable.download_cached) {
				this.pinnedImage = R.drawable.download_streaming;
			}
		}	
		
		Integer calc = 0;
		if (song != null && song.getSize() != null) {
			calc = (int) (partialFile.length() * 100 / song.getSize());
		}
		
		Integer statusImage = R.drawable.dragid_000;  

		dragHandle = (ImageView) findViewById(R.id.drag_handle);


		if (downloadFile.isDownloading() && !downloadFile.isDownloadCancelled() && partialFileExists) {

			if (calc > 98) { statusImage = R.drawable.dragid_100;} 
			else if (calc > 90 ) {statusImage = R.drawable.dragid_090;} 
			else if (calc > 80 ) {statusImage = R.drawable.dragid_080;} 
			else if (calc > 70 ) {statusImage = R.drawable.dragid_070;} 
			else if (calc > 60 ) {statusImage = R.drawable.dragid_060;} 
			else if (calc > 50 ) {statusImage = R.drawable.dragid_050;} 
			else if (calc > 40 ) {statusImage = R.drawable.dragid_040;} 
			else if (calc > 30 ) {statusImage = R.drawable.dragid_030;} 
			else if (calc > 30 ) {statusImage = R.drawable.dragid_030;} 
			else if (calc > 20 ) {statusImage = R.drawable.dragid_020;} 
			else if (calc > 10 ) {statusImage = R.drawable.dragid_010;} 

			//            statusTextView.setText(Util.formatLocalizedBytes(partialFile.length(), getContext()));
			dragHandle.setImageResource(statusImage);

		} else {

			if (downloadFile.isWorkDone()) {
				dragHandle.setImageResource(R.drawable.dragid_100);
			} else {
				statusTextView.setText(null);
				dragHandle.setImageResource(R.drawable.dragid_000);
			}

		}
		// statusTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


		boolean playing = downloadService.getCurrentPlaying() == downloadFile;
		if (playing) {
			if(!this.playing) {
				this.playing = playing;
				titleTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stat_notify_playing2, 0, 0, 0);
			}
		} else {
			if(this.playing) {
				this.playing = playing;
				titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}

	}

	@Override
	public void setChecked(boolean b) {
		checkedTextView.setChecked(b);
	}

	@Override
	public boolean isChecked() {
		return checkedTextView.isChecked();
	}

	@Override
	public void toggle() {
		checkedTextView.toggle();
	}
}
