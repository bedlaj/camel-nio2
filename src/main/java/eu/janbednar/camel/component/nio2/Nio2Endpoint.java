package eu.janbednar.camel.component.nio2;

import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;
import org.apache.camel.Consumer;
import org.apache.camel.MultipleConsumersSupport;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

import java.nio.file.WatchKey;
import java.util.*;

/**
 * Represents a nio2 endpoint.
 */
@UriEndpoint(scheme = "nio2", title = "nio2", syntax="nio2:path",
             consumerClass = Nio2Consumer.class, label = "custom")
public class Nio2Endpoint extends DefaultEndpoint implements MultipleConsumersSupport {
    @UriPath(description = "Path of directory to consume events from")
    @Metadata(required = "true")
    private String path;

    @UriParam(
            description = "Coma separated list of events to watch. Allowed values are: ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE"
            ,defaultValue = "ENTRY_CREATE,ENTRY_MODIFY,ENTRY_DELETE"
    )
    private List<Nio2EventEnum> events = Arrays.asList(Nio2EventEnum.values());

    @UriParam(description="Auto create directory if does not exists", defaultValue = "true")
    private boolean autoCreate = true;

    @UriParam(description="", defaultValue = "1")
    private int concurrentConsumers = 1;

    private WatchKey watchKey = null;

    Nio2Consumer consumer = null;

    public Nio2Endpoint() {
    }

    public Nio2Endpoint(String uri, Nio2Component component) {
        super(uri, component);
    }

    public Nio2Endpoint(String uri, String remaining, Nio2Component component) {
        super(uri, component);
        setPath(remaining);
    }

    public Nio2Endpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("This component does not support producer");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new Nio2Consumer(this, processor);
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
    public void setEvents(List<Nio2EventEnum> events) {
        this.events = events;
    }

    @SuppressWarnings("unused") //called via reflection
    public void setEvents(String commaSeparatedEvents) {
        String[] stringArray = commaSeparatedEvents.split(",");
        Set<Nio2EventEnum> eventsSet = new HashSet<>();
        for (String event: stringArray) {
            eventsSet.add(Nio2EventEnum.valueOf(event.trim()));
        }
        events = new ArrayList<>(eventsSet);
    }

    List<Nio2EventEnum> getEvents() {
        return events;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    @Override
    public Nio2Component getComponent() {
        return (Nio2Component) super.getComponent();
    }


    @Override
    public boolean isMultipleConsumersSupported() {
        return true;
    }
}
