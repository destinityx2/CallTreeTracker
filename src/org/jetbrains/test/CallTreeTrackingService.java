package org.jetbrains.test;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author afonichkin
 *         14.05.17
 */
public class CallTreeTrackingService {
  private static final Map<Long, CallTree> threadIdToCallTree = new ConcurrentHashMap<>();

  private static final String THREAD_ID_JSON_FIELD = "threadId";
  private static final String CALL_TREE_JSON_FIELD = "callTree";

  public static Exiter registerEnter(Object... arguments) {
    final long threadId = Thread.currentThread().getId();
    final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();

    final List<Object> argumentsList = Arrays.asList(arguments);
    if (!threadIdToCallTree.containsKey(threadId)) {
      threadIdToCallTree.put(threadId, new CallTree());
    }
    final CallTree callTree = threadIdToCallTree.get(threadId);

    final MethodCall methodCall = new MethodCall(
            methodName,
            argumentsList.stream().map(Object::toString).collect(Collectors.toList())
    );

    final CallTreeNode activeNode = callTree.getActiveNode();
    final CallTreeNode node = new CallTreeNode(methodCall, activeNode);

    callTree.registerEnter(node);

    return new Exiter();
  }

  public static void registerExit() {
    final long threadId = Thread.currentThread().getId();
    if (!threadIdToCallTree.containsKey(threadId)) {
      throw new RuntimeException("The method call wasn't registered for thread id " + threadId);
    }

    threadIdToCallTree.get(threadId).registerExit();
  }

  public static String getStringRepresentation() {
    final String separatingCharacter = "@";
    final int nCopies = 10;

    final StringBuilder sb = new StringBuilder();
    for (final Long threadId : threadIdToCallTree.keySet()) {
      sb.append(String.join("", Collections.nCopies(nCopies, separatingCharacter)));
      sb.append("\n");
      sb.append(threadIdToCallTree.get(threadId).getStringRepresentation());
    }

    return sb.toString();
  }

  public static JsonElement toJson() {
    final JsonArray array = new JsonArray();
    for (final Long threadId : threadIdToCallTree.keySet()) {
      final JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty(THREAD_ID_JSON_FIELD, threadId);
      jsonObject.add(CALL_TREE_JSON_FIELD, threadIdToCallTree.get(threadId).toJson());

      array.add(jsonObject);
    }

    return array;
  }

  // Recovers static object from Json
  public static void fromJson(final JsonElement jsonElement) {
    final JsonArray jsonArray = jsonElement.getAsJsonArray();
    for (int i = 0; i < jsonArray.size(); ++i) {
      final JsonElement element = jsonArray.get(i);
      final JsonObject object = element.getAsJsonObject();
      final long threadId = object.get(THREAD_ID_JSON_FIELD).getAsLong();
      final JsonElement callTree = object.get(CALL_TREE_JSON_FIELD);
      threadIdToCallTree.put(threadId, CallTree.fromJson(callTree));
    }
  }

  public static class Serializer {
    public static void serialize(final String pathToJson) throws IOException {
      final JsonElement element = CallTreeTrackingService.toJson();
      final Gson gson = new Gson();
      try (JsonWriter jsonWriter = new JsonWriter(new BufferedWriter(new FileWriter(pathToJson)))) {
        gson.toJson(element, jsonWriter);
      }
    }

    // Recovers static object from Json
    public static void deserialize(final String pathToJson) throws IOException {
      final JsonParser jsonParser = new JsonParser();

      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToJson))) {
        final JsonElement elem = jsonParser.parse(bufferedReader);
        CallTreeTrackingService.fromJson(elem);
      }
    }
  }

  public static class Exiter implements Closeable {
    @Override
    public void close() {
      registerExit();
    }
  }
}
