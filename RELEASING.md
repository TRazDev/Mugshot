# Releasing

 1. Update `VERSION_NAME` in `gradle.properties` to the release (non-SNAPSHOT) version.
 2. Update `CHANGELOG.md` for the impending release.
    1. Change the `Unreleased` header to the version, appending today's date
    2. Add a new `Unreleased` section to the top.
    3. Add a link URL at the bottom to ensure the impending release header link works.
    4. Update the `Unreleased` link URL to compare this new version...HEAD
 3. Update `README.md` with the new version.
 4. Open a pull request titled `Prepare version X.Y.Z` and merge it once it is green.

Merging is the last manual step. `tag-release.yml` sees the `Prepare version X.Y.Z`
commit land on `main`, checks that

 * `VERSION_NAME` matches the version in the commit subject,
 * the version is not a SNAPSHOT,
 * `CHANGELOG.md` has a dated section for it,
 * `README.md` mentions it,
 * and the tag does not already exist,

then tags the merge commit and calls `release.yml`, which publishes to Maven Central
and creates the GitHub release from the changelog section. If any check fails nothing
is tagged, so a mistake costs a follow-up commit rather than a burnt version number.

The deployment is staged rather than published: `SONATYPE_AUTOMATIC_RELEASE` is
`false`, so it waits in the Central Portal until you press Publish. Maven Central is
immutable, and this is the last point at which a release can still be dropped.

Afterwards, set `VERSION_NAME` to the next SNAPSHOT version and open a pull request
titled `Prepare next development version`. Every merge to `main` publishes that
snapshot to the Central snapshots repository, which keeps the publishing path
exercised between releases.

## Internal Releasing

1. Update `VERSION_NAME` in `gradle.properties` to the internal release (non-SNAPSHOT) version. [2.0.0-internal01] Ensure that the name doesn't collide with an already released version.
2. Check that the internal variables are configured correctly:
   1. `internalUrl` is set in `~/.gradle/gradle.properties` to the internal repository URL.
   2. Check `internalUsername` and `internalPassword` are set in `~/.gradle/gradle.properties` to the internal repository credentials.
3. Run `./gradlew publishMavenPublicationToInternalRepository mugshot-gradle-plugin:publishAllPublicationsToInternalRepository --no-parallel` to publish the internal release.
   * *Note* if gradle publish fails with `403` error, ensure the `VERSION_NAME` in step 1 is unique and isn't already published.
