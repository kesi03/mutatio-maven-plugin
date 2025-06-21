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
### SSH
Need to test more