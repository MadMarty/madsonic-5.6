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

 Copyright 2010 (C) Sindre Mehus
 */
package github.madmarty.madsonic.util;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import github.madmarty.madsonic.activity.SubsonicTabActivity;
import github.madmarty.madsonic.domain.MusicDirectory;
import github.madmarty.madsonic.view.SongView;

/**
 * @author Sindre Mehus
 */
public class NowplayingEntryAdapter extends ArrayAdapter<MusicDirectory.Entry> {

    private final SubsonicTabActivity activity;
    private final ImageLoader imageLoader;
    private final boolean checkable;
    private final boolean dragable;
    
    public NowplayingEntryAdapter(SubsonicTabActivity activity, ImageLoader imageLoader, List<MusicDirectory.Entry> entries, boolean checkable, boolean dragable) {
        super(activity, android.R.layout.simple_list_item_1, entries);
        this.activity = activity;
        this.imageLoader = imageLoader;
        this.checkable = checkable;
        this.dragable = dragable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicDirectory.Entry entry = getItem(position);
    
            SongView view;
            if (convertView != null && convertView instanceof SongView) {
                view = (SongView) convertView;
            } else {
                view = new SongView(activity);
            }
            view.setObject(entry, checkable, dragable);
            return view;
    }
}
