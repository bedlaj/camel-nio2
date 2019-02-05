package eu.janbednar.camel.component.nio2;

import eu.janbednar.camel.component.nio2.body.FileEvent;
import eu.janbednar.camel.component.nio2.constants.Nio2EventEnum;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The nio2 consumer.
 */
public class Nio2Consumer extends DefaultConsumer {
    WatchService watchService;
    ExecutorService watchDirExecutorService;
    ExecutorService pollExecutorService;
    WatchEvent.Kind[] kinds;
    ArrayBlockingQueue<WatchEvent<?>> eventQueue = new ArrayBlockingQueue<>(1000,true);

    public Nio2Consumer(Nio2Endpoint endpoint, Processor processor) {
        super(endpoint, processor);

        kinds = new WatchEvent.Kind[endpoint.getEvents().size() + 1];
        kinds[0] = StandardWatchEventKinds.OVERFLOW; //always watch Overflow event for logging purposes
        List<Nio2EventEnum> events = endpoint.getEvents();
        for (int i = 0; i < events.size(); i++) {
            kinds[i + 1] = events.get(i).getKind();
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        Path directory = Paths.get(getEndpoint().getPath());
        watchService = directory.getFileSystem().newWatchService();
        if (getEndpoint().isAutoCreate() && !Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        directory.register(watchService, kinds);
        watchDirExecutorService = getEndpoint().getCamelContext().getExecutorServiceManager()
                .newSingleThreadExecutor(this, "Nio2WatchService");
        watchDirExecutorService.submit(new WatchServiceRunnable());

        pollExecutorService = getEndpoint().getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, "Nio2Poll", getEndpoint().getConcurrentConsumers());
        for (int i = 0; i < getEndpoint().getConcurrentConsumers(); i++) {
            pollExecutorService.submit(new PollRunnable());
        }
    }

    @Override
    protected void doStop() throws Exception {
        try {
            watchService.close();
            log.info("WatchService stopped");
        } catch (IOException e) {
            log.info("Cannot stop WatchService", e);
        }
        watchDirExecutorService.shutdownNow();
        pollExecutorService.shutdownNow();

        super.doStop();
    }

    class PollRunnable implements Runnable{
        @Override
        public void run() {
            while (isRunAllowed() && !isStoppingOrStopped() && !isSuspendingOrSuspended()) {
                WatchEvent<?> event = null;
                try {
                    event = eventQueue.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (event != null){
                    WatchEvent<Path> watchEventCast = cast(event);
                    for (int i = 0; i < event.count(); i++) { //if event.count > 1, send it multiple times
                        Exchange exchange = getEndpoint().createExchange();
                        exchange.getIn().setBody(new FileEvent(watchEventCast));
                        try {
                            getProcessor().process(exchange);
                        } catch (Exception e) {
                            getExceptionHandler().handleException(e);
                        }
                    }
                }

            }
        }

        @SuppressWarnings("unchecked")
        private WatchEvent<Path> cast(WatchEvent<?> event){
            if (event != null && event.kind().type() == Path.class){
                return (WatchEvent<Path>) event;
            } else {
                throw new ClassCastException("Cannot cast "+event+" to WatchEvent<Path>");
            }
        }
    }


    class WatchServiceRunnable implements Runnable {
        WatchKey watchKey = null;

        @Override
        public void run() {
            while (take() && isRunAllowed() && !isStoppingOrStopped() && !isSuspendingOrSuspended()) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind().equals(StandardWatchEventKinds.OVERFLOW)) {
                        log.warn("OVERFLOW occurred");
                        continue;
                    }
                    eventQueue.offer(event);
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
            } catch (ClosedWatchServiceException | InterruptedException e) {
                log.info("WatchDirRunnable stopping because " + e.getClass().getSimpleName() + ": " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    public Nio2Endpoint getEndpoint() {
        return (Nio2Endpoint) super.getEndpoint();
    }
}
