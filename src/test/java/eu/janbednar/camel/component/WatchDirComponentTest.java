package eu.janbednar.camel.component;

import eu.janbednar.camel.component.body.FileEvent;
import eu.janbednar.camel.component.constants.NioEventEnum;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

public class WatchDirComponentTest extends WatchDirComponentTestBase {

    @Test
    public void testCreateFile() throws Exception {
        MockEndpoint watchAll = getMockEndpoint("mock:watchAll");
        MockEndpoint watchCreate = getMockEndpoint("mock:watchCreate");
        MockEndpoint watchModify = getMockEndpoint("mock:watchModify");
        MockEndpoint watchDelete = getMockEndpoint("mock:watchDelete");
        MockEndpoint watchDeleteOrCreate = getMockEndpoint("mock:watchDeleteOrCreate");

        File newFile = new File(new File(testPath()), UUID.randomUUID().toString());
        if (!newFile.createNewFile()){
            throw new IllegalStateException("Cannot create file "+newFile.toString());
        }

        watchAll.expectedMessageCount(1);
        watchAll.assertIsSatisfied();

        watchCreate.expectedMessageCount(1);
        watchCreate.assertIsSatisfied();

        watchDeleteOrCreate.expectedMessageCount(1);
        watchDeleteOrCreate.assertIsSatisfied();

        watchModify.expectedMessageCount(0);
        watchModify.assertIsSatisfied();

        watchDelete.expectedMessageCount(0);
        watchDelete.assertIsSatisfied();

        assertFileEvent(newFile.getName(), NioEventEnum.ENTRY_CREATE, watchAll.getExchanges().get(0));
        assertFileEvent(newFile.getName(), NioEventEnum.ENTRY_CREATE, watchCreate.getExchanges().get(0));
        assertFileEvent(newFile.getName(), NioEventEnum.ENTRY_CREATE, watchDeleteOrCreate.getExchanges().get(0));
    }

    @Test
    public void testCreateAndRemoveFile() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:watchAll");

        File newFile = new File(new File(testPath()), UUID.randomUUID().toString());
        Assert.assertTrue(newFile.createNewFile());
        Assert.assertTrue(newFile.delete());

        assertCreateAndRemoveFileEvents(mock, newFile);
    }

    @Test
    public void testStopStart() throws Exception {
        for (int i = 0; i < 10; i++) {
            context.stopRoute("watchAll");
            context.startRoute("watchAll");
        }

        MockEndpoint mock = getMockEndpoint("mock:watchAll");

        File newFile = new File(new File(testPath()), UUID.randomUUID().toString());
        Assert.assertTrue(newFile.createNewFile());
        Assert.assertTrue(newFile.delete());

        assertCreateAndRemoveFileEvents(mock, newFile);
    }

    @Test
    public void testSuspendResume() throws Exception {
        for (int i = 0; i < 10; i++) {
            context.suspendRoute("watchAll");
            context.resumeRoute("watchAll");
        }

        MockEndpoint mock = getMockEndpoint("mock:watchAll");

        File newFile = new File(new File(testPath()), UUID.randomUUID().toString());
        Assert.assertTrue(newFile.createNewFile());
        Assert.assertTrue(newFile.delete());

        assertCreateAndRemoveFileEvents(mock, newFile);
    }

    @Test
    public void testCreateBatch() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:watchAll");

        for (int i = 0; i<100; i++){
            File newFile = new File(new File(testPath()), i+"");
            //Thread.sleep(1000);
            newFile.createNewFile();
        }

        mock.expectedMessageCount(100);
        mock.expectedMessagesMatches(exchange -> exchange.getIn().getBody(FileEvent.class).getEventType() == NioEventEnum.ENTRY_CREATE);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testModify() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:watchModify");

        File newFile = new File(new File(testPath()), UUID.randomUUID().toString());
        newFile.createNewFile();

        for (int i = 0; i < 100; i++) {
            newFile.setLastModified(System.currentTimeMillis() + i);
        }

        mock.expectedMessageCount(100);
        mock.expectedMessagesMatches(exchange -> exchange.getIn().getBody(FileEvent.class).getEventType() == NioEventEnum.ENTRY_MODIFY);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("watchdir://"+testPath())
                        .routeId("watchAll")
                        .to("mock:watchAll");

                from("watchdir://"+testPath()+"?events=ENTRY_CREATE")
                        .to("mock:watchCreate");

                from("watchdir://"+testPath()+"?events=ENTRY_MODIFY")
                        .to("mock:watchModify");

                from("watchdir://"+testPath()+"?events=ENTRY_DELETE")
                        .to("mock:watchDelete");

                from("watchdir://"+testPath()+"?events=ENTRY_DELETE,ENTRY_CREATE")
                        .to("mock:watchDeleteOrCreate");
            }
        };
    }

}
