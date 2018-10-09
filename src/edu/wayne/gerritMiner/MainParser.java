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
import com.google.gson.JsonSyntaxException;


public class MainParser {
	private DbConnector database;
	
	public MainParser( int projectId, String baseUrl, String gerritPath, String project) {

		database = new DbConnector("gerrit_"+project);

		try {
			GetCodeReviewList miner = new GetCodeReviewList(projectId, baseUrl,
					gerritPath, database);
			miner.startMining("2012-01-01");
			
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		
		DetailsParser parser=new DetailsParser(projectId, baseUrl, gerritPath, project);
		parser.startMining();

	}
	
	public static void main(String args[]){
		MainParser parser=new MainParser(1, "gerrit.iotivity.org","https://gerrit.iotivity.org/gerrit/" , "test");
	}
	
}
