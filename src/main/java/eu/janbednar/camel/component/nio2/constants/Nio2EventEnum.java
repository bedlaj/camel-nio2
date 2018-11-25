package eu.janbednar.camel.component.nio2.constants;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public enum Nio2EventEnum {
    ENTRY_CREATE(StandardWatchEventKinds.ENTRY_CREATE),
    ENTRY_DELETE(StandardWatchEventKinds.ENTRY_DELETE),
    ENTRY_MODIFY(StandardWatchEventKinds.ENTRY_MODIFY);

    WatchEvent.Kind<Path> kind;
    Nio2EventEnum(WatchEvent.Kind<Path> kind) {
        this.kind = kind;
    }

    public WatchEvent.Kind<Path> getKind() {
        return kind;
    }
}
