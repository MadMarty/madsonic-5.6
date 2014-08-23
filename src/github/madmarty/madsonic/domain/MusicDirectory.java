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
package github.madmarty.madsonic.domain;

import android.media.MediaMetadataRetriever;
import github.madmarty.madsonic.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Sindre Mehus
 */
public class MusicDirectory implements Serializable {

	private static final Logger LOG = new Logger(MusicDirectory.class);

	private String id;
	private String name;

	private String parentId;

	private List<Entry> children;
	private boolean starred;
	private int count;

	public MusicDirectory() {
		children = new ArrayList<Entry>();
	}

	public MusicDirectory(List<Entry> children) {
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isStarred() {
		return starred;
	}

	public void setStarred(boolean starred) {
		this.starred = starred;
	}

	public void addChild(Entry child) {
		children.add(child);
	}
	public void addChildren(List<Entry> children) {
		this.children.addAll(children);
	}

	public void replaceChildren(List<Entry> children) {
		this.children = children;
	}

	public List<Entry> getChildren() {
		return getChildren(true, true);
	}

	public List<Entry> getChildren(boolean includeDirs, boolean includeFiles) {
		if (includeDirs && includeFiles) {
			return children;
		}

		List<Entry> result = new ArrayList<Entry>(children.size());
		for (Entry child : children) {
			if (child.isDirectory() && includeDirs || !child.isDirectory() && includeFiles) {
				result.add(child);
			}
		}
		return result;
	}

	public int getChildrenSize() {
		return children.size();
	}

	public void sortChildren() {
		EntryComparator.sort(children);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	


    public static class Entry implements Serializable {
		private String id;
		private String parent;
		private boolean directory;
		private boolean isArtist;
		private String title;
		private String album;
		private String artist;
		private Integer track;
		private Integer year;
		private String genre;
		private String mood;
		private String contentType;
		private String suffix;
		private String transcodedContentType;
		private String transcodedSuffix;
		private String coverArt;
		private Long size;
		private Integer duration;
		private Integer bitRate;
		private String path;
		private boolean video;
		private String data;
		private Integer discNumber;
		private boolean starred;
		private int rank;
		private String username;

		public void loadMetadata(File file) {
			try {
				MediaMetadataRetriever metadata = new MediaMetadataRetriever();
				metadata.setDataSource(file.getAbsolutePath());
				String discNumber = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER);
				if(discNumber == null) {
					discNumber = "1/1";
				}
				int slashIndex = discNumber.indexOf("/");
				if(slashIndex > 0) {
					discNumber = discNumber.substring(0, slashIndex);
				}
				try {
					setDiscNumber(Integer.parseInt(discNumber));
				} catch(Exception e) {
					LOG.warn("Non numbers in disc field!");
				}
				String bitrate = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
				setBitRate(Integer.parseInt((bitrate != null) ? bitrate : "0") / 1000);
				String length = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				setDuration(Integer.parseInt(length) / 1000);
				String artist = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
				if(artist != null) {
					setArtist(artist);
				}
				String album = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
				if(album != null) {
					setAlbum(album);
				}
				metadata.release();
			} catch(Exception e) {
				LOG.warn("Device doesn't properly support MediaMetadataRetreiver", e);
			}
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getParent() {
			return parent;
		}

		public void setParent(String parent) {
			this.parent = parent;
		}

		public boolean isDirectory() {
			return directory;
		}

		public boolean isArtist() {
			return isArtist;
		}        

		public void setDirectory(boolean directory) {
			this.directory = directory;
		}

		public void setIsArtist(boolean isartist) {
			this.isArtist = isartist;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public String getArtist() {
			return artist;
		}

		public void setArtist(String artist) {
			this.artist = artist;
		}

		public Integer getTrack() {
			return track == null ? 0 : track;
		}

		public void setTrack(Integer track) {
			this.track = track;
		}

		public Integer getYear() {
			return year == null ? 0 : year;
		}

		public void setYear(Integer year) {
			this.year = year;
		}

		public String getGenre() {
			return genre;
		}

		public void setGenre(String genre) {
			this.genre = genre;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public String getTranscodedContentType() {
			return transcodedContentType;
		}

		public void setTranscodedContentType(String transcodedContentType) {
			this.transcodedContentType = transcodedContentType;
		}

		public String getTranscodedSuffix() {
			return transcodedSuffix;
		}

		public void setTranscodedSuffix(String transcodedSuffix) {
			this.transcodedSuffix = transcodedSuffix;
		}

		public Long getSize() {
			return size;
		}

		public void setSize(Long size) {
			this.size = size;
		}

		public Integer getDuration() {
			return duration;
		}

		public void setDuration(Integer duration) {
			this.duration = duration;
		}

		public Integer getBitRate() {
			return bitRate;
		}

		public void setBitRate(Integer bitRate) {
			this.bitRate = bitRate;
		}

		public String getCoverArt() {
			return coverArt;
		}

		public void setCoverArt(String coverArt) {
			this.coverArt = coverArt;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isVideo() {
			return video;
		}

		public void setVideo(boolean video) {
			this.video = video;
		}

		public Integer getDiscNumber() {
			return discNumber;
		}

		public void setDiscNumber(Integer discNumber) {
			this.discNumber = discNumber;
		}

		public boolean isStarred() {
			return starred;
		}

		public void setStarred(boolean starred) {
			this.starred = starred;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Entry entry = (Entry) o;
			return id.equals(entry.id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return title;
		}

		public String getMood() {
			return mood;
		}

		public void setMood(String mood) {
			this.mood = mood;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}
}

	public static class EntryComparator implements Comparator<Entry> {
			public int compare(Entry lhs, Entry rhs) {
				if(lhs.isDirectory() && !rhs.isDirectory()) {
					return -1;
				} else if(!lhs.isDirectory() && rhs.isDirectory()) {
					return 1;
				} else if(lhs.isDirectory() && rhs.isDirectory()) {
					Integer lhsYear = lhs.getYear();
					Integer rhsYear = rhs.getYear();
					if(lhsYear != null && rhsYear != null) {
						return lhsYear.compareTo(rhsYear);
					} else if(lhsYear != null) {
						return -1;
					} else if(rhsYear != null) {
						return 1;
					} else {
						return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
					}
				}

				Integer lhsDisc = lhs.getDiscNumber();
				Integer rhsDisc = rhs.getDiscNumber();

				if(lhsDisc != null && rhsDisc != null) {
					if(lhsDisc < rhsDisc) {
						return -1;
					} else if(lhsDisc > rhsDisc) {
						return 1;
					}
				}

				Integer lhsTrack = lhs.getTrack();
				Integer rhsTrack = rhs.getTrack();
				if(lhsTrack != null && rhsTrack != null) {
					return lhsTrack.compareTo(rhsTrack);
				} else if(lhsTrack != null) {
					return -1;
				} else if(rhsTrack != null) {
					return 1;
				}

				return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
			}

			public static void sort(List<Entry> entries) {
				try {
					Collections.sort(entries, new EntryComparator());
				} catch (Exception e) {
					LOG.warn("Failed to sort MusicDirectory");
			}
		}
	}
}