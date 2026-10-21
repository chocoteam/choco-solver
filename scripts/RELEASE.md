# Release procedure

A release publishes the version of `develop` (`X.Y.Z-SNAPSHOT` becomes `X.Y.Z`):

- the commit `initiate release X.Y.Z` (versions in the poms, README.md, CHANGES.md, banners, MiniZinc files)
  is merged into `master` and tagged `vX.Y.Z`;
- the artifacts are signed and published on Maven Central;
- `master` is merged back into `develop`, which moves to the next `-SNAPSHOT` version
  with an empty `NEXT MILESTONE` section in CHANGES.md.

It is done by the GitHub Action **Release** (`.github/workflows/release.yml`).
`scripts/release.sh` does the same from a local clone and remains a fallback.

## Before the release (by hand)

1. Make sure `develop` contains everything to release and its CI (**Java CI with Maven**) is green.

2. Optionally, check the dependencies and plugins:

        mvn -U versions:display-dependency-updates
        mvn -U versions:display-plugin-updates

3. Complete the `NEXT MILESTONE` section of **CHANGES.md** on `develop`, and push it:
   - the changes, by theme (`./scripts/generate_changelog.sh` drafts them from the commits since the last tag);
   - the link to the milestone: replace `milestone/xx` with the number of the milestone on GitHub;
   - the contributors to the release.

   The workflow refuses to start if one of them is missing.

## Running the release

In **Actions > Release > Run workflow**, on the `develop` branch:

1. **Dry-run** (box `dry_run` checked, the default). The `prepare` job:
   - checks that `develop` is a `-SNAPSHOT` version, that the tag does not exist, that the CI of `develop`
     succeeded and that CHANGES.md is complete;
   - builds the release commits and checks that `master` builds;
   - pushes nothing: its summary shows the commits, the changed files and the release notes, and the commits are
     available in the `release-X.Y.Z` artifact (`release.bundle`, `release-notes.md`).

   Read the summary.

2. **Release** (box `dry_run` unchecked). The `prepare` job runs again, then the `publish` job waits for
   approval in the `release` environment. Once approved, it:
   - deploys to Maven Central and waits until the artifacts are published (`autoPublish` and `waitUntil=published`
     in the pom): if the deployment fails, nothing has been pushed;
   - pushes `master`, `develop` and the tag `vX.Y.Z` at once;
   - creates the GitHub release, with the section of CHANGES.md as notes;
   - closes the milestone.

   If `master` or `develop` moved in between, the job stops: run the workflow again.

The commits and the tag are authored by the identity set in the `env` of the workflow.
Pushing the tag triggers the Docker image (`docker-image.yml`).

## After the release (by hand)

1. Check the artifacts on [Maven Central](https://central.sonatype.com/namespace/org.choco-solver)
   and the [GitHub release](https://github.com/chocoteam/choco-solver/releases/latest).
2. Create the milestone of the next version on GitHub, and put its number in the `NEXT MILESTONE` section
   of CHANGES.md on `develop`.
3. Update the website, see [docs/dev/documentation.md](../docs/dev/documentation.md#publication).
4. Announce the release.

## Configuration of the repository

Done once, in the settings of the repository. The deployment secrets are stored in the `release` environment,
not in the repository, so that they can only be read once a release is approved.

| Environment | Deployment branches | Reviewers                                  | Secrets                                                                                               |
|-------------|---------------------|--------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `release`   | `develop`           | who approves the `publish` job of a release | `RELEASE_TOKEN`, `OSS_SONATYPE_USERNAME`, `OSS_SONATYPE_PASSWORD`, `MAVEN_GPG_PRIVATE_KEY`, `MAVEN_GPG_PASSPHRASE` |

- `RELEASE_TOKEN`: fine-grained personal access token, restricted to `chocoteam/choco-solver`, with *Contents* and
  *Issues* in read and write. It pushes to `master` and `develop` (its owner must be allowed to bypass their
  protection), creates the tag and the release, and closes the milestone. Unlike `GITHUB_TOKEN`, pushing a tag with
  it triggers the workflows that listen to tags. **It expires**: renew it before its expiration date.
- `OSS_SONATYPE_USERNAME/PASSWORD`: user token of the Central Portal (namespace `org.choco-solver`).
- `MAVEN_GPG_PRIVATE_KEY/PASSPHRASE`: the signing key (`gpg --armor --export-secret-keys <key id>`) and its passphrase.

No snapshot is published: `-SNAPSHOT` versions only exist locally.

## Fallback: local release

From an up-to-date `develop`, with a clean working directory, **Maven 3.x**, the GPG key and the Central Portal
token (server `central`) in `~/.m2/settings.xml`:

    ./scripts/release.sh --dry-run          # simulates everything, pushes and deploys nothing
    ./scripts/release.sh --skip-changelog   # CHANGES.md already up to date

Without `--skip-changelog`, the script drafts the changelog with `generate_changelog.sh` and waits for CHANGES.md
to be updated. The milestone is not closed and the GitHub release is not created: do it by hand.
