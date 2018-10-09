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

public class ReviewRequest {
	public int project_id;
	public int gerrit_id;
	public String gerrit_key;
	public int owner;
	public String owner_name="";
	public String subject;
	public String status;
	public String project;
	public String branch;
	public String topic;
	public boolean starred=false;
	public String lastUpdatedOn;
	public String created;
	public String sortKey;
	public int insertions;
	public int deletions;
	public String owner_email;
}
