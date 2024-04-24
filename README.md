# Live Football World Cup Score Board

Simple score board library for in-memory tracking of ongoing matches and their results.
For each match, we are tracking names of participating teams, time when match was registered on board and current score.

Score board is thread-safe and the data is kept
in [ConcurrentHashMap](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html).
It supports full concurrency of retrievals and high expected concurrency for updates. However, you should keep in mind,
that even though all operations are thread-safe, retrieval operations do not entail locking, and there is not any
support for locking the entire score board in a way that prevents all access.
Since retrieval of summary of matches in progress generally doesn't block, it can overlap with score update operations.
**That means, you should expect only eventual consistency for retrieval of data from this score board implementation.**

## Getting Started

### Prerequisites

You must have Java 22 and Maven 3.x installed.
Classpath set as per manuals of Java and Maven.

### Build from source and run test

1. Go to project folder scoreboard
2. Execute

```sh
./mvn clean test
```

3. Maven will run unit tests and code coverage analysis
4. To examine code coverage reports (after Maven build is finished) go to HTML report

```
/scoreboard/target/site/jacoco/index.html
```

## Using the library

### Initialize the board

```java
// dependencies
MatchStorage matchStorage = new MatchStorage();
Clock clock = Clock.systemUTC();

// It would be the best to use some dependency injection to create and inject MatchStorage and Clock dependencies
// and also to use board further in code
ScoreBoard scoreBoard = new ScoreBoard(matchStorage, clock);
```

### Start new match

Start a new match with initial score 0-0 and add it to the score board:

```java
scoreBoard.startMatch("Mexico","Canada");
```

Match cannot be started if it is already in progress and saved in board.

### Update score

```java
scoreBoard.updateScore("Mexico",1,"Canada",0);
```

Match cannot be updated if it is not in progress and saved in board.

### Finish match

Remove match from score board:

```java
scoreBoard.finishMatch("Mexico","Canada");
```

Match cannot be finished if it is not in progress and saved in board.

### Get a summary of matches in progress

Matches are ordered by their total score descending. If they have same total score, ordering will be by the
most recently started match.

```java
scoreBoard.matchesInProgress();
```

If other threads are updating the board while this operation is executed, we can get stale version of data.

### For more implementation details, check the JavaDoc in code

## Deployment

This library is at the moment only code in this repository. It cannot be packaged as jar and/or be installed in
local/remote Maven repository. It can be done by adding proper sections to pom.xml.

