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
import java.util.ArrayList;
import java.util.Iterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class GetCodeReviewList {
	
	public ArrayList<ReviewRequest> reviewRequestList;
	public String baseUrl;
	public String gerritUrl;	
	DbConnector dbConnection;
	public int projectId;
	public int REQ_COUNT=500;
	
	
	
	public GetCodeReviewList(int projectId, String baseUrl, String gerritUrl, DbConnector database) {
		this.gerritUrl = gerritUrl;
		this.baseUrl = baseUrl;
		this.projectId = projectId;

		dbConnection = database;

	}

	public void startMining(String modifiedAfterDate) {
		int parsed=0;
		int requestId=1;
		boolean hasMore=true;
		
		while(hasMore)
		{
			System.out.println("Page: "+requestId+"  Parsed: "+parsed);
			String query="changes/?q=after:"+modifiedAfterDate+"&O=81&n="+REQ_COUNT;
			

			if(parsed!=0)
				query+="&S="+parsed;
			
			hasMore=this.MineListPages(query);
			requestId++;
			parsed+=REQ_COUNT;
			//break;
		}
	}
	
	public boolean MineListPages(String query){
	    
		
		String apiOutput=GerritClient.gerritGetClient(this.baseUrl,this.gerritUrl, query);
		//System.out.println(apiOutput);
		
		JsonParser parser = new JsonParser();
        JsonArray array=parser.parse(apiOutput.toString()).getAsJsonArray();
        boolean hasMore=false;
        ReviewRequest review=null;
        this.reviewRequestList=new ArrayList<ReviewRequest>();
        
        for(Iterator<JsonElement> iter=array.iterator();iter.hasNext();)
   	   	{
   		   JsonObject reviewObj=iter.next().getAsJsonObject();
   		   review=new ReviewRequest();
   		   review.project_id=this.projectId;
   		   review.gerrit_id=reviewObj.getAsJsonPrimitive("_number").getAsInt();
   		   review.gerrit_key=reviewObj.getAsJsonPrimitive("id").getAsString();
   		   review.project=reviewObj.getAsJsonPrimitive("project").getAsString();
   		   review.branch=reviewObj.getAsJsonPrimitive("branch").getAsString();
   		   review.subject=reviewObj.getAsJsonPrimitive("subject").getAsString();
   		   review.insertions=reviewObj.getAsJsonPrimitive("insertions").getAsInt();
   		   review.deletions=reviewObj.getAsJsonPrimitive("deletions").getAsInt();
   		   JsonPrimitive topics=reviewObj.getAsJsonPrimitive("topic");
   		   if(topics!=null)
   			   review.topic=topics.getAsString();
   		   review.status=reviewObj.getAsJsonPrimitive("status").getAsString();
   		   try{
   			   review.owner_name=reviewObj.getAsJsonObject("owner").getAsJsonPrimitive("name").getAsString();
   			   review.owner=reviewObj.getAsJsonObject("owner").getAsJsonPrimitive("_account_id").getAsInt();
   			   review.owner_email=reviewObj.getAsJsonObject("owner").getAsJsonPrimitive("email").getAsString();
   		   }catch(Exception ex){}
   		   review.lastUpdatedOn=reviewObj.getAsJsonPrimitive("updated").getAsString();
   		   review.created=reviewObj.getAsJsonPrimitive("created").getAsString();
   		   //review.sortKey=reviewObj.getAsJsonPrimitive("_sortkey").getAsString();
   		   
   		   this.reviewRequestList.add(review);
   		   
   		   if(reviewObj.has("_more_changes"))
   			   hasMore=reviewObj.getAsJsonPrimitive("_more_changes").getAsBoolean();
   		   
   	    }
        
        this.dbConnection.saveReviewRequestList(reviewRequestList);
        
        if(hasMore)
        	return true;
        
        return false;
	}
}
