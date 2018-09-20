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



import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.TransportAction;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.model.StorageMedium;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.model.Channel;
import org.teleal.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.teleal.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;


import com.mirage.util.Utils;


import java.io.File;
import java.io.IOException;
import java.net.URI;
/**
 * @author Christian Bauer
 */
//extends PlayBinMediaPlayer 
public class GstMediaPlayer  {

//    final private static Logger log = Logger.getLogger(GstMediaPlayer.class.getName());
    private String TAG = "GstMediaPlayer";
    final private int instanceId;
    final private LastChange avTransportLastChange;
    final private LastChange renderingControlLastChange;

    private URI currentPlayUri;

    
    // We'll synchronize read/writes to these fields
    private volatile TransportInfo currentTransportInfo = new TransportInfo();
    private PositionInfo currentPositionInfo = new PositionInfo();
    private MediaInfo currentMediaInfo = new MediaInfo();
    
    private double storedVolume;
    private Context context;

    
   
    
    public GstMediaPlayer(Context context,int instanceId,
                          LastChange avTransportLastChange,
                          LastChange renderingControlLastChange) {
        this.context = context;
        this.instanceId = instanceId;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
        
         
    }

    public int getInstanceId() {
       return instanceId;
    }

    public LastChange getAvTransportLastChange() {
        return avTransportLastChange;
    }

    public LastChange getRenderingControlLastChange() {
        return renderingControlLastChange;
    }

//    public VideoComponent getVideoComponent() {
//        return videoComponent;
//    }
//
//    // TODO: gstreamer-java has a broken implementation of getStreamInfo(), so we need to
//    // do our best fishing for the stream type inside the playbin pipeline
//
//    synchronized public boolean isDecodingStreamType(String prefix) {
//        for (Element element : getPipeline().getElements()) {
//            if (element.getName().matches("decodebin[0-9]+")) {
//                for (Pad pad : element.getPads()) {
//                    if (pad.getName().matches("src[0-9]+")) {
//                        Caps caps = pad.getNegotiatedCaps();
//                        Structure struct = caps.getStructure(0);
//                        if (struct.getName().startsWith(prefix + "/"))
//                            return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    synchronized public TransportInfo getCurrentTransportInfo() {
//    	Utils.print(TAG, currentTransportInfo.getCurrentTransportState()+"");
//    	Utils.print(TAG, currentTransportInfo.getCurrentTransportStatus()+"");
//    	Utils.print(TAG, currentTransportInfo.getCurrentSpeed()+"");
        return currentTransportInfo;
    }

    synchronized public PositionInfo getCurrentPositionInfo() {
 
        return currentPositionInfo;
    }

    synchronized public MediaInfo getCurrentMediaInfo() {
    	
 
        return currentMediaInfo;
    }


    synchronized public void setURI(URI uri,String type,String name) {

    	Utils.print("mediaplayer", "set uri"+uri.toString());
    	 
    }
    

    
    synchronized public void play(URI uri,String type,String name)
    {
    	PlayListener.setGstMediaPlayer(this);
    	
   
    }

   
  

    // Because we don't have an automated state machine, we need to calculate the possible transitions here

    synchronized public TransportAction[] getCurrentTransportActions() {
        TransportState state = currentTransportInfo.getCurrentTransportState();
        TransportAction[] actions;

        switch (state) {
            case STOPPED:
                actions = new TransportAction[]{
                        TransportAction.Play
                };
                break;
            case PLAYING:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek
                };
                break;
            case PAUSED_PLAYBACK:
                actions = new TransportAction[]{
                        TransportAction.Stop,
                        TransportAction.Pause,
                        TransportAction.Seek,
                        TransportAction.Play
                };
                break;
            default:
                actions = null;
        }
        return actions;
    }

//
    synchronized public void transportStateChanged(TransportState newState) {
    	Utils.print(TAG, "transportStateChanged");
        TransportState currentTransportState = currentTransportInfo.getCurrentTransportState();
        //log.fine("Current state is: " + currentTransportState + ", changing to new state: " + newState);
        currentTransportInfo = new TransportInfo(newState);

        getAvTransportLastChange().setEventedValue(
                getInstanceId(),
                new AVTransportVariable.TransportState(newState),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions())
        );
    }

//	synchronized public void positionChanged() {
//		currentPositionInfo = new PositionInfo(1,
//				currentMediaInfo.getMediaDuration(),
//				currentMediaInfo.getCurrentURI(),
//				// ModelUtil.toTimeString(evt.getPosition().toSeconds()),
//				// ModelUtil.toTimeString(evt.getPosition().toSeconds())
//				"", "");
//	}
//    
//	synchronized public void durationChanged() {
//
//		// String newValue =
//		// ModelUtil.toTimeString(evt.getDuration().toSeconds());
//		String newValue = "3";
//		currentMediaInfo = new MediaInfo(currentMediaInfo.getCurrentURI(), "",
//				new UnsignedIntegerFourBytes(1), newValue,
//				StorageMedium.NETWORK);
//
//		getAvTransportLastChange().setEventedValue(getInstanceId(),
//				new AVTransportVariable.CurrentTrackDuration(newValue),
//				new AVTransportVariable.CurrentMediaDuration(newValue));
//	}
    
    
    
//    protected class GstMediaListener implements MediaListener {
//
//        public void pause() {
//            transportStateChanged(TransportState.PAUSED_PLAYBACK);
//        }
//
//        public void start() {
//            transportStateChanged(TransportState.PLAYING);
//        }
//
//        public void stop() {
//            transportStateChanged(TransportState.STOPPED);
//        }
//
//        public void endOfMedia() {
//            //log.fine("End Of Media event received, stopping media player backend");
//            stop();
//        }
//
//        public void positionChanged() {
//            //log.fine("Position Changed event received: " + evt.getPosition());
//            synchronized (GstMediaPlayer.this) {
//                currentPositionInfo =
//                        new PositionInfo(
//                                1,
//                                currentMediaInfo.getMediaDuration(),
//                                currentMediaInfo.getCurrentURI(),
////                              ModelUtil.toTimeString(evt.getPosition().toSeconds()),
////                              ModelUtil.toTimeString(evt.getPosition().toSeconds())
//                                "",
//                                ""
//                        );
//            }
//        }
//
//        public void durationChanged() {
//            //log.fine("Duration Changed event received: " + evt.getDuration());
//            synchronized (GstMediaPlayer.this) {
////                String newValue = ModelUtil.toTimeString(evt.getDuration().toSeconds());
//            	String newValue = "3";
//                currentMediaInfo =
//                        new MediaInfo(
//                                currentMediaInfo.getCurrentURI(),
//                                "",
//                                new UnsignedIntegerFourBytes(1),
//                                newValue,
//                                StorageMedium.NETWORK
//                        );
//
//                getAvTransportLastChange().setEventedValue(
//                        getInstanceId(),
//                        new AVTransportVariable.CurrentTrackDuration(newValue),
//                        new AVTransportVariable.CurrentMediaDuration(newValue)
//                );
//            }
//        }
//    }
 

	 
	

}

