## Choco3 ##

Choco3 is an open-source Java library for Constraint Programming.

This document reports the release process, the version number 3.1.0 should be adapted.

1. Make sure the code is stable, bug free, etc.

2. Check that ALL issues are reported in CHANGES.md files

3. Mount the /Volumes/choco-repo samba point (required to upload files for maven)

4. Prepare and run the release using the script
	
	$ sh release.sh request	
	$ sh release.sh perform
	$ sh release.sh zip

5. Publish choco-repo/ intranet to internet

6. Upload the zip file onto the website

===================
The Choco3 dev team.
