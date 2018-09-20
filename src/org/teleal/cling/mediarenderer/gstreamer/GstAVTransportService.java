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


import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.AVTransportErrorCode;
import org.teleal.cling.support.avtransport.AVTransportException;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DeviceCapabilities;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PlayMode;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.SeekMode;
import org.teleal.cling.support.model.TransportAction;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportSettings;
import org.teleal.cling.support.model.StorageMedium;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.common.http.HttpFetch;
import org.teleal.common.util.URIUtil;

import android.R.integer;

import com.mirage.util.Utils;






import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class GstAVTransportService extends AbstractAVTransportService {

    final private static Logger log = Logger.getLogger(GstAVTransportService.class.getName());
    private String TAG = "GstAVTransportService";
    private  GstMediaPlayer player;

    protected GstAVTransportService(LastChange lastChange, GstMediaPlayer player) {
        super(lastChange);
        Utils.print(TAG, "init");
        this.player = player;
    }

    protected GstMediaPlayer getPlayer() {
        return player;
    }

    public void closePlayer()
    {
    	player = null;
    }
    
    protected GstMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        GstMediaPlayer player = getPlayer();
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
    	Utils.print(TAG, "setAVTransportURI"+currentURI);
//    	currentURI = currentURI.replace("&", "%26");
    	currentURIMetaData = currentURIMetaData.replace("&", "%26");
//    	getMineType(currentURIMetaData);
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (Exception ex) {
                throw new AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()
                );
            }
        } else if (!currentURI.startsWith("file:")) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            );
        }

        // TODO: Check mime type of resource against supported types

        // TODO: DIDL fragment parsing and handling of currentURIMetaData
//        Utils.print(TAG,getName(currentURIMetaData)+"###");
       
    }

     
    
  
    
    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	//Utils.print(TAG, "getMediaInfo");
        return getInstance(instanceId).getCurrentMediaInfo();
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	//Utils.print(TAG, "getTransportInfo");
        return getInstance(instanceId).getCurrentTransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	//Utils.print(TAG, "getPositionInfo");
        return getInstance(instanceId).getCurrentPositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	//Utils.print(TAG, "getDeviceCapabilities");
    	getInstance(instanceId);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	Utils.print(TAG, "getTransportSettings");
    	getInstance(instanceId);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	Utils.print(TAG, "stop");
 
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
    	Utils.print(TAG, "play");
   
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
    	Utils.print(TAG, "pause");
     
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Record");
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
        final GstMediaPlayer player = getInstance(instanceId);
        SeekMode seekMode;
        try {
//        	Utils.print("seek unit", unit+"@@"+target);
            seekMode = SeekMode.valueOrExceptionOf(unit);

            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }
 
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Next");
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: Previous");
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) throws AVTransportException {
        log.info("### TODO: Not implemented: SetNextAVTransportURI");
        // Not implemented
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetPlayMode");
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
        // Not implemented
        log.info("### TODO: Not implemented: SetRecordQualityMode");
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
    	

        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[1];

        ids[0] = new UnsignedIntegerFourBytes(0);
        return ids;
    }
}
