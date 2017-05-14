package org.jetbrains.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws IOException {
        List<Future<?>> futures = new ArrayList<>();
        ExecutorService service = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 5; i++) {
            int start = 100 * i;
            List<String> arguments = IntStream.range(start, start + 10)
                    .mapToObj(Integer :: toString)
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
