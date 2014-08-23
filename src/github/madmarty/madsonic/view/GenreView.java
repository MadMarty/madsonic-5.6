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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import github.madmarty.madsonic.R;
import github.madmarty.madsonic.domain.Genre;

public class GenreView extends UpdateView {
	
	private static final String TAG = GenreView.class.getSimpleName();

	private TextView titleView;
	private TextView genreInfoView;
	

	public GenreView(Context context) {
		
		super(context);
		LayoutInflater.from(context).inflate(R.layout.genre_list_item, this, true);
		titleView = (TextView) findViewById(R.id.genre_name);
		genreInfoView = (TextView) findViewById(R.id.genre_info);
	}

	public void setObjectImpl(Object obj) {
		
		Genre genre = (Genre) obj;
		titleView.setText(genre.getName());

		if(genre.getAlbumCount() != null) {
			
			genreInfoView.setVisibility(View.VISIBLE);
			genreInfoView.setText(context.getResources().getString(R.string.select_genre_all, genre.getSongCount(), genre.getAlbumCount(), genre.getArtistCount()));
		} else {
			genreInfoView.setVisibility(View.GONE);
		}
	}
}
