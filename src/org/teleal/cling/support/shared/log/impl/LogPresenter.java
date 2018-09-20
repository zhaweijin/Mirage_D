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

package org.teleal.cling.support.shared.log.impl;

import org.teleal.cling.support.shared.TextExpand;
import org.teleal.cling.support.shared.log.LogView;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.SwingUtilities;

/**
 * @author Christian Bauer
 */

//add modify by carter
//implements LogView.Presenter
public class LogPresenter  {


    protected LogView view;


//    protected Event<TextExpand> textExpandEvent;
//
//    public void init() {
//        view.setPresenter(this);
//    }
//
//    @Override
//    public void onExpand(LogMessage logMessage) {
//        textExpandEvent.fire(new TextExpand(logMessage.getMessage()));
//    }


    public void destroy() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                view.dispose();
            }
        });
    }

//    @Override
//    public void pushMessage(final LogMessage message) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                view.pushMessage(message);
//            }
//        });
//    }

}
