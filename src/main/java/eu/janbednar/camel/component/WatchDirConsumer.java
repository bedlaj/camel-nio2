package eu.janbednar.camel.component;

import eu.janbednar.camel.component.body.FileEvent;
import eu.janbednar.camel.component.constants.NioEventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Suspendable;
import org.apache.camel.impl.DefaultConsumer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * The watchDir consumer.
 */
public class WatchDirConsumer extends DefaultConsumer implements Suspendable {
    private final WatchDirEndpoint endpoint;
    WatchService watchService;
    ExecutorService executorService;
    WatchEvent.Kind[] kinds;

    public WatchDirConsumer(WatchDirEndpoint endpoint, Processor processor) throws IOException {
        super(endpoint, processor);
        this.endpoint = endpoint;

        kinds = new WatchEvent.Kind[endpoint.getEvents().size() + 1];
        kinds[0] = StandardWatchEventKinds.OVERFLOW; //always watch Overflow event for logging purposes
        List<NioEventEnum> events = endpoint.getEvents();
        for (int i = 0; i < events.size(); i++) {
            kinds[i + 1] = events.get(i).getKind();
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        Path directory = Paths.get(endpoint.getPath());
        watchService = FileSystems.getDefault().newWatchService();
        if (endpoint.isAutoCreate() && !Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        directory.register(watchService, kinds);
        executorService = endpoint.getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this, endpoint.getEndpointUri());
        executorService.submit(new WatchDirRunnable());
    }

    @Override
    protected void doStop() throws Exception {
        try {
            watchService.close();
            log.info("WatchService stopped");
        } catch (IOException e) {
            log.info("Cannot stop WatchService", e);
        }
        executorService.shutdownNow();
        super.doStop();
    }

    class WatchDirRunnable implements Runnable {
        WatchKey watchKey = null;

        @Override
        public void run() {
            while (take() && isRunAllowed() && !isStoppingOrStopped() && !isSuspendingOrSuspended()) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind().equals(StandardWatchEventKinds.OVERFLOW)) {
                        log.warn("OVERFLOW occured");
                        continue;
                    }

                    WatchEvent<Path> watchEventCast = (WatchEvent<Path>) event;
                    for (int i = 0; i < event.count(); i++) { //if event.count > 1, send it multiple times
                        Exchange exchange = endpoint.createExchange();
                        try {
                            //log.info(event.kind() + " Event Happened on " + event.context());
                            exchange.getIn().setBody(new FileEvent(watchEventCast));
                            getProcessor().process(exchange);
                        } catch (Throwable e) {
                            exchange.setException(e);
                        } finally {
                            // log exception if an exception occurred and was not handled
                            if (exchange.getException() != null) {
                                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                            }
                        }
                    }
                }
            }
        }

        private boolean take() {
            if (watchKey != null && !watchKey.reset()) {
                log.info("WatchDirRunnable stopping, because watchKey is in invalid state");
                return false;
            }
            try {
                watchKey = watchService.take();
                return true;
            } catch (InterruptedException | ClosedWatchServiceException e) {
                log.info("WatchDirRunnable stopping because " + e.getClass().getSimpleName() + ": " + e.getMessage());
                return false;
            }
        }
    }
}
