/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.support.model.container;

import java.net.URI;
import java.util.List;

import static org.teleal.cling.support.model.DIDLObject.Property.UPNP;

/**
 * @author Christian Bauer
 */
public class MusicArtist extends PersonContainer {

    public static final Class CLASS = new Class("object.container.person.musicArtist");

    public MusicArtist() {
        setClazz(CLASS);
    }

    public MusicArtist(Container other) {
        super(other);
    }

    public MusicArtist(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount);
    }

    public MusicArtist(String id, String parentID, String title, String creator, Integer childCount) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
    }

    public String getFirstGenre() {
        return getFirstPropertyValue(UPNP.GENRE.class);
    }

    public String[] getGenres() {
        List<String> list = getPropertyValues(UPNP.GENRE.class);
        return list.toArray(new String[list.size()]);
    }

    public MusicArtist setGenres(String[] genres) {
        removeProperties(UPNP.GENRE.class);
        for (String genre : genres) {
            addProperty(new UPNP.GENRE(genre));
        }
        return this;
    }

    public URI getArtistDiscographyURI() {
        return getFirstPropertyValue(UPNP.ARTIST_DISCO_URI.class);
    }

    public MusicArtist setArtistDiscographyURI(URI uri) {
        replaceFirstProperty(new UPNP.ARTIST_DISCO_URI(uri));
        return this;
    }

}
