package com.pk.eqa.jira;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Jira {

	static String user=ReadProperty.getJiraProperty("Jira.credentials.user");
	static String password=ReadProperty.getJiraProperty("Jira.credentials.password");
	static String url=ReadProperty.getJiraProperty("Jira.instance.url");
	Logger log = LoggerFactory.getLogger(Jira.class);
	
	public static void main(String args[]) throws Exception{
		//authentication();
		//updateResult();
		//getTransitionID();
		//addAttachment();
		Jira jira = new Jira();
		jira.getAuthorization();
	}
	

	
	public void getAuthorization() throws Exception{
		String request = "{\"username\": \""+user+"\",\"password\": \""+password+"\"}";
		String exception="";
    	URL Url = new URL(url+"/rest/auth/1/session");
    	HttpsURLConnection con = openConnection(Url);
    	con.setRequestProperty("Content-Type", "application/json");
    	con.setRequestProperty("Accept", "application/json");
    	con.setRequestMethod("POST");
    	con.connect();
    	try{
	    	OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
	        writer.write(request);
	        writer.close();
	        writer=null;}
    	catch(Exception e) {
    		exception=e.getMessage();
    		log.info("Response code returned="+con.getResponseCode());
    		log.error("Failed to create a Jira session: "+exception);
    	}
    	if(exception.isEmpty()){
    		log.info("Authorization was successful");
    	}
        con.disconnect();
    }
	
	public HttpsURLConnection openConnection(URL url) throws Exception{
		HttpsURLConnection con=null;
		SSLContext ctx = SSLContext.getInstance("TLS");
	    ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
	    SSLContext.setDefault(ctx);
	    try{
	    	 con = (HttpsURLConnection)url.openConnection();
	    }catch(MalformedURLException e){
	    	log.error("Failed to open https connection: "+e);
	    }
	    con.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
	    con.setRequestProperty("Authorization", "Basic " + getAuthentication("whaque","!!Nov1981"));
	    con.setConnectTimeout(15000);
    	con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(false);
        con.setDefaultUseCaches(false);
        con.setAllowUserInteraction(true);
    	
		return con;
	}
	
	public  void updateResult(String issueIDorKey, String status) throws Exception{
		String exception="";
		String transitionID=getTransitionID(issueIDorKey, status);
		String request = "{\"transition\":{\"id\":\""+transitionID+"\"}}"; 
		URL Url = new URL(url+"/rest/api/2/issue/"+issueIDorKey+"/transitions?expand=transitions.fields"); 
		HttpsURLConnection con = openConnection(Url);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("POST");
		con.connect();
		try{
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(request);
			writer.close();
		    writer=null;} 
		catch(Exception e){
			exception=e.getMessage();
			log.info("Response code returned="+con.getResponseCode());
			log.error("Failed to update the status: "+exception);
		}
		if(exception.isEmpty()){
    		log.info("Status of the issue "+issueIDorKey+" was changed to "+status+" successfully");
    	}
	    con.disconnect();
    } 

	public static String getAuthentication(String username, String password) {
	    String auth = Base64.getEncoder().encodeToString((username+":"+password).getBytes());
	    return auth;
	 	}
	
	public  String getTransitionID(String issueIDorKey, String targetStatus) throws Exception{
		
		String responseHeaders="";
		String line="";
		String transitionID="";
		URL Url = new URL(url+"/rest/api/2/issue/"+issueIDorKey+"/transitions"); 
		HttpsURLConnection con = openConnection(Url);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestMethod("GET");
		try{
			con.connect();
		    BufferedReader rd = new BufferedReader (new InputStreamReader(con.getInputStream()));
		    while ((line = rd.readLine()) != null)
		    	responseHeaders = responseHeaders + line;
		    JSONObject jsonobject = new JSONObject(responseHeaders);
		    int count = jsonobject.getJSONArray("transitions").length();
		    for(int i=0;i<count;i++){
		    	String jarray = jsonobject.getJSONArray("transitions").getJSONObject(i).get("name").toString();
		        if(jarray.toLowerCase().equals(targetStatus.toLowerCase())){
		        	transitionID=jsonobject.getJSONArray("transitions").getJSONObject(i).get("id").toString();
		        	break;
		        }
		    }
		}
		    catch(Exception e){
		    	log.info("Response code returned="+con.getResponseCode());
		    	log.error("Failed to get Trasition ID: "+e.getMessage());
		    }
	    con.disconnect();
	    return transitionID;	
	}

	public void addAttachment(String issueIDorKey, String fullFileName) throws Exception{
		String exception="";
		File file=new File(fullFileName);
		byte[] bFile = Files.readAllBytes(file.toPath());
        String contentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"";
 		String contentType = "Content-Type: application/octet-stream";	    	
		String BOUNDARY = "*****";
		String CRLF = "\r\n";		
		StringBuffer requestBody = new StringBuffer();
 		requestBody.append("--");
 		requestBody.append(BOUNDARY);
 		requestBody.append(CRLF);
 		requestBody.append(contentDisposition);
 		requestBody.append(CRLF);
 		requestBody.append(contentType);
 		requestBody.append(CRLF);
	   	requestBody.append(CRLF);
	   	requestBody.append(new String(bFile));
	    requestBody.append(CRLF);
		requestBody.append("--");
	   	requestBody.append(BOUNDARY);
		requestBody.append("--");
	
		URL Url = new URL(url+"/rest/api/2/issue/"+issueIDorKey+"/attachments");
		HttpsURLConnection con = openConnection(Url);
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept","*/*");
		con.setRequestProperty("X-Atlassian-Token", "nocheck");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		try{
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(requestBody.toString());
			wr.flush();
			wr.close();}
		catch(Exception e){
			exception=e.getMessage();
			log.info("Response code returned="+con.getResponseCode());
			log.error("Failed to upload an attachment: "+exception);
		}
		if(exception.isEmpty()){
    		log.info("Attachment - "+fullFileName+" was added to the issue "+issueIDorKey+" successfully");
    	}
		con.disconnect();
	}
}

