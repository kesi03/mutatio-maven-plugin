# Mutatio
## Credentials
### Token
Using a token
Tested only with a github classic token.
https://github.com/settings/tokens

In .m2 settings:
```xml
<server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
</server>
``` 
In your poms properties:
```xml
<properties>
<gitProvider>github</gitProvider>
</properties>
```
Add a scm:
```xml
 <scm>
    <connection>scm:git:https://github.com/kesi03/mutatio-maven.git</connection>
    <developerConnection>scm:git:git@github.com:kesi03/mutatio-maven.git</developerConnection>
    <url>https://github.com/kesi03/mutatio-maven</url>
  </scm>
```
---
## SSH
***STILL UNDER DEVELOPMENT***
## Setting up SSH credentials for GitHub

---

###  1. **Check for Existing SSH Keys**

Open a terminal and run:

```bash
ls -al ~/.ssh
```

If you see files like `id_rsa` and `id_rsa.pub`, you already have a key pair. If not, move to the next step.

---

### 2.  **Create .ssh securely if not present**
```bash
mkdir -p ~/.ssh
chmod 700 ~/.ssh
```
- -p makes sure the command doesn't complain if .ssh already exists.
- chmod 700 locks down the directory—only your user can access it.

### 3. **Generate a New SSH Key**

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```

- Press Enter to accept the default file location.
- Optionally, add a passphrase for extra security.

> If your system doesn’t support `ed25519`, use `rsa` instead:
```bash
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

---

### 4. **Start the SSH Agent and Add Your Key**

```bash
# Start the agent
eval "$(ssh-agent -s)"

# Add your private key
ssh-add ~/.ssh/id_ed25519
```
---
### 5. **Add the Public Key to GitHub**

Copy your public key:

```bash
cat ~/.ssh/id_ed25519.pub
```

Then:

1. Log in to GitHub.
2. Go to **Settings > SSH and GPG keys**.
3. Click **New SSH key**, give it a name, and paste the key.
---
### 6. **Update settings**
mutatio:update-settings
````bash
mvn clean mutatio:update-settings -Did=github-ssh -DuserName=git -DprivateKey="/.ssh/id_ed25519" -Dpassphrase=FindMyKey -Daction=ADD
````
---
### 7. **Add a scm**
```xml
<scm>
    <connection>scm:git:git@github.com:kesi03/mutatio-maven.git</connection>
    <developerConnection>scm:git:git@github.com:kesi03/mutatio-maven.git</developerConnection>
    <url>https://github.com/kesi03/mutatio-maven</url>
</scm>
```

# Mojos
## Branch
### Branch Start
**description**

Create a new branch from development.

🖥️ **Execute**
```bash 
mvn clean mutatio:start-branch -DbranchType=FEATUTE -DrepoIdentity="JIRA-123455"
```
#### ⚙️ Plugin Parameters
- **`branchType`**  
  *Type of branch*  
  
  **Property:** `branchType`
  
  _.e.g._ `${branchType}/${repoIdentiy}`

|Branch Type|Description                                                                            |Example                    |
|-----------|---------------------------------------------------------------------------------------|---------------------------|
|ARCHIVE    |Archived or deprecated branches no longer in active development.                       |archive/${repoIdentity}    |
|BUGFIX     |A branch used for fixing bugs, distinct from general fixes.                            |bugfix/${repoIdentity}     |
|BUILD      |Branches related to build process changes or fixes.                                    |build/${repoIdentity}      |
|CHORE      |Branches for chores like dependency updates, formatting, or maintenance.               |chore/${repoIdentity}      |
|CI         |Branches related to continuous integration configuration.                              |ci/${repoIdentity}         |
|CODE       |Branches used for general code organization or utilities.                              |code/${repoIdentity}       |
|DOCS       |Branches containing documentation-related updates.                                     |docs/${repoIdentity}       |
|EXPERIMENT |Branches for experimental or spike code that may be temporary.                         |experiment/${repoIdentity} |
|FEATURE    |Branches implementing new features.                                                    |feat/${repoIdentity}       |
|FIX        |General bug fixes not classified under bugfix or hotfix.                               |fix/${repoIdentity}        |
|HOTFIX     |Urgent production fixes made directly on a release branch.                             |hotfix/${repoIdentity}     |
|IMPROVEMENT|Branches for general improvements or minor enhancements.                               |improvement/${repoIdentity}|
|PERF       |Branches targeting performance enhancements.                                           |perf/${repoIdentity}       |
|PROTOTYPE  |Prototype branches for proofs of concept or throwaway work.                            |prototype/${repoIdentity}  |
|REFACTOR   |Branches dedicated to refactoring existing code without adding features or fixing bugs.|refactor/${repoIdentity}   |
|SANDBOX    |Safe space branches used for individual experimentation or testing.                    |sandbox/${repoIdentity}    |
|STAGING    |Intermediate branches used for staging pre-production environments.                    |staging/${repoIdentity}    |
|STYLE      |Branches related to code styling, formatting, or aesthetic cleanup.                    |style/${repoIdentity}      |
|TEST       |Branches containing test cases or updates to test frameworks.                          |test/${repoIdentity}       |

- **`repoIdentity`**  
  *The identity of the repository used to determine the branch to start.*  
  This is typically the name of the repository or a unique identifier.  
  **Property:** `repoIdentity`
  
  _.e.g._ `${branchType}/${repoIdentiy}`

---
#### Branch End
Merges a branch into development
```bash
mvn clean mutatio:branch-end -DbranchType=FEATUTE -DrepoIdentity="JIRA-123455"
```
#### ⚙️ Plugin Parameters
- **`branchType`**  
  *Type of branch*  
  
  **Property:** `branchType`
  
  _.e.g._ `${branchType}/${repoIdentiy}`

|Branch Type|Description                                                                            |Example                    |
|-----------|---------------------------------------------------------------------------------------|---------------------------|
|ARCHIVE    |Archived or deprecated branches no longer in active development.                       |archive/${repoIdentity}    |
|BUGFIX     |A branch used for fixing bugs, distinct from general fixes.                            |bugfix/${repoIdentity}     |
|BUILD      |Branches related to build process changes or fixes.                                    |build/${repoIdentity}      |
|CHORE      |Branches for chores like dependency updates, formatting, or maintenance.               |chore/${repoIdentity}      |
|CI         |Branches related to continuous integration configuration.                              |ci/${repoIdentity}         |
|CODE       |Branches used for general code organization or utilities.                              |code/${repoIdentity}       |
|DOCS       |Branches containing documentation-related updates.                                     |docs/${repoIdentity}       |
|EXPERIMENT |Branches for experimental or spike code that may be temporary.                         |experiment/${repoIdentity} |
|FEATURE    |Branches implementing new features.                                                    |feat/${repoIdentity}       |
|FIX        |General bug fixes not classified under bugfix or hotfix.                               |fix/${repoIdentity}        |
|HOTFIX     |Urgent production fixes made directly on a release branch.                             |hotfix/${repoIdentity}     |
|IMPROVEMENT|Branches for general improvements or minor enhancements.                               |improvement/${repoIdentity}|
|PERF       |Branches targeting performance enhancements.                                           |perf/${repoIdentity}       |
|PROTOTYPE  |Prototype branches for proofs of concept or throwaway work.                            |prototype/${repoIdentity}  |
|REFACTOR   |Branches dedicated to refactoring existing code without adding features or fixing bugs.|refactor/${repoIdentity}   |
|SANDBOX    |Safe space branches used for individual experimentation or testing.                    |sandbox/${repoIdentity}    |
|STAGING    |Intermediate branches used for staging pre-production environments.                    |staging/${repoIdentity}    |
|STYLE      |Branches related to code styling, formatting, or aesthetic cleanup.                    |style/${repoIdentity}      |
|TEST       |Branches containing test cases or updates to test frameworks.                          |test/${repoIdentity}       |

- **`repoIdentity`**  
  *The identity of the repository used to determine the branch to start.*  
  This is typically the name of the repository or a unique identifier.  
  **Property:** `repoIdentity`
  
  _.e.g._ `${branchType}/${repoIdentiy}`

---
### Release
#### 🧩 `ReleaseStart`

**Description:**  
This Mojo is used to start the release branch.  
It is typically called at the beginning of the build process to initialize the release branch.

🖥️  **Execute**
```bash
mvn clean mutatio:release-start -DreleaseType=MINOR
```

---

### ⚙️ Parameters

| Parameter           | Description                                                                                      | Property             | Default     | Required | Readonly |
|---------------------|--------------------------------------------------------------------------------------------------|----------------------|-------------|----------|----------|
| `project`           | The Maven project being built. Used to access project properties and configuration.             | `${project}`         | —           | —        | ✅        |
| `settings`          | The settings for the Maven build, including repository configurations from `settings.xml`.      | `${settings}`        | —           | —        | ✅        |
| `repoIdentity`      | The identity of the repository used to determine the branch to start. Typically a unique name.   | `repoIdentity`       | —           | —        | —        |
| `releaseType`       | The type of release to be started. Typically `PATCH`, `MINOR`, or `MAJOR`.                      | `releaseType`        | `PATCH`     | ✅        | —        |
| `versionIdentifier` | The version identifier to be used for the release. Typically `SNAPSHOT` or a specific version.   | `versionIdentifier`  | `SNAPSHOT`  | ❌        | —        |
| `pushChanges`       | Flag to determine whether to push changes to the remote repository after starting the branch.    | `pushChanges`        | `true`      | —        | —        |

----
#### 🧩 `ReleaseEnd`
**Description:**  
This Mojo is used to end the release branch.  
It is typically called at the end of the build process to finalize the release branch.

🖥️  **execute**
```bash
mvn clean mutatio:release-end -Drelease=1.0.10
```

---

### ⚙️ Parameters

| Parameter       | Description                                                                                      | Property         | Default   | Required | Readonly |
|----------------|--------------------------------------------------------------------------------------------------|------------------|-----------|----------|----------|
| `project`       | The Maven project being built. Used to access project properties and configuration.             | `${project}`     | —         | —        | ✅        |
| `settings`      | The settings for the Maven build, including repository configurations from `settings.xml`.      | `${settings}`    | —         | —        | ✅        |
| `repoIdentity`  | The identity of the repository used to determine the branch to end. Typically a unique name.     | `repoIdentity`   | —         | —        | —        |
| `release`       | The release version to be used when ending the release branch. Typically a version number.       | `release`        | —         | —        | —        |
| `mainOrMaster`  | The type of branch to be used as the main or master branch after the release.                   | `mainOrMaster`   | `MASTER`  | —        | —        |
| `pushChanges`   | Flag to determine whether to push changes to the remote repository after archiving.             | `pushChanges`    | `true`    | —        | —        |

---

### 🧩 `ReleaseNotesMojo`

**Description:**  
This mojo is used to create release notes from 2 release tags
It can generate notes based on branch type, categorized changes, or standard release notes.

🖥️  **execute**
```bash
mvn clean mutatio:release-notes -Drelease=1.0.10
```

---

### ⚙️ Parameters

| Parameter     | Description                                                                                      | Property     | Default | Required | Readonly |
|---------------|--------------------------------------------------------------------------------------------------|--------------|---------|----------|----------|
| `project`     | The Maven project being built. Used to access project properties and configuration.             | `${project}` | —       | —        | ✅        |
| `settings`    | The settings for the Maven build, including repository configurations from `settings.xml`.      | `${settings}`| —       | —        | ✅        |
| `notesType`   | Used to choose which kind of notes you want. See: `ReleaseNotesType`.                           | `notesType`  | —       | —        | —        |
| `release`     | Used to determine which release tag you wish to create notes for.                               | `release`    | —       | —        | —        |

---

## 🧩 `ReleaseNotesType`

**Description:**  
Used to determine which kind of release notes are wanted.

### Available values

| Type      | Description                                                                 |
|-----------|-----------------------------------------------------------------------------|
| `STANDARD` | Standard release notes sorted by latest first.                             |
| `CATEGORY` | Release notes sorted by latest first and by category.                      |
| `BRANCH`   | Release notes sorted by latest first and by `BranchType`.   |

---

## Dependencies

### 🧩 `CollateArtifactsMojo`

**Description:**  
This Mojo is used to report which artifacts are created when a release branch is created.

🖥️  **execute**
```bash
mvn clean mutatio:collate-artifacts -Drelease=1.0.10
```

---

#### ⚙️ Parameters

| Parameter         | Description                                                                                      | Property         | Default   | Required | Readonly |
|-------------------|--------------------------------------------------------------------------------------------------|------------------|-----------|----------|----------|
| `currentProject`  | The Maven project being built. Used to access project properties and configuration.             | `${project}`     | —         | —        | ✅        |
| `session`         | The Maven session. See: `MavenSession` | `${session}`     | —         | —        | ✅        |
| `projectBuilder`  | The Maven project builder. See: `ProjectBuilder` | `${projectBuilder}` | —     | —        | ✅        |
| `settings`        | The settings for the Maven build, including repository configurations from `settings.xml`.      | `${settings}`    | —         | —        | ✅        |
| `repoIdentity`    | The identity of the repository used to determine the branch to start. Typically a unique name.   | `repoIdentity`   | —         | —        | —        |
| `release`         | The release version to be used when ending the release branch. Typically a version number.       | `release`        | —         | —        | —        |
| `mainOrMaster`    | The type of branch to be used as the main or master branch after the release.                   | `mainOrMaster`   | `MASTER`  | —        | —        |

---

---

### 🧩 `UpdateDependenciesMojo`

**Description:**  
This Mojo is used to update dependencies in the project-based artifact identifiers.  
It is typically called to ensure that the project uses the latest versions of its dependencies.

🖥️  **execute**
```bash
mvn clean mutatio:update-dependencies -Drelease=1.0.10
```

---

#### ⚙️ Parameters

| Parameter         | Description                                                                                      | Property         | Default   | Required | Readonly |
|-------------------|--------------------------------------------------------------------------------------------------|------------------|-----------|----------|----------|
| `currentProject`  | The Maven project being built. Used to access project properties and configuration.             | `${project}`     | —         | —        | ✅        |
| `session`         | The Maven session. See: [`MavenSession`](https://maven.apache.org/ref/current/maven-core/apidocs/org/apache/maven/execution/MavenSession.html) | `${session}`     | —         | —        | ✅        |
| `projectBuilder`  | The Maven project builder. See: `ProjectBuilder` | `${projectBuilder}` | —     | —        | ✅        |
| `settings`        | The settings for the Maven build, including repository configurations from `settings.xml`.      | `${settings}`    | —         | —        | ✅        |
| `repoIdentity`    | The identity of the repository used to determine the branch to start. Typically a unique name.   | `repoIdentity`   | —         | —        | —        |
| `release`         | The release version to be used when ending the release branch. Typically a version number.       | `release`        | —         | —        | —        |
| `mainOrMaster`    | The type of branch to be used as the main or master branch after the release.                   | `mainOrMaster`   | `MASTER`  | —        | —        |
| `artifacts`       | The artifacts to be updated. This is a comma-separated list of artifact identifiers.             | `artifacts`      | —         | ✅        | —        |

---
## Settings
### 🧩 `UpdateSettingsMojo`

**Description:**  
This Mojo is used to update the Maven `settings.xml` file with server credentials.  
It can add, update, or remove server entries based on the provided parameters.

🖥️  **execute**
```bash
mvn clean mutatio:update-settings -Did=github-ssh -DuserName=git -DprivateKey="/.ssh/id_ed25519" -Dpassphrase=FindMyKey -Daction=ADD
```


---

#### ⚙️ Parameters

| Parameter     | Description                                                                                          | Property     | Default | Required | Readonly |
|---------------|------------------------------------------------------------------------------------------------------|--------------|---------|----------|----------|
| `project`     | The Maven project being built. Used to access project properties and configuration.                 | `${project}` | —       | ✅        | ✅        |
| `settings`    | The settings for the Maven build, including repository configurations from `settings.xml`.          | `${settings}`| —       | ✅        | ✅        |
| `id`          | The ID of the server to be added, updated, or removed. Typically the name defined in `settings.xml`. | `id`         | —       | —        | —        |
| `password`    | The password for the server. Used for authentication.                                                | `password`   | —       | —        | —        |
| `privateKey`  | The private key used for SSH authentication.                                                         | `privateKey` | —       | —        | —        |
| `passphrase`  | The SSH key passphrase used for authentication.                                                     | `passphrase` | —       | —        | —        |
| `userName`    | The username for the server. Used for authentication.                                                | `userName`   | —       | —        | —        |
| `action`      | The action to perform on the server entry: `READ`, `ADD`, `UPDATE`, or `REMOVE`.                    | `action`     | `READ`  | —        | —        |

---