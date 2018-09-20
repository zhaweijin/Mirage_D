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
package org.teleal.cling.mediarenderer;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.mediarenderer.gstreamer.GstMediaRenderer;

import com.mirage.util.Utils;



import android.content.Context;

public class MediaRenderer {


    public static final int SUPPORTED_INSTANCES = 1;

    protected GstMediaRenderer mediaRenderer;
    
	public MediaRenderer(final Context context,final AndroidUpnpService upnpService) {

		Utils.print("init", "init start");
		 

	}
	
	
 

}
