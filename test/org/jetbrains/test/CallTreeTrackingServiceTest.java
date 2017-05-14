package org.jetbrains.test;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author afonichkin
 *         14.05.17
 */
public class CallTreeTrackingServiceTest {
  @Test
  public void testExampleSerialize() throws IOException {
    List<Future<Object>> futures = new ArrayList<>();
    ExecutorService service = Executors.newFixedThreadPool(3);
    for(int i = 0; i < 5; i++) {
      int start = 100 * i;
      List<String> arguments = IntStream.range(start, start + 10)
              .mapToObj(Integer :: toString)
              .collect(Collectors.toList());
      futures.add(service.submit(() -> {
        new DummyApplication(arguments).start();
        return null;
      }));
    }
    service.shutdown();

    for (Future future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    System.out.println(CallTreeTrackingService.getStringRepresentation());

    CallTreeTrackingService.Serializer.serialize("example.json");
  }

  @Test
  public void testExampleDeserialize() throws IOException {
    CallTreeTrackingService.Serializer.deserialize("example.json");
    System.out.println(CallTreeTrackingService.getStringRepresentation());
  }
}
