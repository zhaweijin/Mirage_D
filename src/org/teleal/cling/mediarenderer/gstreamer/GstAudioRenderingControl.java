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

import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.teleal.cling.support.renderingcontrol.RenderingControlErrorCode;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;
import org.teleal.cling.support.model.Channel;

import android.content.Context;
import android.media.AudioManager;


import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class GstAudioRenderingControl extends AbstractAudioRenderingControl {

    final private static Logger log = Logger.getLogger(GstAudioRenderingControl.class.getName());
    private String TAG = "GstAudioRenderingControl";
    private GstMediaPlayer player;
    private Context context;
    protected GstAudioRenderingControl(LastChange lastChange, GstMediaPlayer player,Context context) {
        super(lastChange);
        this.player = player;
        this.context = context;
    }

    protected GstMediaPlayer getPlayer() {
        return player;
    }
    
    public void closePlayer()
    {
    	player = null;
    }

    protected GstMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        GstMediaPlayer player = getPlayer();
        if (player == null) {
            throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    protected void checkChannel(String channelName) throws RenderingControlException {
//    	Utils.print(TAG+"--checkChannel", "checkChannel");
        if (!getChannel(channelName).equals(Channel.Master)) {
            throw new RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
    	//Utils.print(TAG+"--getMute", "getMute");
        checkChannel(channelName);
        return true;

    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {
        checkChannel(channelName);
        log.fine("Setting backend mute to: " + desiredMute);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
//    	Utils.print(TAG+"--getVolume", "getVolume");
    	
    	AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //Utils.print("volume", ""+volume);    	
    	
    	checkChannel(channelName);
//        int vol = (int) (getInstance(instanceId).getVolume() * 100);
//        log.fine("Getting backend volume: " + vol);
        return new UnsignedIntegerTwoBytes(volume);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
//    	Utils.print(TAG+"--setVolume", "setVolume"+desiredVolume);
    	checkChannel(channelName);
    	double vol = Double.parseDouble(desiredVolume+"");
//        double vol = desiredVolume.getValue() / 100d;
//        Utils.print(TAG, vol+"");
////        log.fine("Setting backend volume to: " + vol);

 
    }

    @Override
    protected Channel[] getCurrentChannels() {
        return new Channel[] {
                Channel.Master
        };
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[1];

        ids[0] = new UnsignedIntegerFourBytes(0);
        return ids;
    }
}