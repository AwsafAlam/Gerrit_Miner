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
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DbConnector {
	public Connection connect;

	public DbConnector(String databasename) {

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://" + MinerConfiguration.DBHost + "/" + databasename + "?"
					+ "user=" + MinerConfiguration.DBUser + "&password=" + MinerConfiguration.DBPassword);
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		} catch (SQLException e2) {

			e2.printStackTrace();
		}

	}

	public boolean saveReviewer(Reviewer reviewer) {
		try {
			PreparedStatement statement = connect
					.prepareStatement("insert into people (gerrit_id,full_name,preferred_email,username,avatar) values(?,?,?,?,?)");
			
			statement.setInt(1, reviewer.gerrit_id);
			statement.setString(2, reviewer.fullName);
			statement.setString(3, reviewer.preferredEmail);
			statement.setString(4, reviewer.username);
			statement.setString(5, reviewer.avatar);

			statement.executeUpdate();

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public void saveReviewRequestList(ArrayList<ReviewRequest> requestList) {

		Iterator<ReviewRequest> iterator = requestList.iterator();

		while (iterator.hasNext()) {
			this.saveReviewRequest(iterator.next());
		}

	}

	public boolean saveReviewRequest(ReviewRequest request) {
		try {
			PreparedStatement statement = connect
					.prepareStatement("insert into requests "
							+ "(project_id,gerrit_id,gerrit_key,owner,owner_name,subject,status,project,branch,topic,starred,last_updated_on,sort_key,insertions,deletions,owner_email,created) values"
							+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			statement.setInt(2, request.gerrit_id);
			statement.setString(3, request.gerrit_key);
			statement.setInt(4, request.owner);
			statement.setString(5, request.owner_name);
			statement.setString(6, request.subject);
			statement.setString(7, request.status);
			statement.setString(8, request.project);
			statement.setString(9, request.branch);
			statement.setString(10, request.topic);
			statement.setBoolean(11, request.starred);
			statement.setString(12, request.lastUpdatedOn.substring(0, 19));
			statement.setString(13, request.sortKey);
			statement.setInt(14, request.insertions);
			statement.setInt(15, request.deletions);
			statement.setString(16, request.owner_email);
			statement.setString(17, request.created.substring(0, 19));
			statement.setInt(1, request.project_id);

			statement.executeUpdate();

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public void saveReviewRequestShortList(
			ArrayList<ReviewRequestShort> reviewRequestList) {
		Iterator<ReviewRequestShort> iterator = reviewRequestList.iterator();
		PreparedStatement statement;

		while (iterator.hasNext()) {
			ReviewRequestShort request = iterator.next();
			try {
				statement = connect
						.prepareStatement("insert into requests_temp "
								+ "(project_id,gerrit_id,gerrit_key,owner,sort_key) values"
								+ "(?,?,?,?,?)");

				statement.setInt(2, request.gerrit_id);
				statement.setString(3, request.gerrit_key);
				statement.setString(4, request.owner);
				statement.setString(5, request.sort_key);
				statement.setInt(1, request.project_id);

				statement.executeUpdate();
				
				statement.close();

			} catch (SQLException e) {

				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public boolean saveDetailsRequest(RequestDetails details,
			ArrayList<ReviewComments> commentsList, ArrayList<Patch> patchList,			 
			ArrayList<InlineComments> inlineCommentsList, ArrayList<Review> voteList) {


		PreparedStatement statement;

		// saving request details
		try {
			statement = connect
					.prepareStatement("INSERT INTO `request_detail` ( `request_id`,`gerrit_id`,`project`,`branch`,`topic`,"+
										"`change_id`,`subject`,`status`,	`created`,`updated`,"+
										"`insertions`,`deletions`,`sort_key`,`mergeable`,`owner`,"+
										"`number_patches`,`curent_patch_id`)"+
										" VALUES ( ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)");

			statement.setInt(1, details.request_id);
			statement.setString(2, details.change_id);
			statement.setString(3, details.project);
			statement.setString(4, details.branch);
			statement.setString(5, details.topic);
			statement.setString(6, details.change_id);
			statement.setString(7, details.subject);
			statement.setString(8, details.status);
			statement.setString(9, details.createdOn);
			statement.setString(10, details.lastUpdatedOn);
			statement.setInt(11, details.insertions);
			statement.setInt(12, details.deletions);
			statement.setString(13, details.sortKey);
			statement.setBoolean(14, details.mergeable);
			statement.setInt(15, details.owner);
			statement.setInt(16, details.num_patches);
			statement.setInt(17, details.current_patch_id);

			statement.executeUpdate();
			statement.close();

		} catch (SQLException e) {

			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}


		// saving patch details
		for (Iterator<Patch> patchIterator = patchList.iterator(); patchIterator
				.hasNext();) {
			try {
				Patch patchObj=patchIterator.next();
				statement = connect.prepareStatement("INSERT INTO `patches` ( "+
						"`request_id`,`revision`,`patchset_number`,"+
						"`comment_count`,`subject`,`message`,`checkout`,`cherrypick`,"+
						"`format`,`pull`,`author`,`committer`,`author_id`,`created`,`committed`)"+
						" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

				statement.setInt(1,patchObj.request_id);
				statement.setString(2,patchObj.revision);
				statement.setInt(3,patchObj.patchset_number);
				statement.setInt(4,patchObj.comment_count);
				statement.setString(5,patchObj.subject);
				statement.setString(6,patchObj.message);
				statement.setString(7,patchObj.checkout);
				statement.setString(8,patchObj.cherrypick);
				statement.setString(9,patchObj.format);
				statement.setString(10,patchObj.pull);
				statement.setString(11,patchObj.author);
				statement.setString(12,patchObj.committer);
				statement.setInt(13,patchObj.author_id);
				statement.setString(14,patchObj.created);
				statement.setString(15,patchObj.commmitted);

				statement.executeUpdate();
				statement.close();
				
				for (Iterator<PatchDetails> pDetailsIterator =patchObj.patches.iterator(); pDetailsIterator
						.hasNext();) {
					try {		
						PatchDetails patchDetailsObj=pDetailsIterator.next();

						statement = connect.prepareStatement("INSERT INTO `patch_details` "+
								"(`request_id`,`patchset_id`,	`file_name`,`change_type`,`insertions`,`deletions`)"+
								" VALUES (?, ?, ?, ?, ?, ?)");
						statement.setInt(1,patchDetailsObj.request_id);
						statement.setInt(2,patchDetailsObj.patchset_id);
						statement.setString(3,patchDetailsObj.file_name);
						statement.setString(4,patchDetailsObj.change_type);
						statement.setInt(5,patchDetailsObj.insertions);
						statement.setInt(6,patchDetailsObj.deletions);

						statement.executeUpdate();
						statement.close();

					} catch (SQLException e) {

						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


			} catch (SQLException e) {

				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
		}

		// saving review comments
		for (Iterator<ReviewComments> commentsIterator = commentsList
				.iterator(); commentsIterator.hasNext();) {
			try {
				ReviewComments commentsObj=commentsIterator.next();

				statement = connect.prepareStatement("INSERT INTO `review_comments` "+
						"(`request_id`,`message_id`,`patchset_id`,`author`,`created`,`message`)"+
						" VALUES (?,?,?,?,?,?)");

				statement.setInt(1,commentsObj.request_id);
				statement.setString(2,commentsObj.message_id);
				statement.setInt(3,commentsObj.patchset_id);
				statement.setInt(4,commentsObj.author);
				statement.setString(5,commentsObj.created);
				statement.setString(6,commentsObj.message);

				statement.executeUpdate();
				statement.close();

			} catch (SQLException e) {

				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// saving vote details
		for (Iterator<Review> voteIterator = voteList.iterator(); voteIterator
				.hasNext();) {
			try {
				Review voteObj=voteIterator.next();

				statement = connect.prepareStatement("INSERT INTO `reviews` "+
				"( 	`request_id`,`people_id`,`verified`,`reviewed`,`build`) "+
						"VALUES (?,?,?,?,?)");
				statement.setInt(1,voteObj.request_id);
				statement.setInt(2,voteObj.people_id);
				statement.setShort(3,voteObj.verified);
				statement.setShort(4,voteObj.code_review);
				statement.setShort(5,voteObj.build);
				
				statement.executeUpdate();
				statement.close();

			} catch (SQLException e) {

				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		//saving inline comments
		for (Iterator<InlineComments> iCommentsIterator = inlineCommentsList.iterator(); iCommentsIterator
				.hasNext();) {
			try {		
				InlineComments inlineComments=iCommentsIterator.next();

				statement = connect.prepareStatement("INSERT INTO `inline_comments` " +
						"(`comment_id`,`request_id`,`in_reply_to`,`patchset_id`,`file_name`,"+
						"`line_number`,`author_id`,`written_on`,`status`,`side`,"+
						"`message`,`start_line`,`end_line`,`start_character`,`end_character`) VALUES " +
						"(?, ?, ?, ?, ?,?, ?, ?, ?, ?,?, ?, ?, ?, ?)");
				
				statement.setString(1,inlineComments.comments_id);
				statement.setInt(2,inlineComments.request_id);
				statement.setString(3,inlineComments.in_reply_to);
				statement.setInt(4,inlineComments.patchset_id);
				statement.setString(5,inlineComments.file_name);
				statement.setInt(6,inlineComments.line_number);
				statement.setInt(7,inlineComments.author_id);
				statement.setString(8,inlineComments.written_on);
				statement.setString(9,inlineComments.status);
				statement.setString(10,inlineComments.side);
				statement.setString(11,inlineComments.message);
				statement.setInt(12,inlineComments.start_line);
				statement.setInt(13,inlineComments.end_line);
				statement.setInt(14,inlineComments.start_character);
				statement.setInt(15,inlineComments.end_character);
				
				
				statement.executeUpdate();
				statement.close();

			} catch (SQLException e) {

				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		return true;
	}

}
