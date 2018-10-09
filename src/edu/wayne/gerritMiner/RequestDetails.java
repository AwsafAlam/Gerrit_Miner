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

public class RequestDetails {
	public int request_id;
	public String gerrit_id;
	public String project;
	public String branch;
	public String topic;
	public int owner;
	public String change_id;
	public String subject;
	public String status;
	public String createdOn;
	public String lastUpdatedOn;
	public int insertions;
	public int deletions;
	public String sortKey;
	public boolean mergeable;
	public int num_patches;
	public int current_patch_id;


}
