package com.email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
	/*public static void main(String[] args){
		//List<String> strList=collectDate("http://www.sia-moneyexchange.com/rate/",null);
		try{
			initAuthenticator();
			
			URLConnection connection=null;
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
			//printAllCssUnderId("rate",document);
			String lastUpdate=document.select("#upd").first().ownText();
			System.out.println(lastUpdate);
			//printExchangeRate("rate","JPY",document);
		}catch(Exception e){e.printStackTrace();}
	}*/
	public static void main(String[] args){
		
			URLConnection connection=null;
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
					String rate=printExchangeRate("rate","JPY",document);
					if(rate!=null && !rate.equals(rateChanged)){
						rateChanged=rate;
						SiaSendMailTLS.sendMessage(rate+"<br>"+lastUpdate+"<br>Sent system datetime: "+new Date());
						System.out.println("Send an email: "+lastUpdate+" with "+rate+" "+new Date());
					}else{
						System.out.println("No updated: "+new Date());
					}
					
					Thread.sleep(1000*60);
				}catch(Exception e){e.printStackTrace();}
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
	public static String printExchangeRate(String elementId,String exchangeRate,Document document){

		String cssSelector=document.getElementById(elementId).getElementsContainingOwnText(exchangeRate).get(0).cssSelector();
		String parentTag=cssSelector.substring(0, cssSelector.lastIndexOf(":"));
		
		String buyingRate=document.select(parentTag+":nth-child(4) > table > tbody > tr > td.show-rate").first().ownText();
		String sellingRate=document.select(parentTag+":nth-child(5) > table > tbody > tr > td.show-rate").first().ownText();
		DecimalFormat decimalFormat=new DecimalFormat("0.00");
		try{buyingRate=decimalFormat.format(Double.parseDouble(buyingRate)*100);}catch(Exception e){}
		try{sellingRate=decimalFormat.format(Double.parseDouble(sellingRate)*100);}catch(Exception e){}
		return ("["+exchangeRate+"][¥100], Buying: ฿"+buyingRate+", Selling: ฿"+sellingRate+"");
		
	}
}
