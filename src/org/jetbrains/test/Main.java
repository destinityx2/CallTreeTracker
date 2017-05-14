package org.jetbrains.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

  public static void main(String[] args) throws IOException {
    List<Future<?>> futures = new ArrayList<>();
    ExecutorService service = Executors.newFixedThreadPool(3);
    for (int i = 0; i < 5; i++) {
      int start = 100 * i;
      List<String> arguments = IntStream.range(start, start + 10)
              .mapToObj(Integer::toString)
              .collect(Collectors.toList());
      futures.add(service.submit(() -> new DummyApplication(arguments).start()));
    }
    service.shutdown();

    for (Future future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    // Print string representation to console
    System.out.println(CallTreeTrackingService.getStringRepresentation());

    // Print json representation to console
    System.out.println(CallTreeTrackingService.toJson().toString());
  }
}
