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

import java.io.Serializable;

public class ChatMessage implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 496544310289324167L;
	private String username;
	private Long time;
	private String message;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ChatMessage that = (ChatMessage) o;

		return message.equals(that.message) && time.equals(that.time) && username.equals(that.username);
	}

	@Override
	public int hashCode()
	{
		int result = username.hashCode();
		result = 31 * result + time.hashCode();
		result = 31 * result + message.hashCode();
		return result;
	}
}
