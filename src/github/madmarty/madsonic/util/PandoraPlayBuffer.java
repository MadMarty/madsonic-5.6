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
import github.madmarty.madsonic.activity.DownloadActivity;
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.service.DownloadFile;
import github.madmarty.madsonic.service.DownloadService;
import github.madmarty.madsonic.service.DownloadServiceImpl;
import github.madmarty.madsonic.service.MusicService;
import github.madmarty.madsonic.service.MusicServiceFactory;

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class PandoraPlayBuffer {

    private static final Logger LOG = new Logger(PandoraPlayBuffer.class);
    private static final int CAPACITY = 20;
    private static final int REFILL_THRESHOLD = 11;

    private final ScheduledExecutorService executorService;
    private final List<MusicDirectory.Entry> buffer = new ArrayList<MusicDirectory.Entry>();
    private Context context;
    private int currentServer;
    
        
    public PandoraPlayBuffer(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            	
            	DownloadService downloadService = DownloadServiceImpl.getInstance();
            	if (downloadService.isPandoraPlayEnabled()) {
                    refill();
            	}
            }
        };
    executorService.scheduleWithFixedDelay(runnable, 1, 30, TimeUnit.SECONDS);
    }

    public List<MusicDirectory.Entry> get(int size) {
        clearBufferIfnecessary();

        List<MusicDirectory.Entry> result = new ArrayList<MusicDirectory.Entry>(size);
        synchronized (buffer) {
            while (!buffer.isEmpty() && result.size() < size) {
                result.add(buffer.remove(buffer.size() - 1));
            }
        }
        LOG.info("Taking " + result.size() + " songs from pandora play buffer. " + buffer.size() + " remaining.");
        return result;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public int size() {
        synchronized (buffer) {
        	return buffer.size();
        }
    }
    
    public void clear() {
        synchronized (buffer) {
        	buffer.clear();
        }
    }
    
    public void refill(int id) throws Throwable {

        // Check if active server has changed.
        clearBufferIfnecessary();

        if (buffer.size() > REFILL_THRESHOLD || (!Util.isNetworkConnected(context) && !Util.isOffline(context))) {
            return;
        }
    	
        MusicService service = MusicServiceFactory.getMusicService(context);
        MusicDirectory songs = service.getPandoraSongs(id, context, null);

		DownloadService downloadService = DownloadServiceImpl.getInstance();
		CurrentPlayBuffer currentPlayBuffer = downloadService.getCurrentPlayBuffer();
  	  
		List<MusicDirectory.Entry> currentPlayed = currentPlayBuffer.getall();
		  
        synchronized (buffer) {
        	
        	int resultList = CAPACITY - buffer.size();
        	int count = 0;
        	
        	for (MusicDirectory.Entry newEntry : songs.getChildren() ) {
         	   if (buffer.size() < resultList) {
         		   
         			if (currentPlayed.contains(newEntry)) {
         			   LOG.info("KILLED duplicate!");
         			} else {
             		   buffer.add(newEntry);
            		   count++;
         			}
               }
        	}
            LOG.info("Refilled pandora play buffer with " + count + " songs. Based on id: " + id + " Buffersize: " + buffer.size());
        }        
        
    }
    
    
    public void refill() {

        // Check if active server has changed.
        clearBufferIfnecessary();

        if (buffer.size() > REFILL_THRESHOLD || (!Util.isNetworkConnected(context) && !Util.isOffline(context))) {
            return;
        }

        try {
            MusicService service = MusicServiceFactory.getMusicService(context);

    		DownloadService downloadService = DownloadServiceImpl.getInstance();
    		CurrentPlayBuffer currentPlayBuffer = downloadService.getCurrentPlayBuffer();
      	  
    		List<MusicDirectory.Entry> recentPlayed = currentPlayBuffer.getall();
    		MusicDirectory.Entry lastPlayed = new MusicDirectory.Entry();
    		MusicDirectory songs = new MusicDirectory();
    		
    		if (recentPlayed.size()>0) {
    			lastPlayed = recentPlayed.get(recentPlayed.size()-1);
                songs = service.getPandoraSongs(Integer.valueOf(lastPlayed.getId()), context, null);
                
                if (songs.getChildren().size() > 0) {
                    LOG.info("Refilled buffer Candidates -> LASTPLAYED -> " + lastPlayed.getArtist() + " - " + lastPlayed.getTitle() + " -> " + songs.getChildren().size() );
                } else {
                    LOG.info("no files found");
                    return;
                }
            }


            synchronized (buffer) {
            	
            	int resultList = CAPACITY; // - buffer.size();
            	int count = 0;
            	
            	for (MusicDirectory.Entry newEntry : songs.getChildren() ) {
             	   if (buffer.size() < resultList) {
             		   
         			if (recentPlayed.contains(newEntry)) {
    			 		 LOG.info("KILLED duplicate!");
    			} else {
        		   buffer.add(newEntry);
        		   count++;
    			}
                  }            		

            	}
            	
            	if (count > 0) {
            		LOG.debug("added to pandora buffer " + count + " songs. Buffersize now: " + buffer.size());
            	}
            }
        } catch (Exception x) {
            LOG.warn("Failed to refill pandora play buffer.", x);
        }
    }

    private void clearBufferIfnecessary() {
        synchronized (buffer) {
            if (currentServer != Util.getActiveServer(context)) {
                currentServer = Util.getActiveServer(context);
                buffer.clear();
            }
        }
    }

}
