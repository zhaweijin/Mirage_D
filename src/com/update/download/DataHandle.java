package com.update.download;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



/**
 * @author zhaweijin
 * @function 
 */
public class DataHandle extends DefaultHandler{

	private DataSet dataSet;
	String tagName;
	WebData webData;
	

 	public void startDocument()throws SAXException
	{
		dataSet = new DataSet();
	}
	
	public DataSet getDataSet()
	{
		return this.dataSet;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startDocument();
		//DownloadUtils.print("localname", localName);
		tagName = localName;
		if (tagName.equals("apklist")) { 
			webData = new WebData();
		}
		
	}
	
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		String tmp = new String(ch, start, length);
		//DownloadUtils.print("characters"+tagName, "--->"+tmp);
		if (tagName.equals("name")) {
			if(webData.getName()==null)
				webData.setName("");
			webData.setName(webData.getName()+tmp);
		}
		else if(tagName.equals("url"))
		{
			if(webData.getWebpath()==null)
				webData.setWebpath("");
			webData.setWebpath(webData.getWebpath()+tmp);
		}
		else if(tagName.equals("ver"))
		{
			if(webData.getVersion()==null)
				webData.setVersion("");
			webData.setVersion(webData.getVersion()+tmp);
		}
		else if(tagName.equals("des"))
		{
			if(webData.getDescription()==null)
				webData.setDescription("");
			webData.setDescription(webData.getDescription()+tmp);
		}
	}
	
	public void endElement(String uri, String localName, String qName)   throws SAXException 
	{
		//Utils.print("end_localname", localName);
		if (localName.equals("apklist")) {
			dataSet.getWebDatas().add(webData);
		}

	}
	
	public void endDocument() throws SAXException
	{
		super.endDocument();
	}
	
}
