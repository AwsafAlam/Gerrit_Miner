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


public class Patch {

	public int request_id;
	public int patchset_number;
	public String revision;
	public int author_id;
	public String subject;
	public String checkout;
	public String message;
	public String cherrypick;
	public String format;
	public String pull;
	public String author;
	public String committer;
	public String created;
	public String commmitted;
	public int comment_count;
	
	public ArrayList<PatchDetails> patches=new ArrayList<PatchDetails>();
}
