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

import android.app.Activity;

/**
 * @author Sindre Mehus
 */
public abstract class SilentBackgroundTask<T> extends BackgroundTask<T> {

    public SilentBackgroundTask(Activity activity) {
        super(activity);
    }

    @Override
    public void execute() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final T result = doInBackground();

                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            done(result);
                        }
                    });

                } catch (final Throwable t) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            error(t);
                        }
                    });
                }
            }
        };
        thread.start();
    }

    @Override
    public void updateProgress(int messageId) {
    }

    @Override
    public void updateProgress(String message) {
    }
}
