### Steps

1. Make sure the code is stable, bug free, etc.

2. Check maven dependencies, update if necessary, and clean also (using archiva f-ex.)

        mvn -U versions:display-dependency-updates
        mvn -U versions:display-plugin-updates

3. Run the release script:

        ./scripts/release.sh

   The script will automatically:
   - Verify you are on `develop` with a clean working directory
   - Generate a changelog summary using the Claude agent (`generate_changelog.sh`)
   - Pause for you to review and update **CHANGES.md** (issues, contributors, milestone link)
   - Create the release branch, update versions, deploy, tag, and merge back

   Use `--dry-run` to simulate the full process without pushing or deploying:

        ./scripts/release.sh --dry-run