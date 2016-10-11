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

import com.googlesheets.custom.RateDataBean;
import com.googlesheets.custom.ReadSpreadsheet;
import com.sia.email.SiaSendMailTLS;

public class SiaDataCollection {
	public static Proxy proxy;
	public static int secondTimeout;
	public static final String url="http://www.sia-moneyexchange.com/rate/";
	public static String toEmails="luckymanomo@gmail.com,pin.ppr@gmail.com";
	//final static String toEmails="luckymanomo@gmail.com";
	public static URLConnection connection=null;
	//final static String toEmails="luckymanomo@gmail.com";
	public static boolean testPrintPattern=false; //testing and debugging
	public static final int BUYING_TYPE=1;
	public static final int SELLING_TYPE=2;
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
			
			//loadSheet
			try {
				authenAndLoadSheet();
				checkIfExpired(0);
				ReadSpreadsheet.loadSheet();
			} catch (Exception e1) {
				e1.printStackTrace();
				
			}
			
			while(true){
				//ReadSpreadsheet.credential.setExpiresInSeconds((long) -31030);
				System.out.println("AccessToken: "+ReadSpreadsheet.credential.getAccessToken());
				System.out.println("Refresh-Token: "+ReadSpreadsheet.credential.getRefreshToken());
				long expiredInMinutes=ReadSpreadsheet.credential.getExpiresInSeconds()/60;
				System.out.println("Token will be expired in "+expiredInMinutes+" minutes");
				
				int refreshTime=15;
				if(checkIfExpired(refreshTime)) ReadSpreadsheet.loadSheet(); //testing after exceeding interval time. (assumed in 1 minutes)
				
				retrieveAndSendEmail("JPY",100,"SIA-Japan","[JPY][&yen;100]","https://upload.wikimedia.org/wikipedia/en/thumb/9/9e/Flag_of_Japan.svg/510px-Flag_of_Japan.svg.png");	
				//retrieveAndSendEmail("HKD",1,"Sia-HongKong", "", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/Flag_of_Hong_Kong.svg/250px-Flag_of_Hong_Kong.svg.png");
				
				try {Thread.sleep(1000*60);} catch (InterruptedException e) {e.printStackTrace();}
			}
			
		
	}
	private static void retrieveAndSendEmail(String cType,int multiply,String label,String currencyString, String flagURL) {
		// TODO Auto-generated method stub
		//initAuthenticator();
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
			
			
			//retrieve
			RateDataBean oldRateDataBean=null;
			oldRateDataBean=ReadSpreadsheet.retrieveLastRecord(cType);
			if(oldRateDataBean!=null) {
				rateChanged=oldRateDataBean.getBuyingRate()+":"+oldRateDataBean.getSellingRate();
				System.out.println("Last rate is "+rateChanged);
				if(testPrintPattern || (rate!=null && !rate.equals(rateChanged))){
					
					try{ReadSpreadsheet.insertRecord(cType,new SimpleDateFormat("M/d/YYYY HH:mm:ss",new Locale("en", "US")).format(new Date())+"",htmlRateDataBean.getBuyingRate()+"",htmlRateDataBean.getSellingRate()+"");}catch(Exception e){}
					
					String arrowBuying=null,arrowSelling=null;
					ImageURL imBuy = null,imSell = null;
					if(oldRateDataBean!=null){
						imBuy=getHtmlArrowImage(htmlRateDataBean.getBuyingRate(),oldRateDataBean.getBuyingRate(),BUYING_TYPE);
						imSell=getHtmlArrowImage(htmlRateDataBean.getSellingRate(),oldRateDataBean.getSellingRate(),SELLING_TYPE);
						arrowBuying=imBuy.getArrow();
						arrowSelling=imSell.getArrow();
					}
					String pfontSize="2";
					//String flagBase64="data:image/png;base64,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";
					
					/*String flagURL="https://upload.wikimedia.org/wikipedia/en/thumb/9/9e/Flag_of_Japan.svg/510px-Flag_of_Japan.svg.png";
					String currencyString="[JPY][&yen;100]";
					String label="SIA JAPAN RATE";*/
					
					rate="Buying: ฿"+htmlRateDataBean.getBuyingRate()+" "+arrowBuying+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getBuyingRate()+"</font><br>Selling: ฿"+htmlRateDataBean.getSellingRate()+" "+arrowSelling+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getSellingRate()+"</font>";
					String systemSentDate=new Date()+"";
					
					if(testPrintPattern) System.out.println("currencyString: "+currencyString);
					if(testPrintPattern) System.out.println("flagURL: "+flagURL);
					if(testPrintPattern) System.out.println("label: "+label);
					if(testPrintPattern) System.out.println("rate: "+rate);
					if(testPrintPattern) System.out.println("siaLastUpdate: "+siaLastUpdate);
					if(testPrintPattern) System.out.println("systemSentDate: "+systemSentDate);
					
					
					/*String body="<table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><tr><td><table border='0' width='48%' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #282828;' align='right' valign='middle'><a style='text-decoration: none; color: #ffffff;' href='#'>"
							+currencyString+"</a></td></tr></tbody></table></td></tr><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of preheader --><!-- Start of header --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center' bgcolor='#d41b29'><tbody><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --><tr><td><!-- logo --><table class='devicewidth' border='0' width='140' cellspacing='0' cellpadding='0' align='left'><tbody><tr><td align='center' width='140' height='60'><div class='imgpop'><a href='#' target='_blank'> <img style='display: block; border: none; outline: none; text-decoration: none; border-width: initial;vertical-align: middle;' src='"
							+flagURL+"' alt='' width='128' height='84' border='0' /> </a></div></td></tr></tbody></table><!-- end of logo --> <!-- start of menu --><table class='devicewidth' border='0' width='250' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 20px; color: #ffffff;' align='center' height='60'>"
							+label+"</td></tr></tbody></table><!-- end of menu --></td></tr><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of Header --><!-- Start of main-banner --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><h1 style='text-align: center;'><span style='color: #ffffff;'>"
							+rate+"</span></h1><!-- end of image --></td></tr></tbody></table></td></tr></tbody></table><!-- End of main-banner --><!-- Start of heading --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 24px; color: #ffffff; padding: 15px 0;' align='center' bgcolor='#d41b29'>"
							+siaLastUpdate+"</td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of heading --><!-- Start of Postfooter --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;<table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>"
							+ "Sent Time: "+systemSentDate+
							"</td></tr></tbody></table></td></tr><!-- Spacing --><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>Don't want to receive email Updates? <a style='text-decoration: none; color: #d41b29;' href='#'>Unsubscribe here </a></td></tr><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of postfooter -->";
					*/
					String body="<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'></head><body bgcolor='#F5F4F4'><div><table width='100%'><tbody><tr><td><table align='center' style='width:100%;max-width:700px' bgcolor='FFFFFF'><tbody><tr><td height='13' border='1'></td></tr><tr><td><p style='padding:0 0 0 30'>"
							+"<font face='verdana' color='000000'>Exchange Rate By KhunPin</font></p></td><td align='right' style='padding:0 30 0 0'>"
							+"<font face='verdana' color='000000' style='font-size:14px'><!--By KhunPin--></font></td></tr><tr><td height='13'></td></tr></tbody></table>"
							+"<!--Header Exchange Rate--><table align='center' style='width:100%;max-width:700px' bgcolor='FFFFFF'><tbody><tr><td height='20'></td></tr><!--space 20px--><tr><td><table width='100%'><tbody><tr><!--<td rowspan='2' width='10%' style='vertical-align: bottom;'>"
                            +"<img style='display: inline-block;height:5%;' src='/Users/KhunPin/Desktop/Exchange-rate-down.png' /></td>--><td rowspan='2' width='60%' align='left' style='padding-left:10%'><font face='Helvetica' style='color:#000000;font-size:24px;line-height:26px;font-weight:normal;text-decoration:none'>"
                            +"<b>"+label+"</b></font><br><font face='Helvetica' style='color:#000000;font-size:36px;line-height:40px;font-weight:normal;text-decoration:none'>"
                            +"<b>"+currencyString+"</b></font><br>"
                            +imSell.getImage()+"</td>"
                            +"<!--curency name--><td align='center' width='30%'>"
                            +"<table bgcolor='#"+imBuy.getColorString()+"' style='padding:0 40 0 40; margin:5 0 5 3%'><tbody><tr><td align='center'><font face='Helvetica' style='color:#000000;line-height:28px;font-weight:lighter'><b>Buying</b></font></td></tr><tr><td width='100%' align='center'><font face='Helvetica' style='color:#ffffff;font-size:32px;line-height:34px;font-weight:normal;text-decoration:none'>"
                            +"<b>฿"+htmlRateDataBean.getBuyingRate()+" "+arrowBuying+"</b></font></td></tr><tr><td align='center'><font face='Helvetica' style='color:#eeeeee;font-size:18px;line-height:20px;font-weight:lighter;text-decoration:none'>"
                            +"<b>(฿"+oldRateDataBean.getBuyingRate()+")</b></font></td></tr><tr><td align='center' width='20px'></td></tr></tbody></table></td></tr><!--Buying Price--><tr><td align='center'>"
                            +"<table bgcolor='#"+imSell.getColorString()+"' style='padding:0 40 0 40; margin:5 0 5 3%'><tbody><tr><td align='center'><font face='Helvetica' style='color:#000000;line-height:28px;font-weight:lighter'><b>Selling</b></font></td></tr><tr><td width='100%' align='center'><font face='Helvetica' style='color:#ffffff;font-size:32px;line-height:34px;font-weight:normal;text-decoration:none'>"
                            +"<b>฿"+htmlRateDataBean.getSellingRate()+" "+arrowSelling+"</b></font></td></tr><tr><td align='center'><font face='Helvetica' style='color:#eeeeee;font-size:18px;line-height:20px;font-weight:lighter;text-decoration:none'>"
                            +"<b>(฿"+oldRateDataBean.getSellingRate()+")</b></font></td></tr><tr><td align='center' width='20px'></td></tr></tbody></table></td></tr><!--Selling Price--></tbody></table></td></tr><!--Content Exchange Rate --></tbody></table><table align='center' style='width:100%;max-width:700px' bgcolor='000000'><tbody><tr><td align='center'><font face='Helvetica' style='color:#FFFFFF;font-size:14px;line-height:16px;font-weight:normal;text-decoration:none'>"
                            +siaLastUpdate+"</font></td></tr></tbody></table></td></tr></tbody></table></div></body></html>";
							if(testPrintPattern) toEmails="luckymanomo@gmail.com";
							SiaSendMailTLS.sendMessage(cType+" Sia Exchange Alert",body,toEmails);
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
	public static ImageURL getHtmlArrowImage(double d1,double d2,int bsType){
		//String arrowYellow="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/YellowArrow.png' width='15px'>";
		//String arrowGreen="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/GreenArrow.png'>";
		//String arrowRed="<img src='http://www.wealthmagik.com/App_Themes/FundInfo/images/RedArrow.png'>";
		String arrowYellow="<img src='https://lh3.googleusercontent.com/CSYI_rQMInftiXeJnN2FTI0jqjYS-JjD8fixlXW3JwqGDC03XBDSJMrziHDPQ8fFMovi5w3WsSFl3Ho=w2554-h1146' width='15px'>";
		String arrowGreen="<img src='https://lh6.googleusercontent.com/g34bD63SokxCCQhHH9n5ggcStBUEvn4YLAd19m4bi6VLDLZdgzBzDiZezNfiDqP0RDqiWqnseJnZQKc=w2554-h1146'>";
		String arrowRed="<img src='https://lh4.googleusercontent.com/Xe_wprw43oBZwKwzBFFXyv0QWtBTI15QVDtG-d_m3cpCMPwZMUtDm4hNEhWi0aHuzEhjWz97Ggp1AFk=w2554-h1146'>";
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
				colorString="33BB95";
				image="<img style='display: inline-block;height:100%;'  src='https://lh6.googleusercontent.com/HOov4GBM3UmnA5ogo6ZDOROT_soe9Ne2SdFrJUjB9TMBMac6iozYyw-9DSu-lvDsO6exFuyPgECNJPs=w2554-h1146'>";
			}else if(colorString.equals("33BB95")){
				colorString="E74D41";
				image="<img style='display: inline-block;height:100%;' src='https://lh4.googleusercontent.com/Mpg9cyM244kgde99xgc9e3n2vLMjXMTdtYIFaGRH3GEP7uuZPf42jaP8rprgO94rqDtHJ7_Ibil3V8A=w2554-h1146'>";
			}
			
		}
		
		ImageURL imageURL=new ImageURL();
		imageURL.setArrow(arrowColor);
		imageURL.setColorString(colorString);
		imageURL.setImage(image);
		return imageURL;
	}
	static class ImageURL{
		String arrow;
		String image;
		String colorString;
		public String getArrow() {
			return arrow;
		}
		public void setArrow(String arrow) {
			this.arrow = arrow;
		}
		public String getImage() {
			return image;
		}
		public void setImage(String image) {
			this.image = image;
		}
		public String getColorString() {
			return colorString;
		}
		public void setColorString(String colorString) {
			this.colorString = colorString;
		}
	}
	public static void authenAndLoadSheet() throws Exception{
		try {
			ReadSpreadsheet.credential = ReadSpreadsheet.authorize();
			//ReadSpreadsheet.loadSheet();
		} catch (com.google.gdata.util.AuthenticationException e) {
			e.printStackTrace();
			/*try {
				ReadSpreadsheet.credential.refreshToken();
				System.out.println("try to refresh token...");
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
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
		DecimalFormat decimalFormat=new DecimalFormat("0.00");
		try{buyingRate=decimalFormat.format(Double.parseDouble(buyingRate)*multiply);}catch(Exception e){}
		try{sellingRate=decimalFormat.format(Double.parseDouble(sellingRate)*multiply);}catch(Exception e){}
		//return ("["+exchangeRate+"][¥100], Buying: ฿"+buyingRate+", Selling: ฿"+sellingRate+"");
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
