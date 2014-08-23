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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import github.madmarty.madsonic.activity.SubsonicTabActivity;
import github.madmarty.madsonic.domain.Artist;
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.domain.PodcastChannel;
import github.madmarty.madsonic.domain.PodcastEpisode;

/**
 * @author Sindre Mehus
 */
public class FileUtil {

    private static final Logger LOG = new Logger(FileUtil.class);
    
    private static final String[] FILE_SYSTEM_UNSAFE = {"/", "\\", "..", ":", "\"", "?", "*", "<", ">", "|"};
    private static final String[] FILE_SYSTEM_UNSAFE_DIR = {"\\", "..", ":", "\"", "?", "*", "<", ">", "|"};
    private static final List<String> MUSIC_FILE_EXTENSIONS = Arrays.asList("mp3", "ogg", "oga", "aac", "flac", "m4a", "wav", "wma");
	private static final List<String> VIDEO_FILE_EXTENSIONS = Arrays.asList("flv", "mp4", "m4v", "wmv", "avi", "mov", "mpg", "mkv");
	private static final List<String> PLAYLIST_FILE_EXTENSIONS = Arrays.asList("m3u");
	
    private static File DEFAULT_MUSIC_DIR;
	
	public static File getAnySong(Context context) {
		File dir = getMusicDirectory(context);
		return getAnySong(context, dir);
	}
	
	private static File getAnySong(Context context, File dir) {
		for(File file: dir.listFiles()) {
			if(file.isDirectory()) {
				return getAnySong(context, file);
			}
			
			String extension = getExtension(file.getName());
			if(MUSIC_FILE_EXTENSIONS.contains(extension)) {
				return file;
			}
		}
		
		return null;
	}

    public static File getSongFile(Context context, MusicDirectory.Entry song) {
        File dir = getAlbumDirectory(context, song);

        StringBuilder fileName = new StringBuilder();
        Integer track = song.getTrack();
        if (track != null) {
            if (track < 10) {
                fileName.append("0");
            }
            fileName.append(track).append("-");
        }

        fileName.append(fileSystemSafe(song.getTitle())).append(".");

        if (song.getTranscodedSuffix() != null) {
            fileName.append(song.getTranscodedSuffix());
        } else {
            fileName.append(song.getSuffix());
        }

        return new File(dir, fileName.toString());
    }
	
	public static File getPlaylistFile(Context context, String id) {
		File playlistDir = getPlaylistDirectory(context);
		return new File(playlistDir, id);
	}
	
	public static File getPlaylistDirectory(Context context) {
		File playlistDir = new File(getMadsonicDirectory(context), "playlists");
		ensureDirectoryExistsAndIsReadWritable(playlistDir);
		return playlistDir;
	}
	
	public static File getPlaylistDirectory(Context context, String server) {
		File playlistDir = new File(getPlaylistDirectory(context), server);
		ensureDirectoryExistsAndIsReadWritable(playlistDir);
		return playlistDir;
	}

    public static File getAlbumArtFile(Context context, MusicDirectory.Entry entry) {
        File albumDir = getAlbumDirectory(context, entry);
        return getAlbumArtFile(albumDir);
    }

	public static File getAvatarFile(Context context, String username)
	{
		File avatarArtDir = getAvatarArtDirectory(context);

		if (avatarArtDir == null || username == null)
		{
			return null;
		}

		String md5Hex = Util.md5Hex(username);
		return new File(avatarArtDir, String.format("%s.jpeg", md5Hex));
	}

    public static File getAlbumArtFile(File albumDir) {
        return new File(albumDir, Constants.ALBUM_ART_FILE);
    }

	public static Bitmap getAvatarBitmap(Context context, String username, int size)
	{
		if (username == null) return null;

		File avatarFile = getAvatarFile(context, username);

		SubsonicTabActivity subsonicTabActivity = SubsonicTabActivity.getInstance();
		Bitmap bitmap = null;
		ImageLoader imageLoader = null;

		if (subsonicTabActivity != null)
		{
			imageLoader = subsonicTabActivity.getImageLoader();

			if (imageLoader != null)
			{
				bitmap = imageLoader.getImageBitmap(username, size);
			}
		}

		if (bitmap != null)
		{
			return bitmap.copy(bitmap.getConfig(), false);
		}

		if (avatarFile != null && avatarFile.exists())
		{
			final BitmapFactory.Options opt = new BitmapFactory.Options();

			if (size > 0)
			{
				opt.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(avatarFile.getPath(), opt);

				opt.inDither = true;
				opt.inPreferQualityOverSpeed = true;

				opt.inPurgeable = true;
				opt.inSampleSize = Util.calculateInSampleSize(opt, size, Util.getScaledHeight(opt.outHeight, opt.outWidth, size));
				opt.inJustDecodeBounds = false;
			}

			try
			{
				bitmap = BitmapFactory.decodeFile(avatarFile.getPath(), opt);
			}
			catch (Exception ex)
			{
				LOG.error("Exception in BitmapFactory.decodeFile()", ex);
			}

			Log.i("getAvatarBitmap", String.valueOf(size));

			if (bitmap != null)
			{
				if (imageLoader != null)
				{
					imageLoader.addImageToCache(bitmap, username, size);
				}
			}

			return bitmap == null ? null : bitmap;
		}

		return null;
	}

    public static Bitmap getAlbumArtBitmap(Context context, MusicDirectory.Entry entry, int size) {
        File albumArtFile = getAlbumArtFile(context, entry);
        if (albumArtFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(albumArtFile.getPath());
			return (bitmap == null) ? null : Bitmap.createScaledBitmap(bitmap, size, size, true);
        }
        return null;
    }

	public static Bitmap getAvatarBitmap(Context context, String username, int size, boolean highQuality)
	{
		if (username == null) return null;

		File avatarFile = getAvatarFile(context, username);

		SubsonicTabActivity subsonicTabActivity = SubsonicTabActivity.getInstance();
		Bitmap bitmap = null;
		ImageLoader imageLoader = null;

		if (subsonicTabActivity != null)
		{
			imageLoader = subsonicTabActivity.getImageLoader();

			if (imageLoader != null)
			{
				bitmap = imageLoader.getImageBitmap(username, size);
			}
		}

		if (bitmap != null)
		{
			return bitmap.copy(bitmap.getConfig(), false);
		}

		if (avatarFile != null && avatarFile.exists())
		{
			final BitmapFactory.Options opt = new BitmapFactory.Options();

			if (size > 0)
			{
				opt.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(avatarFile.getPath(), opt);

				if (highQuality)
				{
					opt.inDither = true;
					opt.inPreferQualityOverSpeed = true;
				}

				opt.inPurgeable = true;
				opt.inSampleSize = Util.calculateInSampleSize(opt, size, Util.getScaledHeight(opt.outHeight, opt.outWidth, size));
				opt.inJustDecodeBounds = false;
			}

			try
			{
				bitmap = BitmapFactory.decodeFile(avatarFile.getPath(), opt);
			}
			catch (Exception ex)
			{
				LOG.error("Exception in BitmapFactory.decodeFile()", ex);
			}

			Log.i("getAvatarBitmap", String.valueOf(size));

			if (bitmap != null)
			{
				if (imageLoader != null)
				{
					imageLoader.addImageToCache(bitmap, username, size);
				}
			}

			return bitmap == null ? null : bitmap;
		}

		return null;
	}

	public static Bitmap getAlbumArtBitmap(Context context, MusicDirectory.Entry entry, int size, boolean highQuality)
	{
		if (entry == null) return null;

		File albumArtFile = getAlbumArtFile(context, entry);

		SubsonicTabActivity subsonicTabActivity = SubsonicTabActivity.getInstance();
		Bitmap bitmap = null;
		ImageLoader imageLoader = null;

		if (subsonicTabActivity != null)
		{
			imageLoader = subsonicTabActivity.getImageLoader();

			if (imageLoader != null)
			{
				bitmap = imageLoader.getImageBitmap(entry, true, size);
			}
		}

		if (bitmap != null)
		{
			return bitmap.copy(bitmap.getConfig(), false);
		}

		if (albumArtFile != null && albumArtFile.exists())
		{
			final BitmapFactory.Options opt = new BitmapFactory.Options();

			if (size > 0)
			{
				opt.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(albumArtFile.getPath(), opt);

				if (highQuality)
				{
					opt.inDither = true;
					opt.inPreferQualityOverSpeed = true;
				}

				opt.inPurgeable = true;
				opt.inSampleSize = Util.calculateInSampleSize(opt, size, Util.getScaledHeight(opt.outHeight, opt.outWidth, size));
				opt.inJustDecodeBounds = false;
			}

			try
			{
				bitmap = BitmapFactory.decodeFile(albumArtFile.getPath(), opt);
			}
			catch (Exception ex)
			{
				LOG.error("Exception in BitmapFactory.decodeFile()", ex);
			}

			Log.i("getAlbumArtBitmap", String.valueOf(size));

			if (bitmap != null)
			{
				if (imageLoader != null)
				{
					imageLoader.addImageToCache(bitmap, entry, size);
				}
			}

			return bitmap == null ? null : bitmap;
		}

		return null;
	}
    
	public static Bitmap getSampledBitmap(byte[] bytes, int size, boolean highQuality)
	{
		final BitmapFactory.Options opt = new BitmapFactory.Options();

		if (size > 0)
		{
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);

			if (highQuality)
			{
				opt.inDither = true;
				opt.inPreferQualityOverSpeed = true;
			}

			opt.inPurgeable = true;
			opt.inSampleSize = Util.calculateInSampleSize(opt, size, Util.getScaledHeight(opt.outHeight, opt.outWidth, size));
			opt.inJustDecodeBounds = false;
		}

		Log.i("getSampledBitmap", String.valueOf(size));
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
	}
	
	public static Bitmap getSampledBitmap(byte[] bytes, int size)
	{
		final BitmapFactory.Options opt = new BitmapFactory.Options();

		if (size > 0)
		{
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);

			opt.inDither = true;
			opt.inPreferQualityOverSpeed = true;

			opt.inPurgeable = true;
			opt.inSampleSize = Util.calculateInSampleSize(opt, size, Util.getScaledHeight(opt.outHeight, opt.outWidth, size));
			opt.inJustDecodeBounds = false;
		}

		Log.i("getSampledBitmap", String.valueOf(size));
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
	}
    
	public static File getArtistDirectory(Context context, Artist artist) {
		File dir = new File(getMusicDirectory(context).getPath() + "/music/" + fileSystemSafe(artist.getName()));
		return dir;
	}

	public static File getAvatarArtDirectory(Context context) {
		File avatarArtDir = new File(getMadsonicDirectory(context), "avatar");
		ensureDirectoryExistsAndIsReadWritable(avatarArtDir);
		ensureDirectoryExistsAndIsReadWritable(new File(avatarArtDir, ".nomedia"));
		return avatarArtDir;
	}
	
	public static File getAlbumArtDirectory(Context context) {
		File albumArtDir = new File(getMadsonicDirectory(context), "artwork");
		ensureDirectoryExistsAndIsReadWritable(albumArtDir);
		ensureDirectoryExistsAndIsReadWritable(new File(albumArtDir, ".nomedia"));
		return albumArtDir;
	}
	
    public static File getAlbumDirectory(Context context, MusicDirectory.Entry entry) {
        File dir;
        if (entry.getPath() != null) {
            File f = new File(fileSystemSafeDir(entry.getPath()));
            dir = new File(getMusicDirectory(context).getPath() + "/" + (entry.isDirectory() ? f.getPath() : f.getParent()));
        } else {
            String artist = fileSystemSafe(entry.getArtist());
            String album = fileSystemSafe(entry.getAlbum());
			if("unnamed".equals(album)) {
            	album = fileSystemSafe(entry.getTitle());
            }
            dir = new File(getMusicDirectory(context).getPath() + "/" + artist + "/" + album);
        }
        return dir;
    }
	
	public static String getPodcastPath(Context context, PodcastEpisode episode) {
		return fileSystemSafe(episode.getArtist()) + "/" + fileSystemSafe(episode.getTitle());
	}
	public static File getPodcastFile(Context context, String server) {
		File dir = getPodcastDirectory(context);
		return new File(dir.getPath() + "/" +  fileSystemSafe(server));
	}
	public static File getPodcastDirectory(Context context) {
		File dir = new File(context.getCacheDir(), "podcasts");
		ensureDirectoryExistsAndIsReadWritable(dir);
		return dir;
	}
	public static File getPodcastDirectory(Context context, PodcastChannel channel) {
		File dir = new File(getMusicDirectory(context).getPath() + "/" + fileSystemSafe(channel.getName()));
		return dir;
	}
	public static File getPodcastDirectory(Context context, String channel) {
		File dir = new File(getMusicDirectory(context).getPath() + "/" + fileSystemSafe(channel));
		return dir;
	}

    public static void createDirectoryForParent(File file) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LOG.error("Failed to create directory " + dir);
            }
        }
    }

    private static File createDirectory(Context context, String name) {
        File dir = new File(getMadsonicDirectory(context), name);
        if (!dir.exists() && !dir.mkdirs()) {
            LOG.error("Failed to create " + name);
        }
        return dir;
    }

    
    public static File getMadsonicDirectory(Context context) {

        // Starting with KitKat, write access is not always allowed outside the app's private directory.
        // Use the app-private dir that is now recommended by Android.
        // Note that we select the second directory (if available) as the first one
        // is typically an emulated external directory not physically located on the SD card.
        if (Build.VERSION.SDK_INT >= 19) {
            File[] externalDirs = context.getExternalFilesDirs(null);
            File externalDir = externalDirs[0];
            if (externalDirs.length > 1 && ensureDirectoryExistsAndIsReadWritable(externalDirs[1])) {
                externalDir = externalDirs[1];
            }
            return new File(externalDir, "madsonic");
        }

        // Otherwise, use the directory specified in the settings if we can.
        String path = Util.getPreferences(context).getString(Constants.PREFERENCES_KEY_CACHE_LOCATION, null);
        if (path != null) {
            File dir = new File(path);
            if (ensureDirectoryExistsAndIsReadWritable(dir)) {
                return dir;
            }
        }

        // Pre-KitKat and user hasn't specified anything.
        return new File(Environment.getExternalStorageDirectory(), "madsonic");
    }
    
//    public static File getMadsonicDirectory(Context context) {
//        return context.getExternalFilesDir(null);
//    }
    
    public static String getDefaultDownloadDirectory(){
    	return Environment.getExternalStorageDirectory() + "/Download/";
    }

    public static File getDefaultMusicDirectory(Context context) {
		if(DEFAULT_MUSIC_DIR == null) {
			DEFAULT_MUSIC_DIR = createDirectory(context, "music");
		}

        return DEFAULT_MUSIC_DIR;
    }

    public static File getMusicDirectory(Context context) {
        String path = Util.getPreferences(context).getString(Constants.PREFERENCES_KEY_CACHE_LOCATION + "/music/", getDefaultMusicDirectory(context).getPath());
        File dir = new File(path);
        return ensureDirectoryExistsAndIsReadWritable(dir) ? dir : getDefaultMusicDirectory(context);
    }
    
	public static boolean deleteMusicDirectory(Context context) {
		File musicDirectory = FileUtil.getMusicDirectory(context);
		return Util.recursiveDelete(musicDirectory);
	}
	
	public static void deleteSerializedCache(Context context) {
		for(File file: context.getCacheDir().listFiles()) {
			if(file.getName().indexOf(".ser") != -1) {
				file.delete();
			}
		}
	}

    public static boolean ensureDirectoryExistsAndIsReadWritable(File dir) {
        if (dir == null) {
            return false;
        }

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                LOG.warn(dir + " exists but is not a directory.");
                return false;
            }
        } else {
            if (dir.mkdirs()) {
                LOG.info("Created directory " + dir);
            } else {
                LOG.warn("Failed to create directory " + dir);
                return false;
            }
        }

        if (!dir.canRead()) {
            LOG.warn("No read permission for directory " + dir);
            return false;
        }

        if (!dir.canWrite()) {
            LOG.warn("No write permission for directory " + dir);
            return false;
        }
        return true;
    }
	public static boolean verifyCanWrite(File dir) {
		if(ensureDirectoryExistsAndIsReadWritable(dir)) {
			try {
				File tmp = new File(dir, "tmp");
				if(tmp.createNewFile()) {
					tmp.delete();
					return true;
				} else {
					return false;
				}
			} catch(Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

    /**
    * Makes a given filename safe by replacing special characters like slashes ("/" and "\")
    * with dashes ("-").
    *
    * @param filename The filename in question.
    * @return The filename with special characters replaced by hyphens.
    */
    private static String fileSystemSafe(String filename) {
        if (filename == null || filename.trim().length() == 0) {
            return "unnamed";
        }

        for (String s : FILE_SYSTEM_UNSAFE) {
            filename = filename.replace(s, "-");
        }
        return filename;
    }

    /**
     * Makes a given filename safe by replacing special characters like colons (":")
     * with dashes ("-").
     *
     * @param path The path of the directory in question.
     * @return The the directory name with special characters replaced by hyphens.
     */
    private static String fileSystemSafeDir(String path) {
        if (path == null || path.trim().length() == 0) {
            return "";
        }

        for (String s : FILE_SYSTEM_UNSAFE_DIR) {
            path = path.replace(s, "-");
        }
        return path;
    }

    /**
     * Similar to {@link File#listFiles()}, but returns a sorted set.
     * Never returns {@code null}, instead a warning is logged, and an empty set is returned.
     */
    public static SortedSet<File> listFiles(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            LOG.warn("Failed to list children for " + dir.getPath());
            return new TreeSet<File>();
        }

        return new TreeSet<File>(Arrays.asList(files));
    }

    public static SortedSet<File> listMediaFiles(File dir) {
        SortedSet<File> files = listFiles(dir);
        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (!file.isDirectory() && !isMediaFile(file)) {
                iterator.remove();
            }
        }
        return files;
    }

    private static boolean isMediaFile(File file) {
        String extension = getExtension(file.getName());
        return MUSIC_FILE_EXTENSIONS.contains(extension) || VIDEO_FILE_EXTENSIONS.contains(extension);
    }
	
	public static boolean isMusicFile(File file) {
		String extension = getExtension(file.getName());
        return MUSIC_FILE_EXTENSIONS.contains(extension);
	}
	public static boolean isVideoFile(File file) {
		String extension = getExtension(file.getName());
        return VIDEO_FILE_EXTENSIONS.contains(extension);
	}
	
	public static boolean isPlaylistFile(File file) {
		String extension = getExtension(file.getName());
		return PLAYLIST_FILE_EXTENSIONS.contains(extension);
	}

    /**
     * Returns the extension (the substring after the last dot) of the given file. The dot
     * is not included in the returned extension.
     *
     * @param name The filename in question.
     * @return The extension, or an empty string if no extension is found.
     */
    public static String getExtension(String name) {
        int index = name.lastIndexOf('.');
        return index == -1 ? "" : name.substring(index + 1).toLowerCase(Locale.getDefault());
    }

    /**
     * Returns the base name (the substring before the last dot) of the given file. The dot
     * is not included in the returned basename.
     *
     * @param name The filename in question.
     * @return The base name, or an empty string if no basename is found.
     */
    public static String getBaseName(String name) {
        int index = name.lastIndexOf('.');
        return index == -1 ? name : name.substring(0, index);
    }
	
	public static long getUsedSize(Context context, File file) {
		long size = 0L;
		
		if(file.isFile()) {
			return file.length();
		} else {
			for (File child : FileUtil.listFiles(file)) {
				size += getUsedSize(context, child);
			}
			return size;
		}
	}

    public static <T extends Serializable> boolean serialize(Context context, T obj, String fileName) {
        File file = new File(context.getCacheDir(), fileName);
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(obj);
            LOG.info(String.format("Serialized object to %s", file));
            return true;
        } catch (Throwable x) {
        	LOG.warn(String.format("Failed to serialize object to %s", file));
            return false;
        } finally {
            Util.close(out);
        }
    }

    @SuppressWarnings("unchecked")
	public static <T extends Serializable> T deserialize(Context context, String fileName) {
        File file = new File(context.getCacheDir(), fileName);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            T result = ((T) in.readObject());
            LOG.info( String.format("Deserialized object from %s", file));
            return result;
        } catch (Throwable x) {
        	LOG.warn(String.format("Failed to deserialize object from %s", file), x);
            return null;
        } finally {
            Util.close(in);
        }
    }

    public static boolean canWriteOrCreate(File file) {
        if (file.exists()) {
            return file.canWrite();
        }
        File parent = file.getParentFile();
        return parent != null && parent.exists() && parent.canWrite();
    }
}
