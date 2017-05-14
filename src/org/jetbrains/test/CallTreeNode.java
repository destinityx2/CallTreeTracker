package org.jetbrains.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author afonichkin
 *         14.05.17
 */
public class CallTreeNode {
  private final CallTreeNode parent;
  private final List<CallTreeNode> children;
  private final MethodCall value;

  private static final String VALUE_JSON_FIELD = "value";
  private static final String CHILDREN_JSON_FIELD = "children";

  public CallTreeNode(final MethodCall value, final CallTreeNode parent) {
    this.parent = parent;
    this.value = value;
    this.children = new ArrayList<>();
  }

  public void add(final CallTreeNode child) {
    children.add(child);
  }

  public CallTreeNode getParent() {
    return parent;
  }

  public String getStringRepresentation(final int depth) {
    final StringBuilder sb = new StringBuilder();
    sb.append(String.join("", Collections.nCopies(depth, "--")));
    sb.append(value);
    sb.append("\n");

    for (final CallTreeNode child : children) {
      sb.append(child.getStringRepresentation(depth + 1));
    }

    return sb.toString();
  }

  public JsonElement toJson() {
    final Gson gson = new Gson();
    final JsonObject result = new JsonObject();
    result.add(VALUE_JSON_FIELD, gson.toJsonTree(this.value));

    final JsonArray array = new JsonArray();
    for (final CallTreeNode child : children) {
      array.add(child.toJson());
    }
    result.add(CHILDREN_JSON_FIELD, array);

    return result;
  }

  public static CallTreeNode fromJson(final JsonElement jsonElement, final CallTreeNode parent) {
    final Gson gson = new Gson();
    final JsonObject object = jsonElement.getAsJsonObject();
    final MethodCall value = gson.fromJson(object.getAsJsonObject(VALUE_JSON_FIELD), MethodCall.class);
    if (value == null) {
      return null;
    }

    JsonArray children = object.getAsJsonArray(CHILDREN_JSON_FIELD);

    final CallTreeNode node = new CallTreeNode(value, parent);

    for (int i = 0; i < children.size(); ++i) {
      final JsonElement childElement = children.get(i);
      final CallTreeNode childNode = CallTreeNode.fromJson(childElement, node);
      node.add(childNode);
    }

    return node;
  }

  public List<CallTreeNode> getChildren() {
    return children;
  }
}
