### Steps

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)


    $ mvn -U versions:display-dependency-updates

    $ mvn -U versions:display-plugin-updates


3. Check that ALL issues are reported in **CHANGES.md** files


    ltag=`git describe --abbrev=0 --tags`;git log ${ltag}..master | grep "#[0-9]"


4. Update **CHANGES.md** file with authors and link to milestone.


    ltag=`git describe --abbrev=0 --tags`;git log --format='%aE' ${ltag}..master|sort -u

5. Now you can run the command: 


    ./scripts/release.sh