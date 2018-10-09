# A software that uses the REST based Gerrit API to mine code reviews.

Before using, please make sure the change the MinerConfiguration.java to reflect appropriate configurations. The main method is located inside the MainParser.java class. You would also need to create a MySQL database using the gerritminer.sql file. The name of the database must be 'gerrit_project' name.  For example, the database for qt should be 'gerrit_qt'

Copyright: Dr. Amiangshu Bosu, Department of Computer Science, Wayne State University
