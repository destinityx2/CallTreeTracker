package org.jetbrains.test;

import java.util.List;

/**
 * @author afonichkin
 *         14.05.17
 */
public class MethodCall {
  private final String methodName;
  private final List<String> arguments;

  public MethodCall(final String methodName, final List<String> arguments) {
    this.methodName = methodName;
    this.arguments = arguments;
  }

  @Override
  public String toString() {
    return methodName + arguments.toString();
  }
}
