# SnowyCrescentCore


### Project Introduction

#### 1.Capability Tool

- You can register a capability with very little code. And It will auto sync to client.
- **Supports for players and other entities**

#### 2.Player Animator Api

- **The animation has compose now!!! You can dance with other player.**

- You can **register layers** on the server via JSON or Event.
- You can **register animation** on the server via JSON or Event.
- You can **register raw animation** on the client via JSON or Event.
- You can invite other player to participate in certain animations together.
- You can apply to join other player who is playing animation with ride.
- You can request a player to playing an animation.

### How to implementation?

​	**In repositories:**

```java
repositories {
	maven {
		name = "Mafuyu404 Maven"
		url = "https://maven.sighs.cc/repository/maven-public/"
    }
}
```

​	**In dependencies:**

```java
dependencies {
	implementation("io.zershyan:sccore:1.20.1-1.0.0")
}
```


