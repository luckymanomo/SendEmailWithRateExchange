package com.sia.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.googlesheets.custom.RateDataBean;
import com.googlesheets.custom.ReadSpreadsheet;
import com.sia.email.SiaSendMailTLS;

public class SiaDataCollection {
	public static Proxy proxy;
	public static int secondTimeout;
	public static final String url="http://www.sia-moneyexchange.com/rate/";
	public static void initAuthenticator(){
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.scb.co.th", 8080));
		Authenticator authenticator = new Authenticator() {
	        public PasswordAuthentication getPasswordAuthentication() {
	            return (new PasswordAuthentication("s45440","Workplace@77".toCharArray()));
	        }
	    };
	    Authenticator.setDefault(authenticator);
	}
	public static void main(String[] args){
		
			URLConnection connection=null;
			//loadSheet
			try {
				authenAndLoadSheet();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//retrieve old
			RateDataBean oldRateDataBean=null;
			String rateChanged="";
			while(true){
				try{
					//initAuthenticator();
					if(proxy!=null) connection = new URL(url).openConnection(proxy);
					else connection = new URL(url).openConnection();
					connection.setConnectTimeout(secondTimeout*1000);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
					
					String line = null;
					StringBuffer stringBuffer = new StringBuffer();
					while ((line = bufferedReader.readLine()) != null) {
						stringBuffer.append(line);
					}
					Document document = Jsoup.parse(String.valueOf(stringBuffer));
					String lastUpdate=document.select("#upd").first().ownText();
					RateDataBean htmlRateDataBean=printExchangeRate("rate","JPY",document);
					String rate=htmlRateDataBean.getBuyingRate()+":"+htmlRateDataBean.getSellingRate();
					
					//retrieve
					oldRateDataBean=null;
					try{oldRateDataBean=ReadSpreadsheet.retrieveLastRecord();}catch(Exception e){}
					if(oldRateDataBean!=null) rateChanged=oldRateDataBean.getBuyingRate()+":"+oldRateDataBean.getSellingRate();
					System.out.println("rateChanged:"+rateChanged);
					if(rate!=null && !rate.equals(rateChanged)){
						/*rateChanged=rate;
						
						//retrieve old
						oldRateDataBean=null;
						try{oldRateDataBean=ReadSpreadsheet.retrieveLastRecord();}catch(Exception e){}*/
						//insert new
						try{ReadSpreadsheet.insertRecord(DateFormat.getInstance().format(new Date())+"",htmlRateDataBean.getBuyingRate()+"",htmlRateDataBean.getSellingRate()+"");}catch(Exception e){}
						
						String arrowBuying=null,arrowSelling=null;
						if(oldRateDataBean!=null){
							arrowBuying=getHtmlArrowImage(htmlRateDataBean.getBuyingRate(),oldRateDataBean.getBuyingRate());
							arrowSelling=getHtmlArrowImage(htmlRateDataBean.getSellingRate(),oldRateDataBean.getSellingRate());
						}
						String pfontSize="2";
						
						rate="Buying: ฿"+htmlRateDataBean.getBuyingRate()+" "+arrowBuying+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getBuyingRate()+"</font><br>Selling: ฿"+htmlRateDataBean.getSellingRate()+" "+arrowSelling+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getSellingRate()+"</font>";
						
						String body="<!-- Start of preheader --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><tr><td><table border='0' width='48%' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #282828;' align='right' valign='middle'><a style='text-decoration: none; color: #ffffff;' href='#'>[JPY][&yen;100]</a></td></tr></tbody></table></td></tr><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of preheader --><!-- Start of header --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center' bgcolor='#d41b29'><tbody><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --><tr><td><!-- logo --><table class='devicewidth' border='0' width='140' cellspacing='0' cellpadding='0' align='left'><tbody><tr><td align='center' width='140' height='60'><div class='imgpop'><a href='#' target='_blank'> <img style='display: block; border: none; outline: none; text-decoration: none; border-width: initial;vertical-align: middle;' src='https://lh3.googleusercontent.com/bvwu8jIgNA3lmE0lQsMTHe6S5-OlZTRM3qHC6oajVYDGvrvsuJt58ksUvn8aYBr3Oy9EaCw=s128' alt='' width='128' height='84' border='0' /> </a></div></td></tr></tbody></table><!-- end of logo --> <!-- start of menu --><table class='devicewidth' border='0' width='250' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 20px; color: #ffffff;' align='center' height='60'>SIA JAPAN RATE</td></tr></tbody></table><!-- end of menu --></td></tr><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of Header --><!-- Start of main-banner --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><h1 style='text-align: center;'>"
								+ "<span style='color: #ffffff;'>"+rate+"</span>"
								+ "</h1><!-- end of image --></td></tr></tbody></table></td></tr></tbody></table><!-- End of main-banner --><!-- Start of heading --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 24px; color: #ffffff; padding: 15px 0;' align='center' bgcolor='#d41b29'>"
								+lastUpdate+"</td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of heading --><!-- Start of Postfooter --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;<table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>"
								+ "Sent Time: "+new Date()+
								"</td></tr></tbody></table></td></tr><!-- Spacing --><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>Don't want to receive email Updates? <a style='text-decoration: none; color: #d41b29;' href='#'>Unsubscribe here </a></td></tr><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of postfooter -->";
						SiaSendMailTLS.sendMessage(body);
						System.out.println("Send an email: "+lastUpdate+" with "+rate+" "+new Date());
					}else{
						System.out.println("No updated: "+new Date());
					}
					
					Thread.sleep(1000*60);
				}catch(Exception e){e.printStackTrace();}
			}
		
	}
	public static String getHtmlArrowImage(double d1,double d2){
		String arrowYellow="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/YellowArrow.png' width='15px'>";
		String arrowGreen="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/GreenArrow.png'>";
		String arrowRed="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/RedArrow.png'>";
		String arrowColor="";
		if(d1==d2) arrowColor=arrowYellow;
		else if(d1>d2) arrowColor=arrowGreen;
		else arrowColor=arrowRed;
		return arrowColor;
	}
	public static void authenAndLoadSheet() throws Exception{
		try {
			ReadSpreadsheet.credential = ReadSpreadsheet.authorize();
			ReadSpreadsheet.loadSheet();
		} catch (com.google.gdata.util.AuthenticationException e) {
			e.printStackTrace();
			System.out.println("Trying connecting");
			System.out.println(ReadSpreadsheet.DATA_STORE_DIR.getAbsolutePath());
			new File(ReadSpreadsheet.DATA_STORE_DIR.getAbsolutePath()+File.separator+"StoredCredential").delete();
			ReadSpreadsheet.credential = ReadSpreadsheet.authorize();
			ReadSpreadsheet.loadSheet();
		}
		/*	
		try {
			RateDataBean rateDataBean=retrieveLastRecord();
			System.out.println(rateDataBean);
			//insertRecord(DateFormat.getInstance().format(new Date())+"","30.33","10.50");
		} catch (IOException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	public static void printAllCssUnderId(String id,Document document){
	    Element e = document.getElementById(id);
	    int hasDataCount=0;
	    for(int i=0;i<e.getAllElements().size();i++){
	    	String data=document.select(e.getAllElements().get(i).cssSelector()).first().ownText();
			if(!"".equals(data)){
				//String cssSelector=e.getAllElements().get(i).cssSelector();
				System.out.println(e.getAllElements().get(i).cssSelector());
				System.out.println("id "+hasDataCount+++": "+data);
				System.out.println("-------------------------");
			}
	    }
	}
	public static RateDataBean printExchangeRate(String elementId,String exchangeRate,Document document){

		String cssSelector=document.getElementById(elementId).getElementsContainingOwnText(exchangeRate).get(0).cssSelector();
		String parentTag=cssSelector.substring(0, cssSelector.lastIndexOf(":"));
		
		String buyingRate=document.select(parentTag+":nth-child(4) > table > tbody > tr > td.show-rate").first().ownText();
		String sellingRate=document.select(parentTag+":nth-child(5) > table > tbody > tr > td.show-rate").first().ownText();
		DecimalFormat decimalFormat=new DecimalFormat("0.00");
		try{buyingRate=decimalFormat.format(Double.parseDouble(buyingRate)*100);}catch(Exception e){}
		try{sellingRate=decimalFormat.format(Double.parseDouble(sellingRate)*100);}catch(Exception e){}
		//return ("["+exchangeRate+"][¥100], Buying: ฿"+buyingRate+", Selling: ฿"+sellingRate+"");
		return new RateDataBean(new Date(),Double.parseDouble(buyingRate),Double.parseDouble(sellingRate));
		//return ("Buying: ฿"+buyingRate+"<br>Selling: ฿"+sellingRate+"");
		
	}
}
