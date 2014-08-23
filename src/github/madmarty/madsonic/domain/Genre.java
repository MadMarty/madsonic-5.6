package github.madmarty.madsonic.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Genre implements Serializable {
	
	private String name;
    private String index;
    
	private Integer artistCount = 0;
	private Integer albumCount;
	private Integer songCount;
	
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
    
    @Override
    public String toString() {
        return name;
    }

	public Integer getAlbumCount() {
		return albumCount;
	}

	public void setAlbumCount(Integer albumCount) {
		this.albumCount = albumCount;
	}

	public Integer getSongCount() {
		return songCount;
	}

	public void setSongCount(Integer songCount) {
		this.songCount = songCount;
	}

	public Integer getArtistCount() {
		return artistCount;
	}

	public void setArtistCount(Integer artistCount) {
		if (artistCount != null) { 
			this.artistCount = artistCount;
		} 
	}

	public static class GenreComparator implements Comparator<Genre> {
		@Override
		public int compare(Genre genre1, Genre genre2) {
			return genre1.getName().compareToIgnoreCase(genre2.getName());
		}

		public static List<Genre> sort(List<Genre> genres) {
			Collections.sort(genres, new GenreComparator());
			return genres;
		}

	}
}
