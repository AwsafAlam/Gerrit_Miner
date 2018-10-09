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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonSyntaxException;

public class DetailsParser {
	private DbConnector database;
	DetailsPageMiner miner;

	public DetailsParser(int projectID, String baseUrl, String gerritPath, String name) {

		database = new DbConnector("gerrit_" + name);

		miner = new DetailsPageMiner(projectID, baseUrl, gerritPath, name);

		}

	public void startMining() {
		PreparedStatement statement;
		try {

			int error_count = 0;

			statement = database.connect.prepareStatement(
					"select request_id,gerrit_id from requests where status!='NEW' order by gerrit_id ASC");

			ResultSet rsRequest = statement.executeQuery();
			while (rsRequest.next()) {

				int request_id = rsRequest.getInt(1);
				int gerrit_id = rsRequest.getInt(2);

				try {
					miner.MineRequest(gerrit_id);
					Thread.sleep(MinerConfiguration.timeBetweenRequest);
				}

				catch (NullPointerException ex) {
					ex.printStackTrace();
					System.out.println(miner.lastOutput);
					try {
						Thread.sleep(60);
						// miner.MineRequest(gerrit_id);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					error_count++;
					if (error_count > 125)
						return;

				} catch (IllegalStateException is) {
					// retry once more
					try {
						miner.MineRequest(gerrit_id);
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// return;
				catch (JsonSyntaxException e) {
					e.printStackTrace();
					// System.out.print(miner.lastOutput.substring(0, 4));
					// if(miner.lastOutput.substring(0, 5).compareTo("found")!=0)
					miner.MineRequest(gerrit_id);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
