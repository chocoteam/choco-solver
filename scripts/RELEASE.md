### Steps

1. Run license check:

    
    mvn license:format

2. Update **CHANGES.md** file with authors and link to milestone.


    ltag=`git describe --abbrev=0 --tags`;git log --format='%aE' ${ltag}..master|sort -u

3. Now you can run the command: 


    ./scritps/release.sh