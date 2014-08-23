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
package github.madmarty.madsonic.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.service.MusicService;
import github.madmarty.madsonic.service.MusicServiceFactory;

/**
 * @author Madevil
 * @version $Id$
 */
public class CurrentPlayBuffer {

	private static final Logger LOG = new Logger(CurrentPlayBuffer.class);

	private static final int CAPACITY = 100;
	private static final int CLEAN_THRESHOLD = 51;

	private final List<MusicDirectory.Entry> buffer = new ArrayList<MusicDirectory.Entry>();

	private final ScheduledExecutorService executorService;
	private Context context;


	public CurrentPlayBuffer(Context context) {
		this.context = context;
		executorService = Executors.newSingleThreadScheduledExecutor();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				truncate();
			}
		};
		executorService.scheduleWithFixedDelay(runnable, 1, 60, TimeUnit.SECONDS);
	}

	public List<MusicDirectory.Entry> getall(){
		synchronized (buffer) { 
			return buffer; 
			}
	}


	private void truncate(){
		synchronized (buffer) {
			if (buffer.size() >= CLEAN_THRESHOLD) {
				buffer.remove(0);
				LOG.warn("truncate currentPlayBuffer: " + buffer.size());
			}
		}
	}


	public void add(MusicDirectory.Entry entry){

		synchronized (buffer) {
				if (!buffer.contains(entry)) {
					buffer.add(entry);
						LOG.debug("Add ID " + entry.getId() + " to buffer. Buffersize: " + buffer.size());
				}
			}
	}
}
