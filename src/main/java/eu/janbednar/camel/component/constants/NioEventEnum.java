package eu.janbednar.camel.component.constants;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public enum NioEventEnum {
    ENTRY_CREATE(StandardWatchEventKinds.ENTRY_CREATE),
    ENTRY_DELETE(StandardWatchEventKinds.ENTRY_DELETE),
    ENTRY_MODIFY(StandardWatchEventKinds.ENTRY_MODIFY);

    WatchEvent.Kind<Path> kind;
    NioEventEnum(WatchEvent.Kind<Path> kind) {
        this.kind = kind;
    }

    public WatchEvent.Kind<Path> getKind() {
        return kind;
    }
}
