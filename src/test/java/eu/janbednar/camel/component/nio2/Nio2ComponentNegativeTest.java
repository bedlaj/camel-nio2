package eu.janbednar.camel.component.nio2;

import org.apache.camel.builder.RouteBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class Nio2ComponentNegativeTest extends Nio2ComponentTestBase {

    @Test
    public void testNonExistentDirectory() throws Exception{
        Exception ex = null;
        String nonExistentDirectory = Paths.get(testPath(), "nonExistentDirectory").toString();
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from("nio2://"+nonExistentDirectory+"?autoCreate=false")
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
            public void configure() {
                from("nio2://"+nonExistentDirectory+"?autoCreate=true")
                        .to("mock:watchAll");
            }
        });

    }

}
