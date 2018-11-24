package eu.janbednar.camel.component;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Map;

/**
 * Represents the component that manages {@link WatchDirEndpoint}.
 */
public class WatchDirComponent extends DefaultComponent {
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new WatchDirEndpoint(uri, remaining, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
