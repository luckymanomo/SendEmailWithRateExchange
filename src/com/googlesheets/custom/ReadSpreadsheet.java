package com.googlesheets.custom;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
 
public class ReadSpreadsheet {
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport(); 
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	/** Directory to store user credentials. */
	public static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/credentials_storage1");
	public static final String SPREADSHEET_FEED="https://spreadsheets.google.com/feeds/spreadsheets/private/full";
	public static final List<String> SCOPES = Arrays.asList("https://spreadsheets.google.com/feeds","https://docs.google.com/feeds");
     
    /*String[] SCOPESArray = {"https://spreadsheets.google.com/feeds", "https://spreadsheets.google.com/feeds/spreadsheets/private/full", "https://docs.google.com/feeds"};
    final List<String> SCOPES = Arrays.asList(SCOPESArray);*/
	public static FileDataStoreFactory dataStoreFactory;
	public static final GoogleClientSecrets googleClientSecrets=null;
    public static URL SPREADSHEET_FEED_URL;
    public static Credential credential = null;
    public static SpreadsheetService service;
    public static SpreadsheetFeed feed;
    public static SpreadsheetEntry spreadsheetSia;

    public static void main(String[] args) throws Exception{
    	try {
			credential = authorize();
			loadSheet();
		} catch (com.google.gdata.util.AuthenticationException e) {
			/*e.printStackTrace();
			System.out.println("Trying connecting");
			System.out.println(DATA_STORE_DIR.getAbsolutePath());
			new File(DATA_STORE_DIR.getAbsolutePath()+File.separator+"StoredCredential").delete();
			credential = authorize();
			loadSheet();*/
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		try {
			RateDataBean rateDataBean=retrieveLastRecord("JPY");
			System.out.println(rateDataBean);
			//insertRecord(DateFormat.getInstance().format(new Date())+"","30.33","10.50");
		} catch (IOException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
      /** Authorizes the installed application to access user's protected data. */
    public static Credential authorize() throws Exception {
    	String keyName="client_secret.json";
    	SPREADSHEET_FEED_URL = new URL(SPREADSHEET_FEED);
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,new InputStreamReader(Toolkit.getDefaultToolkit().getClass().getResourceAsStream("/"+keyName)));
        
        // Initialize the data store factory.
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        // set up authorization code flow
        //GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,SCOPES).setDataStoreFactory(dataStoreFactory).build();
        //GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,SCOPES).setAccessType("offline").setDataStoreFactory(dataStoreFactory).build();
        //GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,SCOPES).setApprovalPrompt("force").setDataStoreFactory(dataStoreFactory).build();
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,SCOPES).setDataStoreFactory(dataStoreFactory).build();
        
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user2");
     }
      
    public static void loadSheet() throws IOException, ServiceException{
    	  	service = new SpreadsheetService("SIA_DEMO");
			System.out.println("Initialize...");
			System.out.println("AccessToken:"+credential.getAccessToken());
			System.out.println("RefreshToken:"+credential.getRefreshToken());
			System.out.println("Expired Time (minutes):"+credential.getExpiresInSeconds()/60);
			
			String accessToken = credential.getAccessToken();
			
			//String accessToken = credential.getRefreshToken();
			service.setAuthSubToken(accessToken);
			credential.refreshToken();
			feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			// service.setAuthSubToken(accessToken);
			List<SpreadsheetEntry> spreadsheets = feed.getEntries();
			// Iterate through all of the spreadsheets returned
			//int i=0;
			for (SpreadsheetEntry spreadsheet : spreadsheets) {
				//System.out.println("Sheet Name: "+(++i)+". "+spreadsheet.getTitle().getPlainText());
				//Specify this sheet
				if ("SiaRateExchange".equalsIgnoreCase(spreadsheet.getTitle().getPlainText())) {
					System.out.println("SiaRateExchange is set!");
					spreadsheetSia=spreadsheet;
					/*WorksheetFeed worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
					List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
					worksheet = worksheets.get(0);*/
					
					/*URL cellFeedURL = worksheet.getCellFeedUrl();
					CellFeed cellFeed = service.getFeed(cellFeedURL, CellFeed.class);*/

					//System.out.println("Row Count:"+worksheet.getRowCount());
					
					/*for (CellEntry cell : cellFeed.getEntries()) {
						//System.out.println(cell.getTitle().getPlainText() + ":" + cell.getCell().getInputValue());
						if (cell.getTitle().getPlainText().equals("A2")) {
							cell.changeInputValueLocal("50");
							cell.update();
						}
					}*/
				}
			}
      }
      
      /**
       * This method is an alternative to {@link #readCredentialFromCommandLine}. It reads
       * client secrets from a {@code client_secrets.json} file, interactively creates
       * the necessary authorization tokens on first run, and stores the tokens in the
       * {@code FileDataStore}. Subsequent runs will no longer require interactivity
       * as long as the {@code .credentials/doubleclicksearch.json} file is not removed.
       * You can download the {@code .credentials/doubleclicksearch.json} file from the
       * Google Developers Console.
       * Note that setting the {@link GoogleAuthorizationCodeFlow} access type
       * to {@code offline} is what causes {@code GoogleAuthorizationCodeFlow} to obtain
       * and store refresh tokens.
       *//*
      private static Credential generateCredentialInteractively() throws Exception {
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        		JSON_FACTORY, new InputStreamReader(
            		ReadSpreadsheet.class.getResourceAsStream("/client_secret5.json")));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        		HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
      }*/

    
    public static RateDataBean retrieveLastRecord(String sheetName) throws IOException, ServiceException{
    	  RateDataBean rateDataBean=new RateDataBean();
    	  WorksheetEntry readWorksheetEntry=null;
    	  WorksheetFeed worksheetFeed = service.getFeed(spreadsheetSia.getWorksheetFeedUrl(), WorksheetFeed.class);
			List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
			for(WorksheetEntry eachSheet:worksheets){
				//System.out.println(eachSheet.getTitle().getPlainText());
				if(eachSheet.getTitle().getPlainText().equalsIgnoreCase(sheetName)){
					readWorksheetEntry=eachSheet;break;
				}
			}
			//worksheet = worksheets.get(0);
    	    URL listFeedUrl = readWorksheetEntry.getListFeedUrl();
    	    ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

    	    // Iterate through each row, printing its cell values.
    	    ListEntry row = listFeed.getEntries().get(readWorksheetEntry.getRowCount()-2);
    	    System.out.println("Row Count:"+readWorksheetEntry.getRowCount());
    	      // Print the first column's cell value
    	      //System.out.print(row.getTitle().getPlainText() + "\t");
    	      // Iterate over the remaining columns, and print each cell value
    	      for (String tag : row.getCustomElements().getTags()) {
    	    	//System.out.println("Tag:"+tag);
    	        //System.out.print(row.getCustomElements().getValue(tag) + "\t");
    	        String value=row.getCustomElements().getValue(tag);
    	       
    	        switch(tag){
    	        	case "datetime":try {
						rateDataBean.setDateTime(new SimpleDateFormat("M/d/yyyy HH:mm:ss").parse(value));
					} catch (ParseException e) {
						e.printStackTrace();
					}break;
    	        	case "buyingvalue":rateDataBean.setBuyingRate(Double.parseDouble(value));break;
    	        	case "sellingvalue":rateDataBean.setSellingRate(Double.parseDouble(value));break;
    	        	default:;
    	        }
    	      }
    	      //System.out.println();
    	    
    	  return rateDataBean;
      }

    
    public static void insertRecord(String sheetName,String dateTime,String buyingValue,String sellingValue) throws IOException, ServiceException{
    	// Fetch the list feed of the worksheet.
    	WorksheetEntry insertWorksheetEntry=null;
	    	WorksheetFeed worksheetFeed = service.getFeed(spreadsheetSia.getWorksheetFeedUrl(), WorksheetFeed.class);
			List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
			for(WorksheetEntry eachSheet:worksheets){
				//System.out.println(eachSheet.getTitle().getPlainText());
				if(eachSheet.getTitle().getPlainText().equalsIgnoreCase(sheetName)){
					insertWorksheetEntry=eachSheet;break;
				}
			}
		    URL listFeedUrl = insertWorksheetEntry.getListFeedUrl();
		    //ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);

		    // Create a local representation of the new row.
		    ListEntry row = new ListEntry();
		    row.getCustomElements().setValueLocal("DateTime", dateTime);
		    row.getCustomElements().setValueLocal("BuyingValue", buyingValue);
		    row.getCustomElements().setValueLocal("SellingValue", sellingValue);

		    // Send the new row to the API for insertion.
		    row = service.insert(listFeedUrl, row);
      }
     
}