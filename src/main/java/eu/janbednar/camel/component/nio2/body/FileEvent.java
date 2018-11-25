package eu.janbednar.camel.component.nio2.body;

import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FileEvent {
    private Nio2EventEnum eventType;
    private Path eventPath;

    public FileEvent(Nio2EventEnum eventType, Path eventPath) {
        this.eventType = eventType;
        this.eventPath = eventPath;
    }

    public FileEvent(WatchEvent<Path> event) {
        this.eventPath = event.context();
        this.eventType = Nio2EventEnum.valueOf(event.kind().name());
    }

    public Nio2EventEnum getEventType() {
        return eventType;
    }

    public Path getEventPath() {
        return eventPath;
    }

    @Override
    public String toString() {
        return "FileEvent{" +
                "eventType=" + eventType +
                ", eventPath=" + eventPath +
                '}';
    }
}
