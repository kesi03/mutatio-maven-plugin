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
mvn clean mutatio:update-settings -Did=github-ssh -DuserName=git -DprivateKey="/.ssh/id_ed25519" -Dpassphrase=BettnaBears1 -Daction=ADD
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