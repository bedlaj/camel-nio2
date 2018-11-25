# Camel WatchDir component

[![Build Status](https://travis-ci.org/bedlaj/camel-watchDir.svg?branch=master)](https://travis-ci.org/bedlaj/camel-watchDir)

# Description
This component uses Java-7 NIO.2 to watch directory changes. Please see [WatchService JavaDoc](https://docs.oracle.com/javase/8/docs/api/java/nio/file/WatchService.html), specially section "Platform dependencies" Before using this component.

# Component dependency
#### Maven instructions:
Add [JitPack.io](https://jitpack.io/#bedlaj/camel-watchDir) repository

```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

Add dependency to component. Always use the same component version as is your `camel-core` version.

```xml
<dependency>
    <groupId>com.github.bedlaj</groupId>
    <artifactId>camel-watchDir</artifactId>
    <version>2.22.1-SNAPSHOT</version>
</dependency>
```

### URI format
```java
watchdir:path[?autoCreate=false][&events=ENTRY_CREATE,ENTRY_MODIFY,ENTRY_DELETE]
```

### Endpoint options

| Name | Default value | Description |
| ---- | ------------- | ----------- |
| autocreate | true | Auto create directory if does not exists |
| events | ENTRY_CREATE,ENTRY_MODIFY,ENTRY_DELETE | Coma separated list of events to watch. Allowed values are: ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE |

### Examples:
```java
from("watchdir:/tmp/inputPath?events=ENTRY_CREATE,ENTRY_DELETE")
    .process(exchange -> {
        FileEvent event = exchange.getIn().getBody(FileEvent.class);
        log.info(event.getEventType()+" happened with path "+event.getEventPath());
    });
```
Other examples can be found in [JUnit tests](https://github.com/bedlaj/camel-watchDir/tree/master/src/test/java/eu/janbednar/camel/component)
