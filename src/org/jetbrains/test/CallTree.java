package org.jetbrains.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author afonichkin
 *         14.05.17
 */
public class CallTree {
  private CallTreeNode root;
  private CallTreeNode activeNode;

  public CallTree() {
    // fake root
    root = new CallTreeNode(null, null);
    activeNode = root;
  }

  public CallTreeNode getActiveNode() {
    return activeNode;
  }

  public void registerEnter(final CallTreeNode node) {
    if (root == null) {
      root = node;
      activeNode = root;
    } else {
      activeNode.add(node);
      activeNode = node;
    }
  }

  public void registerExit() {
    activeNode = activeNode.getParent();
  }

  // We don't override {@code toString()}, because {@code toString()} is often used for logging,
  // but here we get full representation of call tree, which is not suitable for logging purposes.
  public String getStringRepresentation() {
    final StringBuilder sb = new StringBuilder();
    for (final CallTreeNode child : root.getChildren()) {
      sb.append(child.getStringRepresentation(0));
    }
    return sb.toString();
  }

  public JsonElement toJson() {
    JsonArray array = new JsonArray();
    for (final CallTreeNode child : root.getChildren()) {
      array.add(child.toJson());
    }
    return array;
  }

  public static CallTree fromJson(final JsonElement jsonElement) {
    final CallTree callTree = new CallTree();
    callTree.root = new CallTreeNode(null, null);
    JsonArray array = jsonElement.getAsJsonArray();
    for (int i = 0; i < array.size(); ++i) {
      final CallTreeNode childNode = CallTreeNode.fromJson(array.get(i), callTree.root);
      callTree.root.add(childNode);
    }

    return callTree;
  }
}
