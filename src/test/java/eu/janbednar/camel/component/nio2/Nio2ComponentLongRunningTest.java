package eu.janbednar.camel.component.nio2;

import eu.janbednar.camel.component.nio2.category.LongRunningTests;
import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Category(LongRunningTests.class)
public class Nio2ComponentLongRunningTest extends Nio2ComponentTestBase {
    private Counter counterCreate = new Counter();
    private Counter counterDelete = new Counter();

    @Test
    public void testCreateFileEverySecondForMinute() throws Exception {
        MockEndpoint watchAll = getMockEndpoint("mock:watchCreate");

        Long end = System.currentTimeMillis() + 60*1000;
        int i = 0;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+"");
            if (!newFile.createNewFile()){
                throw new IllegalStateException("Cannot create file "+newFile.toString());
            }
            System.out.println(newFile.toString());
            Thread.sleep(1000);
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_CREATE, watchAll.getExchanges().get(i));
            i++;
        }
    }

    @Test
    public void testCreateFileForMinute() throws Exception {
        context.stopRoute("watchCreate"); //Stop this route to prevent out of memory caused by full mock endpoint
        Long end = System.currentTimeMillis() + 60*1000;
        Long created = 0L;
        Long deleted = 0L;
        while (System.currentTimeMillis() < end){
            File newFile = new File(new File(testPath()), System.currentTimeMillis()+UUID.randomUUID().toString());
            if (newFile.createNewFile()){
                created++;
            }

            if (newFile.delete()){
                deleted++;
            };
        }

        Thread.sleep(10000);
        Assert.assertEquals(created, counterCreate.getCount());
        Assert.assertEquals(deleted, counterDelete.getCount());
    }


    static class Counter implements Processor{
        AtomicLong count = new AtomicLong();
        @Override
        public void process(Exchange exchange) throws Exception {
            count.incrementAndGet();
            //System.out.println(exchange.getIn().getBody());
        }

        public Long getCount() {
            return count.get();
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
