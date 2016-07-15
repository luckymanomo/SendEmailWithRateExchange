package com.sia.main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
	final static String toEmails="luckymanomo@gmail.com,pin.ppr@gmail.com";
	//final static String toEmails="luckymanomo@gmail.com";
	public static URLConnection connection=null;
	//final static String toEmails="luckymanomo@gmail.com";
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
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			while(true){
				
				System.out.println("AccessToken: "+ReadSpreadsheet.credential.getAccessToken());
				System.out.println("Refresh-Token: "+ReadSpreadsheet.credential.getRefreshToken());
				long expiredInMinutes=ReadSpreadsheet.credential.getExpiresInSeconds()/60;
				System.out.println("Token will be expired in "+ReadSpreadsheet.credential.getExpiresInSeconds()/60+" minutes");
				try {
					int refreshTime=30;
					if(expiredInMinutes<=refreshTime){
						ReadSpreadsheet.credential.refreshToken();
						System.out.println("Refresh token has been performed if it is less than "+refreshTime+" minutes.");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try{retrieveAndSendEmail("JPY",100,"SIA-Japan","[JPY][&yen;100]","https://upload.wikimedia.org/wikipedia/en/thumb/9/9e/Flag_of_Japan.svg/510px-Flag_of_Japan.svg.png");}catch(Exception e){e.printStackTrace();}
					
				try{retrieveAndSendEmail("HKD",1,"Sia-Hong_Kong", "", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/Flag_of_Hong_Kong.svg/250px-Flag_of_Hong_Kong.svg.png");}catch(Exception e){e.printStackTrace();}
				
				try {Thread.sleep(1000*60);} catch (InterruptedException e) {e.printStackTrace();}
			}
			
		
	}
	private static void retrieveAndSendEmail(String cType,int multiply,String label,String currencyString, String flagURL) throws UnsupportedEncodingException, IOException {
		// TODO Auto-generated method stub
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
		RateDataBean htmlRateDataBean=printExchangeRate("rate",cType,document,multiply);
		String rate=htmlRateDataBean.getBuyingRate()+":"+htmlRateDataBean.getSellingRate();
		
		//retrieve old
		String rateChanged="";
		
		//retrieve
		RateDataBean oldRateDataBean=null;
		try{oldRateDataBean=ReadSpreadsheet.retrieveLastRecord(cType);}catch(Exception e){e.printStackTrace();}
		if(oldRateDataBean!=null) {
			rateChanged=oldRateDataBean.getBuyingRate()+":"+oldRateDataBean.getSellingRate();
			System.out.println("Last rate has been changed to "+rateChanged);
			if(rate!=null && !rate.equals(rateChanged)){
				
				try{ReadSpreadsheet.insertRecord(cType,new SimpleDateFormat("M/d/YYYY HH:mm:ss",new Locale("en", "US")).format(new Date())+"",htmlRateDataBean.getBuyingRate()+"",htmlRateDataBean.getSellingRate()+"");}catch(Exception e){}
				
				String arrowBuying=null,arrowSelling=null;
				if(oldRateDataBean!=null){
					arrowBuying=getHtmlArrowImage(htmlRateDataBean.getBuyingRate(),oldRateDataBean.getBuyingRate());
					arrowSelling=getHtmlArrowImage(htmlRateDataBean.getSellingRate(),oldRateDataBean.getSellingRate());
				}
				String pfontSize="2";
				//String flagBase64="data:image/png;base64,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";
				
				/*String flagURL="https://upload.wikimedia.org/wikipedia/en/thumb/9/9e/Flag_of_Japan.svg/510px-Flag_of_Japan.svg.png";
				String currencyString="[JPY][&yen;100]";
				String label="SIA JAPAN RATE";*/
				
				String flagImage="<a href='#' target='_blank'> <img style='display: block; border: none; outline: none; text-decoration: none; border-width: initial;vertical-align: middle;' src='"+flagURL+"' alt='' width='128' height='84' border='0' /> </a>";
				
				rate="Buying: ฿"+htmlRateDataBean.getBuyingRate()+" "+arrowBuying+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getBuyingRate()+"</font><br>Selling: ฿"+htmlRateDataBean.getSellingRate()+" "+arrowSelling+" <font size='"+pfontSize+"'>฿"+oldRateDataBean.getSellingRate()+"</font>";
				
				
				String body="<!-- Start of preheader --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><tr><td><table border='0' width='48%' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #282828;' align='right' valign='middle'><a style='text-decoration: none; color: #ffffff;' href='#'>"+currencyString+"</a></td></tr></tbody></table></td></tr><!-- Spacing --><tr><td width='100%' height='10'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of preheader --><!-- Start of header --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center' bgcolor='#d41b29'><tbody><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --><tr><td><!-- logo --><table class='devicewidth' border='0' width='140' cellspacing='0' cellpadding='0' align='left'><tbody><tr><td align='center' width='140' height='60'><div class='imgpop'>"+flagImage+"</div></td></tr></tbody></table><!-- end of logo --> <!-- start of menu --><table class='devicewidth' border='0' width='250' cellspacing='0' cellpadding='0' align='right'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 20px; color: #ffffff;' align='center' height='60'>"+label+"</td></tr></tbody></table><!-- end of menu --></td></tr><!-- Spacing --><tr><td style='font-size: 1px; line-height: 1px; mso-line-height-rule: exactly;' height='5'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of Header --><!-- Start of main-banner --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><h1 style='text-align: center;'>"
						+ "<span style='color: #ffffff;'>"+rate+"</span>"
						+ "</h1><!-- end of image --></td></tr></tbody></table></td></tr></tbody></table><!-- End of main-banner --><!-- Start of heading --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 24px; color: #ffffff; padding: 15px 0;' align='center' bgcolor='#d41b29'>"
						+lastUpdate+"</td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of heading --><!-- Start of Postfooter --><table id='backgroundTable' border='0' width='100%' cellspacing='0' cellpadding='0' bgcolor='#2a2a2a'><tbody><tr><td><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td width='100%'><table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;<table class='devicewidth' border='0' width='600' cellspacing='0' cellpadding='0' align='center'><tbody><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>"
						+ "Sent Time: "+new Date()+
						"</td></tr></tbody></table></td></tr><!-- Spacing --><tr><td style='font-family: Helvetica, arial, sans-serif; font-size: 13px; color: #ffffff;' align='center' valign='middle'>Don't want to receive email Updates? <a style='text-decoration: none; color: #d41b29;' href='#'>Unsubscribe here </a></td></tr><!-- Spacing --><tr><td width='100%' height='20'>&nbsp;</td></tr><!-- Spacing --></tbody></table></td></tr></tbody></table></td></tr></tbody></table><!-- End of postfooter -->";
				SiaSendMailTLS.sendMessage(cType+" Sia Exchange Alert",body,toEmails);
				System.out.println("Sent an email at "+new Date());
			}else{
				System.out.println("No updated at "+new Date());
			}
		}else{
			System.err.println("Cannot get the last rate record from google spreadsheet!");
			
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
/*			System.out.println("Trying connecting");
			System.out.println(ReadSpreadsheet.DATA_STORE_DIR.getAbsolutePath());
			new File(ReadSpreadsheet.DATA_STORE_DIR.getAbsolutePath()+File.separator+"StoredCredential").delete();
			ReadSpreadsheet.credential = ReadSpreadsheet.authorize();
			ReadSpreadsheet.loadSheet();*/
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
}
