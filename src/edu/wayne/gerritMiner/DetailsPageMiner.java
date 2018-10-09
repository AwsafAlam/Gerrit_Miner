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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class DetailsPageMiner {
	public String baseUrl;
	public String gerritUrl;
	DbConnector dbConnection;
	DbConnector detailsDb;
	public int projectId;
	public int requestId;
	public int requestCount;
	public HashMap<Integer, Reviewer> reviewerList;
	public String lastOutput;
	JsonParser parser;

	public DetailsPageMiner(int projectId, String baseUrl, String gerritUrl,
			String name) {
		this.gerritUrl = gerritUrl;
		this.baseUrl = baseUrl;
		this.projectId = projectId;

		dbConnection = new DbConnector("gerrit_" + name);
		requestId = 1;
		this.requestCount = 1;

		this.initReviewerList();
		parser = new JsonParser();

	}

	private void initReviewerList() {
		this.reviewerList = new HashMap<Integer, Reviewer>();

		PreparedStatement statement;
		try {
			statement = dbConnection.connect
					.prepareStatement("select gerrit_id,full_name,preferred_email from people ");

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				Reviewer reviewer = new Reviewer();

				reviewer.gerrit_id = rs.getInt(1);
				reviewer.fullName = rs.getString(2);
				reviewer.preferredEmail = rs.getString(3);
				this.reviewerList.put(reviewer.gerrit_id, reviewer);
			}
		} catch (Exception ex) {
		}

	}

	public void MineRequest(int id) {
		

	

		ResultSet rsCheck;
		try {
			PreparedStatement stmt = dbConnection.connect
					.prepareStatement("select * from request_detail where request_id="
							+ id);
			rsCheck = stmt.executeQuery();
			if(rsCheck.next())
				return;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		
		String query = "changes/?q=" + id + "&o=ALL_REVISIONS"
				+ "&o=DETAILED_LABELS"
				+ "&o=ALL_COMMITS&o=ALL_FILES&o=DETAILED_ACCOUNTS&"
				+ "o=MESSAGES";
		String apiOutput = GerritClient.gerritGetClient(this.baseUrl,
				this.gerritUrl, query);
		lastOutput = apiOutput;
		// System.out.println(apiOutput);
		System.out.println("Mining request#" + this.requestCount + ": "
				+ this.gerritUrl + "#/c/" + id);

		JsonArray idArray = parser.parse(apiOutput.toString()).getAsJsonArray();

		JsonObject pageObject;
		try {
			pageObject = idArray.get(0).getAsJsonObject();
		} catch (IndexOutOfBoundsException exc) {
			// TODO Auto-generated catch block
			return;
		}

		String reviewerQuery = "changes/" + id + "/reviewers";
		String reviewerOutput = GerritClient.gerritGetClient(this.baseUrl,
				this.gerritUrl, reviewerQuery);

		// parsing reviewers
		JsonArray accountsArray = parser.parse(reviewerOutput.toString())
				.getAsJsonArray();
		ArrayList<Review> reviewList = new ArrayList<Review>();

		for (Iterator<JsonElement> iter = accountsArray.iterator(); iter
				.hasNext();) {

			JsonObject detailsObj = iter.next().getAsJsonObject();
			JsonPrimitive idObj = detailsObj.getAsJsonPrimitive("_account_id");

			Reviewer reviewer = new Reviewer();
			reviewer.project_id = this.projectId;

			if (detailsObj.has("name"))
				reviewer.fullName = detailsObj.getAsJsonPrimitive("name")
						.getAsString().trim();
			reviewer.gerrit_id = idObj.getAsInt();
			String preferredEmail = "";
			if (detailsObj.has("email"))
				preferredEmail = detailsObj.getAsJsonPrimitive("email")
						.getAsString();
			reviewer.preferredEmail = preferredEmail.trim();

			String username = null;
			if (detailsObj.has("username"))
				username = detailsObj.getAsJsonPrimitive("username")
						.getAsString().trim();
			reviewer.username = username;

			String avatar = null;

			try {
				if (detailsObj.has("avatars"))
					avatar = detailsObj.getAsJsonArray("avatars").get(0)
							.getAsJsonObject().getAsJsonPrimitive("url")
							.getAsString();
			} catch (Exception ex) {

			}
			reviewer.avatar = avatar;

			addToReviewerList(reviewer);

			Review review = new Review();
			review.request_id = id;
			review.people_id = reviewer.gerrit_id;

			if (detailsObj.has("approvals")) {
				JsonObject approvalObj = detailsObj
						.getAsJsonObject("approvals");

				if (approvalObj.has("Verified"))
					review.verified = numParse(approvalObj
							.getAsJsonPrimitive("Verified"));

				if (approvalObj.has("Code-Review"))
					review.code_review = numParse(approvalObj
							.getAsJsonPrimitive("Code-Review"));

				if (approvalObj.has("Build-Status"))
					review.build = numParse(approvalObj
							.getAsJsonPrimitive("Build-Status"));

			}

			reviewList.add(review);

		}

		JsonObject currentDetailsObject = pageObject
				.getAsJsonObject("currentDetail");

		JsonObject changeDetailsObject = pageObject.getAsJsonObject("change");

		// parsing request details
		RequestDetails rDetails = new RequestDetails();
		rDetails.request_id = id;

		rDetails.change_id = pageObject.getAsJsonPrimitive("change_id")
				.getAsString();
		rDetails.gerrit_id = pageObject.getAsJsonPrimitive("id").getAsString();
		rDetails.project = pageObject.getAsJsonPrimitive("project")
				.getAsString();
		rDetails.branch = pageObject.getAsJsonPrimitive("branch").getAsString();
		if (pageObject.has("topic"))
			rDetails.topic = pageObject.getAsJsonPrimitive("topic")
					.getAsString();
		rDetails.subject = pageObject.getAsJsonPrimitive("subject")
				.getAsString();
		rDetails.status = pageObject.getAsJsonPrimitive("status").getAsString();
		if (pageObject.has("insertions"))
			rDetails.insertions = pageObject.getAsJsonPrimitive("insertions")
					.getAsInt();
		if (pageObject.has("deletions"))
			rDetails.deletions = pageObject.getAsJsonPrimitive("deletions")
					.getAsInt();
		if (pageObject.has("_sortkey"))
			rDetails.sortKey = pageObject.getAsJsonPrimitive("_sortkey")
					.getAsString();
		rDetails.createdOn = pageObject.getAsJsonPrimitive("created")
				.getAsString().substring(0, 19);
		rDetails.lastUpdatedOn = pageObject.getAsJsonPrimitive("updated")
				.getAsString().substring(0, 19);
		if (pageObject.has("mergeable"))
			rDetails.mergeable = pageObject.getAsJsonPrimitive("mergeable")
					.getAsBoolean();

		JsonObject ownerObj = pageObject.getAsJsonObject("owner");

		rDetails.owner = ownerObj.getAsJsonPrimitive("_account_id").getAsInt();

		Reviewer owner = new Reviewer();

		if (ownerObj.has("name"))
			owner.fullName = ownerObj.getAsJsonPrimitive("name").getAsString()
					.trim();
		owner.gerrit_id = rDetails.owner;
		String preferredEmail = "";
		if (ownerObj.has("email"))
			preferredEmail = ownerObj.getAsJsonPrimitive("email").getAsString();
		owner.preferredEmail = preferredEmail.trim();

		String username = null;
		if (ownerObj.has("username"))
			username = ownerObj.getAsJsonPrimitive("username").getAsString()
					.trim();
		owner.username = username;

		String avatar = null;

		try {
			if (ownerObj.has("avatars"))
				avatar = ownerObj.getAsJsonArray("avatars").get(0)
						.getAsJsonObject().getAsJsonPrimitive("url")
						.getAsString();
		} catch (Exception ex) {

		}
		owner.avatar = avatar;

		addToReviewerList(owner);

		// parsing review comments
		ArrayList<ReviewComments> rCommentsArray = new ArrayList<ReviewComments>();
		JsonArray messagesArray = pageObject.getAsJsonArray("messages");

		for (Iterator<JsonElement> iter = messagesArray.iterator(); iter
				.hasNext();) {

			try {
				JsonObject reviewCommentsObject = iter.next().getAsJsonObject();

				ReviewComments rComments = new ReviewComments();
				rComments.request_id = id;

				rComments.author = reviewCommentsObject
						.getAsJsonObject("author")
						.getAsJsonPrimitive("_account_id").getAsInt();
				rComments.created = reviewCommentsObject
						.getAsJsonPrimitive("date").getAsString()
						.substring(0, 19);
				rComments.message = reviewCommentsObject.getAsJsonPrimitive(
						"message").getAsString();
				rComments.message_id = reviewCommentsObject.getAsJsonPrimitive(
						"id").getAsString();

				try {
					rComments.patchset_id = reviewCommentsObject
							.getAsJsonPrimitive("_revision_number").getAsInt();
				} catch (NullPointerException ex) {
				}
				rCommentsArray.add(rComments);
			} catch (NullPointerException ex) {
			}
		}

		// parsing patch details
		ArrayList<Patch> patchesArray = new ArrayList<Patch>();
		ArrayList<InlineComments> inlineCommentsList = new ArrayList<InlineComments>();

		JsonObject revisionObject = pageObject.getAsJsonObject("revisions");

		for (Iterator<Map.Entry<String, JsonElement>> iterator = revisionObject
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, JsonElement> revEntry = iterator.next();
			JsonObject revObj = revEntry.getValue().getAsJsonObject();
			String revisionNumber = revEntry.getKey();
			Patch patch = new Patch();
			patch.request_id = id;
			patch.revision = revisionNumber;

			patch.patchset_number = revObj.getAsJsonPrimitive("_number")
					.getAsInt();

			if (revObj.has("commit")) {
				patch.author = revObj.getAsJsonObject("commit")
						.getAsJsonObject("author").getAsJsonPrimitive("email")
						.getAsString();
				patch.committer = revObj.getAsJsonObject("commit")
						.getAsJsonObject("committer")
						.getAsJsonPrimitive("email").getAsString();

				patch.created = revObj.getAsJsonObject("commit")
						.getAsJsonObject("author").getAsJsonPrimitive("date")
						.getAsString().substring(0, 19);
				;
				patch.commmitted = revObj.getAsJsonObject("commit")
						.getAsJsonObject("committer")
						.getAsJsonPrimitive("date").getAsString()
						.substring(0, 19);
				;

				patch.subject = revObj.getAsJsonObject("commit")
						.getAsJsonPrimitive("subject").getAsString();

				patch.message = revObj.getAsJsonObject("commit")
						.getAsJsonPrimitive("message").getAsString();
			}

			if (revObj.has("fetch")) {
				if (revObj.getAsJsonObject("fetch").has("anonymous http")) {
					if (revObj.getAsJsonObject("fetch")
							.getAsJsonObject("anonymous http").has("commands")) {
						JsonObject cmdObject = revObj.getAsJsonObject("fetch")
								.getAsJsonObject("anonymous http")
								.getAsJsonObject("commands");

						if (cmdObject.has("Checkout"))
							patch.checkout = cmdObject.getAsJsonPrimitive(
									"Checkout").getAsString();
						if (cmdObject.has("Cherry Pick"))
							patch.cherrypick = cmdObject.getAsJsonPrimitive(
									"Cherry Pick").getAsString();
						if (cmdObject.has("Format Patch"))
							patch.pull = cmdObject.getAsJsonPrimitive(
									"Format Patch").getAsString();
						if (cmdObject.has("Pull"))
							patch.pull = cmdObject.getAsJsonPrimitive("Pull")
									.getAsString();
					}
				}
			}

			if (revObj.has("files"))

			{
				JsonObject filesObj = revObj.getAsJsonObject("files");

				for (Iterator<Map.Entry<String, JsonElement>> itr = filesObj
						.entrySet().iterator(); itr.hasNext();) {
					Map.Entry<String, JsonElement> changeEntry = itr.next();
					JsonObject changeDetails = changeEntry.getValue()
							.getAsJsonObject();
					String filePath = changeEntry.getKey();
					PatchDetails pDetails = new PatchDetails();

					pDetails.file_name = filePath;
					pDetails.request_id = patch.request_id;
					pDetails.patchset_id = patch.patchset_number;

					if (changeDetails.has("status"))
						pDetails.change_type = changeDetails
								.getAsJsonPrimitive("status").getAsString();
					else
						pDetails.change_type = "M";

					if (changeDetails.has("lines_inserted"))
						pDetails.insertions = changeDetails.getAsJsonPrimitive(
								"lines_inserted").getAsInt();

					if (changeDetails.has("lines_deleted"))
						pDetails.deletions = changeDetails.getAsJsonPrimitive(
								"lines_deleted").getAsInt();

					patch.patches.add(pDetails);
				}

			}

			patchesArray.add(patch);

			if (patchesArray.size() < 128) {

				String commentsQuery = "changes/" + id + "/revisions/"
						+ patch.patchset_number + "/comments";
				String commentsOutput = GerritClient.gerritGetClient(
						this.baseUrl, this.gerritUrl, commentsQuery);

				JsonObject commentsObject = parser.parse(
						commentsOutput.toString()).getAsJsonObject();

				for (Iterator<Map.Entry<String, JsonElement>> comItr = commentsObject
						.entrySet().iterator(); comItr.hasNext();) {

					Map.Entry<String, JsonElement> commentsEntry = comItr
							.next();

					String fileName = commentsEntry.getKey();
					JsonArray commentsArray = commentsEntry.getValue()
							.getAsJsonArray();

					for (Iterator<JsonElement> comItr2 = commentsArray
							.iterator(); comItr2.hasNext();) {
						InlineComments comments = new InlineComments();
						patch.comment_count++;
						comments.file_name = fileName;
						comments.request_id = patch.request_id;
						comments.patchset_id = patch.patchset_number;

						JsonObject comObject = comItr2.next().getAsJsonObject();

						comments.comments_id = comObject.getAsJsonPrimitive(
								"id").getAsString();

						if (comObject.has("message"))
							comments.message = comObject.getAsJsonPrimitive(
									"message").getAsString();

						if (comObject.has("in_reply_to"))
							comments.in_reply_to = comObject
									.getAsJsonPrimitive("in_reply_to")
									.getAsString();

						if (comObject.has("updated"))
							comments.written_on = comObject
									.getAsJsonPrimitive("updated")
									.getAsString().substring(0, 19);

						if (comObject.has("line"))
							comments.line_number = comObject
									.getAsJsonPrimitive("line").getAsInt();

						if (comObject.has("side"))
							comments.side = comObject
									.getAsJsonPrimitive("side").getAsString();

						if (comObject.has("range")) {
							JsonObject rangeObj = comObject
									.getAsJsonObject("range");

							if (rangeObj.has("start_line"))
								comments.start_line = rangeObj
										.getAsJsonPrimitive("start_line")
										.getAsInt();

							if (rangeObj.has("end_line"))
								comments.end_line = rangeObj
										.getAsJsonPrimitive("end_line")
										.getAsInt();
							if (rangeObj.has("start_character"))
								comments.start_character = rangeObj
										.getAsJsonPrimitive("start_character")
										.getAsInt();
							if (rangeObj.has("end_character"))
								comments.end_character = rangeObj
										.getAsJsonPrimitive("end_character")
										.getAsInt();

						}

						comments.author_id = comObject
								.getAsJsonObject("author")
								.getAsJsonPrimitive("_account_id").getAsInt();
						inlineCommentsList.add(comments);

					}
				}
			}

		}

		rDetails.num_patches = patchesArray.size();
		rDetails.current_patch_id = rDetails.num_patches;
		dbConnection.saveDetailsRequest(rDetails, rCommentsArray, patchesArray,
				inlineCommentsList, reviewList);

		this.requestId = (this.requestId + 1);
		this.requestCount++;
		if ((this.requestId % 200) == 0)
			System.gc();
	}

	private void addToReviewerList(Reviewer reviewer) {
		if (this.reviewerList.get(reviewer.gerrit_id) == null) {
			this.reviewerList.put(reviewer.gerrit_id, reviewer);
			dbConnection.saveReviewer(reviewer);
		}
	}

	private short numParse(JsonPrimitive number) {
		short num;

		String numString = number.getAsString().trim();

		num = Short.parseShort(numString);
		return num;
	}

}
