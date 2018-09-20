/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
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

package org.teleal.cling.support.contentdirectory.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.teleal.cling.model.action.ActionException;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.support.contentdirectory.callback.Browse;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import com.mirage.dlna.HomeActivity;
import com.mirage.dlna.application.ContentConfigData;
import com.mirage.dlna.application.MessageControl;
import com.mirage.dmp.ContentItem;
import com.mirage.util.Utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Updates a tree model after querying a backend <em>ContentDirectory</em>
 * service.
 * 
 * @author Christian Bauer
 */
public class ContentBrowseActionCallback extends Browse {
    private final static String TAG="ContentBrowseActionCallback";
    
//	private static Logger log = Logger
//			.getLogger(ContentBrowseActionCallback.class.getName());

	private Service service;
	private Activity activity;
	
	private  ArrayList<ContentItem>  listcontent=new ArrayList<ContentItem>();
	private  ArrayList<ContentItem>  listphoto=new ArrayList<ContentItem>();
	private  ArrayList<ContentItem>  listmusic=new ArrayList<ContentItem>();
	private  ArrayList<ContentItem>  listvideo=new ArrayList<ContentItem>();
	private Handler mHandler;
	private static int loadfileFailure=5;
	
	
	
	protected DefaultTreeModel treeModel;
    protected DefaultMutableTreeNode treeNode;

	public ContentBrowseActionCallback(Service service, DefaultTreeModel treeModel, DefaultMutableTreeNode treeNode) {
        super(service, ((Container) treeNode.getUserObject()).getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0, null, new SortCriterion(true, "dc:title"));
        this.treeModel = treeModel;
        this.treeNode = treeNode;
    }

    public ContentBrowseActionCallback(Service service, DefaultTreeModel treeModel, DefaultMutableTreeNode treeNode,
                                       String filter, long firstResult, long maxResults, SortCriterion... orderBy) {
        super(service, ((Container) treeNode.getUserObject()).getId(), BrowseFlag.DIRECT_CHILDREN, filter, firstResult, maxResults, orderBy);
        this.treeModel = treeModel;
        this.treeNode = treeNode;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
    }

    

	public ContentBrowseActionCallback(Activity activity, Service service,
			Container container) {
		super(service, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
				null, new SortCriterion(true, "dc:title"));
		this.activity = activity;
		this.service = service;
	}
	
	public ContentBrowseActionCallback(Activity activity, Service service,
			Container container,Handler mHandler) {
		super(service, container.getId(), BrowseFlag.DIRECT_CHILDREN, "*", 0,
				null, new SortCriterion(true, "dc:title"));
		Log.i(TAG , "ContentBrowseActionCallback");
		this.activity = activity;
		this.service = service;
		this.mHandler=mHandler;
	}
	

	public void received(final ActionInvocation actionInvocation,
			final DIDLContent didl) {
		Utils.print(TAG, "received");
//		log.fine("Received browse action DIDL descriptor, creating tree nodes");
		activity.runOnUiThread(new Runnable() {
			public void run() {
				
			//	list.removeAll(list);
//				Log.i(TAG , "yehongjian");
//				MyApplication  myApplication=(MyApplication)activity.getApplication();
//				myApplication.didl=didl;
				try {
					//listAdapter.clear();
//					Log.i(TAG , "didl.getContainers()====="+didl.getContainers().size());
					// Containers first
					for (Container childContainer : didl.getContainers()) {
//						Log.i(TAG , "childContainer"+childContainer);
//						Log.i(TAG , "service"+service);
//						log.fine("add child container " + childContainer.getTitle());
					//	listAdapter.add(new ContentItem(childContainer, service));					
						listcontent.add(new ContentItem(childContainer, service));
					}
					
					
					// Now items
					for (Item childItem : didl.getItems()) {
					
					
						ContentItem contentItem=new ContentItem(childItem, service);
						
//						Utils.print("333", contentItem.getItem().g);
//						contentItem.getItem().
						if(contentItem.getItem().getTitle().toString()!=null){
							
							if(contentItem.getItem().getResources()!=null){
								List <Res> res =contentItem.getItem().getResources();
								if(res.size()!=0){
									if(res.get(0).getProtocolInfo()!=null){
										if(res.get(0).getProtocolInfo().getContentFormat()!=null){
											
											if(res.get(0).getProtocolInfo().getContentFormat().substring(0, res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("image")){
												listphoto.add(new ContentItem(childItem, service));
												listcontent.add(new ContentItem(childItem, service));
												
											}else if(res.get(0).getProtocolInfo().getContentFormat().substring(0, res.get(0).getProtocolInfo().getContentFormat().indexOf("/")).equals("audio")){
												listmusic.add(new ContentItem(childItem, service));
												listcontent.add(new ContentItem(childItem, service));
											}else{
												
												listvideo.add(new ContentItem(childItem, service));
												listcontent.add(new ContentItem(childItem, service));
												
											}
											
										}
									}
								}
							}

													
							
						}
						
						
						
//						
//					if(childItem.toString().substring(childItem.toString().lastIndexOf(".")+1,childItem.toString().lastIndexOf("@")).equals("Photo")){
//						
//						listphoto.add(new ContentItem(childItem, service));
//						list1.add(new ContentItem(childItem, service));
//					}else{
//						listnophoto.add(new ContentItem(childItem, service));
//						list1.add(new ContentItem(childItem, service));
//					}
					
					
//						log.fine("add child item" + childItem.getTitle());
//						Log.i(TAG , "childItem"+childItem.getTitle());
//						Log.i(TAG , "service"+service);
					//	listAdapter.add(new ContentItem(childItem, service));
						
					}
					if(!((HomeActivity)activity).isFinishing()){
						Message msg=new Message();
						ContentConfigData myContentConfig=new ContentConfigData();
						myContentConfig.listcontent=listcontent;
						myContentConfig.listphoto=listphoto;
						myContentConfig.listmusic=listmusic;
						myContentConfig.listvideo=listvideo;
						msg.what=MessageControl.CONTAINER_REFLESH;
				        msg.obj=myContentConfig;
				        mHandler.sendMessage(msg);
					}
					
				} catch (Exception ex) {
					System.out.println("Creating DIDL tree nodes failed:");
//					log.fine("Creating DIDL tree nodes failed: " + ex);
					actionInvocation.setFailure(new ActionException(
							ErrorCode.ACTION_FAILED,
							"Can't create list childs: " + ex, ex));
					failure(actionInvocation, null);
				    
				
				}
			}
		});
	}

	public void updateStatus(final Status status) {
	}

	
	@Override
	public void failure(ActionInvocation invocation, UpnpResponse operation,
			final String defaultMsg) {
		Utils.print(TAG, "content  failuer");
		Message msg = new Message();
		msg.what = MessageControl.LOADING_FAILED;
		mHandler.sendMessage(msg);
	}
}
