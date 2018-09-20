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

package org.teleal.cling.mediarenderer.gstreamer;


import org.teleal.cling.support.connectionmanager.ConnectionManagerService;
import org.teleal.cling.support.model.Protocol;
import org.teleal.cling.support.model.ProtocolInfo;

 


/**
 * @author carter
 */
public class GstConnectionManagerService extends ConnectionManagerService {


    private String[] contentFormat = {
 
    };
    
    
	public GstConnectionManagerService() {

		for (int i = 0; i < contentFormat.length; i++) {
			ProtocolInfo info = new ProtocolInfo(Protocol.HTTP_GET, "*",contentFormat[i], "*");
			sinkProtocolInfo.add(info);
		}

		// Utils.print("GstConnectionManagerService","Supported MIME types: " +
		// sinkProtocolInfo.size());
	}
    
    
    

}