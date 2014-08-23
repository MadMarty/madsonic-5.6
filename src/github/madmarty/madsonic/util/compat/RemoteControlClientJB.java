package github.madmarty.madsonic.util.compat;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import github.madmarty.madsonic.activity.SubsonicTabActivity;
import github.madmarty.madsonic.service.DownloadService;

@TargetApi(18)
public class RemoteControlClientJB extends RemoteControlClientICS {
	@Override
	public void register(final Context context, final ComponentName mediaButtonReceiverComponent) {
		super.register(context, mediaButtonReceiverComponent);
		
		mRemoteControl.setOnGetPlaybackPositionListener(new RemoteControlClient.OnGetPlaybackPositionListener() {
			@Override
			public long onGetPlaybackPosition() {
				return downloadService.getPlayerPosition();
			}
		});
		mRemoteControl.setPlaybackPositionUpdateListener(new RemoteControlClient.OnPlaybackPositionUpdateListener() {
			@Override
			public void onPlaybackPositionUpdate(long newPosition) {
				downloadService.seekTo((int) newPosition);
				setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
		});
	}
	
	@Override
	public void setPlaybackState(final int state) {
		long position = -1;
		if(state == RemoteControlClient.PLAYSTATE_PLAYING || state == RemoteControlClient.PLAYSTATE_PAUSED) {
			position = downloadService.getPlayerPosition();
		}
		mRemoteControl.setPlaybackState(state, position, 1.0f);
	}
	
	@Override
	protected int getTransportFlags() {
		return super.getTransportFlags() | RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE;
	}

}
