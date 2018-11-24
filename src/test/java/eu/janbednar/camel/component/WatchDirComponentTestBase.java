package eu.janbednar.camel.component;

import eu.janbednar.camel.component.body.FileEvent;
import eu.janbednar.camel.component.constants.NioEventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class WatchDirComponentTestBase extends CamelTestSupport {

    private static final String TEST_PATH = new File("src/test/resources/testDir/").getAbsolutePath();

    @Override
    protected void doPreSetup() throws Exception {
        super.doPostSetup();
        new File(testPath()).mkdirs();
        cleanTestDir(new File(testPath()));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        cleanTestDir(new File(testPath()));
    }

    private void cleanTestDir(File file) throws Exception{
        if (file == null || !file.exists() || file.listFiles() == null){
            return;
        }
        for (File childFile : file.listFiles()) {
            if (childFile.isDirectory()) {
                cleanTestDir(childFile);
            } else {
                if (!childFile.delete()) {
                    throw new IOException();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }

    protected String testPath(){
        return Paths.get(TEST_PATH, getClass().getSimpleName() + "_"+getTestName().getMethodName()).toString();
    }


    static void assertFileEvent(String expectedFileName, NioEventEnum expectedEventType, Exchange exchange){
        Assert.assertEquals(expectedFileName, exchange.getIn().getBody(FileEvent.class).getEventPath().getFileName().toString());
        Assert.assertEquals(expectedEventType, exchange.getIn().getBody(FileEvent.class).getEventType());
    }
}
