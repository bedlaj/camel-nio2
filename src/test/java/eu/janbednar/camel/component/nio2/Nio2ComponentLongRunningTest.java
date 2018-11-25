package eu.janbednar.camel.component.nio2;

import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

@Ignore
public class Nio2ComponentLongRunningTest extends Nio2ComponentTestBase {
    private Counter counterCreate = new Counter();
    private Counter counterDelete = new Counter();

    @Test
    public void testCreateFileEveryMinute() throws Exception {
        MockEndpoint watchAll = getMockEndpoint("mock:watchCreate");

        Long end = System.currentTimeMillis() + 60*60*1000;
        int i = 0;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+"");
            if (!newFile.createNewFile()){
                throw new IllegalStateException("Cannot create file "+newFile.toString());
            }
            System.out.println(newFile.toString());
            Thread.sleep(1000);
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_CREATE, watchAll.getExchanges().get(i));
            System.out.println(i+": OK");
            i++;
        }
    }

    @Test
    public void testCreateFileEverySecondForHour() throws Exception {
        MockEndpoint watchAll = getMockEndpoint("mock:watchCreate");

        Long end = System.currentTimeMillis() + 60*60*1000;
        int i = 0;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+"");
            if (!newFile.createNewFile()){
                throw new IllegalStateException("Cannot create file "+newFile.toString());
            }
            System.out.println(newFile.toString());
            Thread.sleep(1000);
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_CREATE, watchAll.getExchanges().get(i));
            System.out.println(i+": OK");
            i++;
        }
    }

    @Test
    public void testCreateFileForHour() throws Exception {
        context.stopRoute("watchCreate"); //Stop this route to prevent out of memory caused by full mock endpoint
        Long end = System.currentTimeMillis() + 60*60*1000;
        Long i = 0L;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+UUID.randomUUID().toString());
            if (!newFile.createNewFile()){
                throw new IllegalStateException("Cannot create file "+newFile.toString());
            }
            Thread.sleep(5);
            i++;
            newFile.delete();
            Thread.sleep(5);
        }
        Thread.sleep(10000);
        Assert.assertEquals(i, counterCreate.getCount());
        Assert.assertEquals(i, counterDelete.getCount());
    }

    @Test
    public void testCreateFileEveryTenMinutesForHour() throws Exception {
        MockEndpoint watchAll = getMockEndpoint("mock:watchCreate");

        Long end = System.currentTimeMillis() + 60*60*1000;
        int i = 0;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+"");
            if (!newFile.createNewFile()){
                throw new IllegalStateException("Cannot create file "+newFile.toString());
            }
            System.out.println(newFile.toString());
            Thread.sleep(600000);
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_CREATE, watchAll.getExchanges().get(i));
            System.out.println(i+": OK");
            i++;
        }
    }

    static class Counter implements Processor{
        Long count = 0L;
        @Override
        public void process(Exchange exchange) throws Exception {
            count++;
            System.out.println(exchange.getIn().getBody());
        }

        public Long getCount() {
            return count;
        }
    }



    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("nio2://"+testPath()+"?events=ENTRY_CREATE")
                        .routeId("watchCreate")
                        .to("mock:watchCreate");

                from("nio2://"+testPath()+"?events=ENTRY_CREATE")
                        .process(counterCreate);

                from("nio2://"+testPath()+"?events=ENTRY_DELETE")
                        .process(counterDelete);
            }
        };
    }
}
