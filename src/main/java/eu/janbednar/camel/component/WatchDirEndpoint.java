package eu.janbednar.camel.component;

import eu.janbednar.camel.component.constants.NioEventEnum;
import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import java.util.*;

/**
 * Represents a watchDir endpoint.
 */
@UriEndpoint(scheme = "watchdir", title = "watchDir", syntax="watchdir:path",
             consumerClass = WatchDirConsumer.class, label = "custom")
public class WatchDirEndpoint extends DefaultEndpoint implements MultipleConsumersSupport {
    @UriPath(description = "Path of directory to consume events from")
    @Metadata(required = "true")
    private String path;

    @UriParam(
            description = "Coma separated list of events to watch. Allowed values are: ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE"
            ,defaultValue = "ENTRY_CREATE,ENTRY_MODIFY,ENTRY_DELETE"
    )
    private List<NioEventEnum> events = Arrays.asList(NioEventEnum.values());

    @UriParam(description="Auto create directory if does not exists", defaultValue = "true")
    private boolean autoCreate = true;

    public WatchDirEndpoint() {
    }

    public WatchDirEndpoint(String uri, WatchDirComponent component) {
        super(uri, component);
    }

    public WatchDirEndpoint(String uri,String remaining, WatchDirComponent component) {
        super(uri, component);
        setPath(remaining);
    }

    public WatchDirEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("This component does not support producer");
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new WatchDirConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Some description of this option, and what it does
     */
    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    /**
     * Some description of this option, and what it does
     */
    @SuppressWarnings("unused") //called via reflection
    public void setEvents(List<NioEventEnum> events) {
        this.events = events;
    }

    @SuppressWarnings("unused") //called via reflection
    public void setEvents(String commaSeparatedEvents) {
        String[] stringArray = commaSeparatedEvents.split(",");
        Set<NioEventEnum> eventsSet = new HashSet<>();
        for (String event: stringArray) {
            eventsSet.add(NioEventEnum.valueOf(event.trim()));
        }
        events = new ArrayList<>(eventsSet);
    }

    List<NioEventEnum> getEvents() {
        return events;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    @Override
    public boolean isMultipleConsumersSupported() {
        return true;
    }
}
