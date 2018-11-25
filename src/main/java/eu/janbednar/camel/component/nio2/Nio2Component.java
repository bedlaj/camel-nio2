package eu.janbednar.camel.component.nio2;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.util.Map;

/**
 * Represents the component that manages {@link Nio2Endpoint}.
 */
public class Nio2Component extends DefaultComponent {
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new Nio2Endpoint(uri, remaining, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
