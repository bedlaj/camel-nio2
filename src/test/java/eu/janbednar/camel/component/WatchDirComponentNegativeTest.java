package eu.janbednar.camel.component;

import eu.janbednar.camel.component.body.FileEvent;
import eu.janbednar.camel.component.constants.NioEventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.UUID;

public class WatchDirComponentNegativeTest extends WatchDirComponentTestBase {

    @Test
    public void testNonExistentDirectory() throws Exception{
        Exception ex = null;
        String nonExistentDirectory = Paths.get(testPath(), "nonExistentDirectory").toString();
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("watchdir://"+nonExistentDirectory+"?autoCreate=false")
                            .to("mock:watchAll");
                }
            });
        } catch (Exception e){
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof NoSuchFileException);
        Assert.assertEquals(nonExistentDirectory, ex.getMessage());

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("watchdir://"+nonExistentDirectory+"?autoCreate=true")
                        .to("mock:watchAll");
            }
        });

    }

}
