package eu.janbednar.camel.component.nio2;

import eu.janbednar.camel.component.nio2.body.FileEvent;
import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class Nio2ComponentTestBase extends CamelTestSupport {

    //private static final String TEST_PATH = new File("src/test/resources/testDir/").getAbsolutePath();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Override
    protected void doPreSetup() throws Exception {
        super.doPostSetup();
        cleanTestDir(new File(testPath()));
        new File(testPath()).mkdirs();

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

    protected String testPath() {
            return folder.getRoot().getAbsolutePath() + folder.getRoot().toPath().getFileSystem().getSeparator();
    }


    static void assertFileEvent(String expectedFileName, Nio2EventEnum expectedEventType, Exchange exchange){
        Assert.assertEquals(expectedFileName, exchange.getIn().getBody(FileEvent.class).getEventPath().getFileName().toString());
        Assert.assertEquals(expectedEventType, exchange.getIn().getBody(FileEvent.class).getEventType());
    }

    static boolean isWindows(){
        //WatchService behaves differently on Windows (Emits both MODIFY and DELETE when file deleted)
        //see https://stackoverflow.com/questions/33753561/java-nio-watch-service-created-both-entry-create-and-entry-modify-when-a-new
        return SystemUtils.IS_OS_WINDOWS;
    }

    static void assertCreateAndRemoveFileEvents(MockEndpoint mock, File newFile) throws Exception{
        mock.expectedMessageCount(isWindows() ? 3 : 2);
        mock.assertIsSatisfied();

        assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_CREATE, mock.getExchanges().get(0));

        if (isWindows()){
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_MODIFY, mock.getExchanges().get(1));
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_DELETE, mock.getExchanges().get(2));
        } else {
            assertFileEvent(newFile.getName(), Nio2EventEnum.ENTRY_DELETE, mock.getExchanges().get(1));
        }
    }
}
