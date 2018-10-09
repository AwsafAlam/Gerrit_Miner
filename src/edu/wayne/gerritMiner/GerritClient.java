/*
 * Copyright (C) 2018 Wayne State University SEVERE Lab
 *
 * Author: Amiangshu Bosu
 *
 * Licensed under GNU LESSER GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.wayne.gerritMiner;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class GerritClient {
	protected static HttpClient httpclient; 
	

	public static String CallGerritAPI(String base_url, String gerrit_url, String method,String service)
	{
		if(httpclient==null)
			httpclient  = WebClientDevWrapper.wrapClient(new DefaultHttpClient());
		
		String apiOutput="";
		
        try {
            HttpPost httpRequest = new HttpPost(gerrit_url+"gerrit_ui/rpc/"+service);
           // System.out.println(gerrit_url+"gerrit/rpc/"+service);
            httpRequest.addHeader("Content-type", "application/json; charset=utf-8");
            httpRequest.addHeader("Accept", "application/json,application/json,application/jsonrequest;");
            httpRequest.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:16.0) Gecko/20100101 Firefox/16.0");
            httpRequest.addHeader("Host", base_url);
            httpRequest.addHeader("Pragma", "no-cache");
            httpRequest.addHeader("Referer", "http://"+base_url);
   
            httpRequest.setEntity(new StringEntity(method));
            
            
            HttpResponse response = httpclient.execute(httpRequest);
            
            HttpEntity entity =response.getEntity();
            
            InputStream instream = entity.getContent();
            String t;
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));
            StringBuffer output=new StringBuffer();
            while(true) {
                t = br.readLine();
                if(t != null) {                        
                    output.append(t);
                }
                else break;             
            }
           // System.out.println(output.toString());
            apiOutput=output.toString();
            instream.close();
            Thread.sleep(300);
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
           // httpclient.getConnectionManager().shutdown();
        }
        return apiOutput;
	}
	
	public static String gerritGetClient(String base_url,String gerrit_url,String query){
		String apiOutput="";
		
		HttpClient httpclient  = WebClientDevWrapper.wrapClient(new DefaultHttpClient());
		
		
        try {
            HttpGet httpRequest = new HttpGet(gerrit_url+query);
            
            httpRequest.addHeader("Content-type", "application/json; charset=utf-8");
            httpRequest.addHeader("Accept", "application/json,application/json,application/jsonrequest;");
            httpRequest.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:16.0) Gecko/20100101 Firefox/16.0");
            httpRequest.addHeader("Host", base_url);
            httpRequest.addHeader("Pragma", "no-cache");
            httpRequest.addHeader("Referer", "http://"+base_url);
            
            HttpResponse response = httpclient.execute(httpRequest);
            
            HttpEntity entity =response.getEntity();
            
            InputStream instream = entity.getContent();
            String t;
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));
            StringBuffer output=new StringBuffer();
            while(true) {
                t = br.readLine();
                if(t != null) {                        
                    output.append(t);
                }
                else break;             
            }
           
            apiOutput=output.toString().substring(4);
            br.close();
            
        }catch(Exception ex){}
        finally {
            
            httpclient.getConnectionManager().shutdown();
        }
    
		
		return apiOutput;
	}
	
}
