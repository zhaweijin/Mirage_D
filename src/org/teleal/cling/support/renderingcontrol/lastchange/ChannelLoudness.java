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

package org.teleal.cling.support.renderingcontrol.lastchange;

import org.teleal.cling.support.model.Channel;

/**
 * @author Christian Bauer
 */
public class ChannelLoudness {

    protected Channel channel;
    protected Boolean loudness;

    public ChannelLoudness(Channel channel, Boolean loudness) {
        this.channel = channel;
        this.loudness = loudness;
    }

    public Channel getChannel() {
        return channel;
    }

    public Boolean getLoudness() {
        return loudness;
    }

    @Override
    public String toString() {
        return "Loudness: " + getLoudness() + " (" + getChannel() + ")";
    }
}
