package eu.janbednar.camel.component.body;

import eu.janbednar.camel.component.constants.NioEventEnum;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FileEvent {
    private NioEventEnum eventType;
    private Path eventPath;

    public FileEvent(NioEventEnum eventType, Path eventPath) {
        this.eventType = eventType;
        this.eventPath = eventPath;
    }

    public FileEvent(WatchEvent<Path> event) {
        this.eventPath = event.context();
        this.eventType = NioEventEnum.valueOf(event.kind().name());
    }

    public NioEventEnum getEventType() {
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
