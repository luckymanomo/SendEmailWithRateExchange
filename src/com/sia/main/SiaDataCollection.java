package com.sia.main;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.googlesheets.custom.ConfigurationBean;
import com.googlesheets.custom.ImageArrowColorBean;
import com.googlesheets.custom.RateDataBean;
import com.googlesheets.custom.ReadSpreadsheet;
import com.sia.email.SiaSendMailTLS;

public class SiaDataCollection {
	public static boolean isTested=false; //testing and debugging
	
	public static Proxy proxy;
	public static int secondTimeout=5; //minute
	public static final String url="http://www.sia-moneyexchange.com/rate/";
	//public static String toEmails="luckymanomo@gmail.com,pin.ppr@gmail.com";
	public static URLConnection connection=null;
	
	
	public static final int BUYING_TYPE=1;
	public static final int SELLING_TYPE=2;
	public static ConfigurationBean configurationBean;
	public static final DecimalFormat decimalFormat=new DecimalFormat("0.00");
	public static void initAuthenticator(){
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.scb.co.th", 8080));
		Authenticator authenticator = new Authenticator() {
	        @Override
			public PasswordAuthentication getPasswordAuthentication() {
	            return (new PasswordAuthentication("s45440","Workplace@77".toCharArray()));
	        }
	    };
	    Authenticator.setDefault(authenticator);
	}
	public static void main(String[] args){
			try {
				authenticate();
				checkIfExpired(0);
				ReadSpreadsheet.loadSheet();
			} catch (Exception e1) {
				e1.printStackTrace();
				
			}
			
			while(true){
				
				System.out.println("AccessToken: "+ReadSpreadsheet.credential.getAccessToken());
				System.out.println("Refresh-Token: "+ReadSpreadsheet.credential.getRefreshToken());
				long expiredInMinutes=ReadSpreadsheet.credential.getExpiresInSeconds()/60;
				System.out.println("Token will be expired in "+expiredInMinutes+" minutes");
				
				int refreshTime=15;
				if(checkIfExpired(refreshTime)) ReadSpreadsheet.loadSheet(); //testing after exceeding interval time. (assumed in 1 minutes)
			
				retrieveAndSendEmail("JPY",100,"SIA-Japan","[JPY][&yen;100]","https://upload.wikimedia.org/wikipedia/en/thumb/9/9e/Flag_of_Japan.svg/510px-Flag_of_Japan.svg.png");	
				//retrieveAndSendEmail("HKD",1,"Sia-HongKong", "", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/Flag_of_Hong_Kong.svg/250px-Flag_of_Hong_Kong.svg.png");
				
				//interval time = 1 minute
				try {Thread.sleep(1000*60);} catch (InterruptedException e) {e.printStackTrace();}
			}
			
		
	}
	/**
	 * 
	 * @param cType, sheet name used for storing its history rate
	 * @param multiply
	 * @param label
	 * @param currencyString
	 * @param flagURL, flag image URL
	 */
	private static void retrieveAndSendEmail(String cType,int multiply,String label,String currencyString, String flagURL) {
		// TODO Auto-generated method stub
		
		try{
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
			String siaLastUpdate=document.select("#upd").first().ownText();
			RateDataBean htmlRateDataBean=printExchangeRate("rate",cType,document,multiply);
			String rate=htmlRateDataBean.getBuyingRate()+":"+htmlRateDataBean.getSellingRate();
			
			//retrieve old
			String rateChanged="";
			//retrieve old rate from spread sheet
			RateDataBean oldRateDataBean=null;
			oldRateDataBean=ReadSpreadsheet.retrieveLastRecord(cType);
			if(oldRateDataBean!=null) {
				rateChanged=oldRateDataBean.getBuyingRate()+":"+oldRateDataBean.getSellingRate();
				System.out.println("Last rate is "+rateChanged);
				if(isTested || (rate!=null && !rate.equals(rateChanged))){
					//load configuration
					configurationBean=ReadSpreadsheet.loadConfiguration("Configuration");
					//System.out.println("configurationBean:"+configurationBean);
					
					//insert new changed rate
					try{ReadSpreadsheet.insertRecord(cType,new SimpleDateFormat(ReadSpreadsheet.DATE_PATTERN,new Locale("en", "US")).format(new Date())+"",htmlRateDataBean.getBuyingRate()+"",htmlRateDataBean.getSellingRate()+"");}catch(Exception e){}
					
					String arrowBuying=null,arrowSelling=null;
					ImageArrowColorBean imBuy = null,imSell = null;
					if(oldRateDataBean!=null){
						imBuy=getHtmlArrowImage(htmlRateDataBean.getBuyingRate(),oldRateDataBean.getBuyingRate(),BUYING_TYPE);
						imSell=getHtmlArrowImage(htmlRateDataBean.getSellingRate(),oldRateDataBean.getSellingRate(),SELLING_TYPE);
						arrowBuying=imBuy.getArrow();
						arrowSelling=imSell.getArrow();
					}
					String pfontSize="2";
					
					rate="Buying: ฿"+htmlRateDataBean.getBuyingRate()+" "+arrowBuying+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getBuyingRate()+"</font><br>Selling: ฿"+htmlRateDataBean.getSellingRate()+" "+arrowSelling+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getSellingRate()+"</font>";
					String systemSentDate=new Date()+"";
					
					if(isTested) System.out.println("currencyString: "+currencyString);
					if(isTested) System.out.println("flagURL: "+flagURL);
					if(isTested) System.out.println("label: "+label);
					if(isTested) System.out.println("rate: "+rate);
					if(isTested) System.out.println("siaLastUpdate: "+siaLastUpdate);
					if(isTested) System.out.println("systemSentDate: "+systemSentDate);
					
					/*String body="<table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><tr><td><table border='0' width='48%' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #282828;' align='right' valign='middle'><a style='text-decoration: none; color: #ffffff;' href='#'>"
							+currencyString+"</a></td></tr></tbody></table></td></tr><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of preheader --><!-- Start of header --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center' bgcolor='#d41b29'><tbody><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --><tr><td><!-- logo --><table class='devicewidth' border='0' width='140' cellspacing='0' cellpadding='0' align='left'><tbody><tr><td align='center' width='140' height='60'><div class='imgpop'><a href='#' target='_blank'> <img style='display: block; border: none; outline: none; text-decoration: none; border-width: initial;vertical-align: middle;' src='"
							+flagURL+"' alt='' width='128' height='84' border='0' /> </a></div></td></tr></tbody></table><!-- end of logo --> <!-- start of menu --><table class='devicewidth' border='0' width='250' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 20px; color: #ffffff;' align='center' height='60'>"
							+label+"</td></tr></tbody></table><!-- end of menu --></td></tr><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of Header --><!-- Start of main-banner --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><h1 style='text-align: center;'><span style='color: #ffffff;'>"
							+rate+"</span></h1><!-- end of image --></td></tr></tbody></table></td></tr></tbody></table><!-- End of main-banner --><!-- Start of heading --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 24px; color: #ffffff; padding: 15px 0;' align='center' bgcolor='#d41b29'>"
							+siaLastUpdate+"</td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of heading --><!-- Start of Postfooter --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;<table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>"
							+ "Sent Time: "+systemSentDate+
							"</td></tr></tbody></table></td></tr><!-- Spacing --><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>Don't want to receive email Updates? <a style='text-decoration: none; color: #d41b29;' href='#'>Unsubscribe here </a></td></tr><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of postfooter -->";
					*/
					String body="<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'></head><body bgcolor='#F5F4F4'><div><table width='100%'><tbody><tr><td><table align='center' style='width:100%;max-width:700px' bgcolor='#000000'><tbody><tr><td><p style='padding:0 0 0 30'>"
							+"<font face='verdana' color='#FFFFFF'><center>Exchange Rate By KhunPin</center></font></p></td><td align='right' style='padding:0 30 0 0'>"
							+"<font face='verdana' color='000000' style='font-size:14px'><!--By KhunPin--></font></td></tr></tbody></table>"
							+"<!--Header Exchange Rate--><table align='center' style='width:100%;max-width:700px' bgcolor='#555555'><tbody><tr><td height='10'></td></tr><!--space 20px--><tr><td><table width='100%'><tbody><tr><!--<td rowspan='2' width='10%' style='vertical-align: bottom;'>"
                            +"<img style='display: inline-block;height:5%;' src='/Users/KhunPin/Desktop/Exchange-rate-down.png' /></td>--><td rowspan='2' width='60%' align='left' style='padding-left:10%'><font face='Helvetica' style='color:#000000;font-size:24px;line-height:26px;font-weight:normal;text-decoration:none'>"
                            +"<b>"+label+"</b></font><br><font face='Helvetica' style='color:#000000;font-size:36px;line-height:40px;font-weight:normal;text-decoration:none'>"
                            +"<b>"+currencyString+"</b></font><br>"
                            +imSell.getImage()+"</td>"
                            +"<!--curency name--><td align='center' width='30%'>"
                            +"<table bgcolor='#"+imSell.getColorString()+"' style='padding:0 40 0 40; margin:0 5 0 3%'><tbody><tr><td align='center'><font face='Helvetica' style='color:#000000;line-height:28px;font-weight:lighter'>"
                            +"<b>Selling</b> "+arrowSelling+"</font></td></tr><tr><td width='100%' align='center'><font face='Helvetica' style='color:#ffffff;font-size:32px;line-height:34px;font-weight:normal;text-decoration:none'>"
                            +"<b>฿"+decimalFormat.format(htmlRateDataBean.getSellingRate())+"</b></font></td></tr><tr><td align='center'><font face='Helvetica' style='color:#eeeeee;font-size:18px;line-height:20px;font-weight:lighter;text-decoration:none'>"
                            +"<b>(฿"+decimalFormat.format(oldRateDataBean.getSellingRate())+")</b></font><br><br></td></tr><tr><td align='center' width='20px'></td></tr></tbody></table></td></tr><!--Buying Price--><tr><td align='center'>"
                            +"<table bgcolor='#"+imBuy.getColorString()+"' style='padding:0 40 0 40; margin:0 5 0 3%'><tbody><tr><td align='center'><font face='Helvetica' style='color:#000000;line-height:28px;font-weight:lighter'>"
                            +"<b>Buying</b> "+arrowBuying+"</font></td></tr><tr><td width='100%' align='center'><font face='Helvetica' style='color:#ffffff;font-size:32px;line-height:34px;font-weight:normal;text-decoration:none'>"
                            +"<b>฿"+decimalFormat.format(htmlRateDataBean.getBuyingRate())+"</b></font></td></tr><tr><td align='center'><font face='Helvetica' style='color:#eeeeee;font-size:18px;line-height:20px;font-weight:lighter;text-decoration:none'>"
                            +"<b>(฿"+decimalFormat.format(oldRateDataBean.getBuyingRate())+")</b></font><br><br></td></tr><tr><td align='center' width='20px'></td></tr></tbody></table></td></tr><!--Selling Price--></tbody></table></td></tr><!--Content Exchange Rate --></tbody></table><table align='center' style='width:100%;max-width:700px' bgcolor='000000'><tbody><tr><td align='center'><font face='Helvetica' style='color:#FFFFFF;font-size:14px;line-height:16px;font-weight:normal;text-decoration:none'>"
                            +siaLastUpdate+"</font></td></tr></tbody></table></td></tr></tbody></table></div></body></html>";
							//System.out.println("body:"+body);
							if(isTested) configurationBean.setEmailList("luckymanomo@gmail.com");
							SiaSendMailTLS.sendMessage(cType+" Sia Exchange Alert",body,configurationBean.getEmailList());
					System.out.println("Sent an email at "+new Date());
				}else{
					System.out.println("No updated at "+new Date());
				}
			}else{
				System.err.println("Cannot get the last rate record from google spreadsheet!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}

	public static ImageArrowColorBean getHtmlArrowImage(double d1,double d2,int bsType){
		//String arrowYellow="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/YellowArrow.png' width='15px'>";
		//String arrowGreen="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/GreenArrow.png'>";
		//String arrowRed="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/RedArrow.png'>";
		
/*		String arrowYellowURL="https://lh3.googleusercontent.com/CSYI_rQMInftiXeJnN2FTI0jqjYS-JjD8fixlXW3JwqGDC03XBDSJMrziHDPQ8fFMovi5w3WsSFl3Ho=w2554-h1146";
		String arrowGreenURL="https://lh6.googleusercontent.com/g34bD63SokxCCQhHH9n5ggcStBUEvn4YLAd19m4bi6VLDLZdgzBzDiZezNfiDqP0RDqiWqnseJnZQKc=w2554-h1146";
		String arrowRedURL="https://lh4.googleusercontent.com/Xe_wprw43oBZwKwzBFFXyv0QWtBTI15QVDtG-d_m3cpCMPwZMUtDm4hNEhWi0aHuzEhjWz97Ggp1AFk=w2554-h1146";
		*/
		String arrowYellow="<img src='"+configurationBean.getImageURLYellow()+"' width='15px'>";
		String arrowGreen="<img src='"+configurationBean.getImageURLGreen()+"'>";
		String arrowRed="<img src='"+configurationBean.getImageURLRed()+"'>";
		
		String arrowColor="";
		String colorString="";
		String image="";
		if(d1==d2) {
			arrowColor=arrowYellow;
			colorString="CCCC00";
		}else if(d1>d2){
			arrowColor=arrowGreen;
			colorString="33BB95";
		}else {
			arrowColor=arrowRed;
			colorString="E74D41";
		}
		if(bsType==SELLING_TYPE){
			if(colorString.equals("E74D41")){
				colorString="33BB95"; //good
				image="<img style='display: inline-block;height:70%;' src='"+configurationBean.getImageGood()+"'>";
			}else if(colorString.equals("33BB95")){
				colorString="E74D41"; //bad
				image="<img style='display: inline-block;height:70%;' src='"+configurationBean.getImageBad()+"'>";
			}else{
				image="<img style='display: inline-block;height:70%;' src='"+configurationBean.getImageNeutral()+"'>";
			}
			
		}
		
		ImageArrowColorBean imageURL=new ImageArrowColorBean();
		imageURL.setArrow(arrowColor);
		imageURL.setColorString(colorString);
		imageURL.setImage(image);
		return imageURL;
	}
	public static void authenticate() throws Exception{
		try {
			ReadSpreadsheet.credential = ReadSpreadsheet.authorize();
		} catch (com.google.gdata.util.AuthenticationException e) {
			e.printStackTrace();
		}
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
	public static RateDataBean printExchangeRate(String elementId,String exchangeRate,Document document,int multiply){

		String cssSelector=document.getElementById(elementId).getElementsContainingOwnText(exchangeRate).get(0).cssSelector();
		String parentTag=cssSelector.substring(0, cssSelector.lastIndexOf(":"));
		
		String buyingRate=document.select(parentTag+":nth-child(4) > table > tbody > tr > td.show-rate").first().ownText();
		String sellingRate=document.select(parentTag+":nth-child(5) > table > tbody > tr > td.show-rate").first().ownText();
		
		try{buyingRate=decimalFormat.format(Double.parseDouble(buyingRate)*multiply);}catch(Exception e){}
		try{sellingRate=decimalFormat.format(Double.parseDouble(sellingRate)*multiply);}catch(Exception e){}
		
		return new RateDataBean(new Date(),Double.parseDouble(buyingRate),Double.parseDouble(sellingRate));
		//return ("Buying: ฿"+buyingRate+"<br>Selling: ฿"+sellingRate+"");
		
	}
	public static boolean checkIfExpired(int refreshTime){
		boolean isExpired=false;
		long expiredInMinutes=ReadSpreadsheet.credential.getExpiresInSeconds()/60;
		if(expiredInMinutes<=refreshTime){
			isExpired=true;
			try {
				ReadSpreadsheet.credential.refreshToken();
			} catch (Exception e1) {e1.printStackTrace();}
			System.out.println("Refresh token has been performed if it is less than "+refreshTime+" minutes.");
		}
		return isExpired;
	}
}
