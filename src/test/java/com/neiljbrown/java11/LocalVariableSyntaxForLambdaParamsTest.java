/*
 *  Copyright 2019-present the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neiljbrown.java11;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * A JUnit test case providing examples of Java 11's new support for using local variable type inference ('var') syntax
 * on parameters of lambda expressions.
 * <p>
 * Java 10 introduced type inference for local variables declared using 'var'. Java 11 extends this to allow 'var' to
 * also be used on lambda parameters. Lambda expressions already supported type inference for params in J10, so you
 * might wonder why feature was added in Java 11. It supports annotating a Lambda parameter(s) without having to specify
 * an explicit type (sacrificing type inference).
 */
public class LocalVariableSyntaxForLambdaParamsTest {

  /**
   * An example of the use of the 'var' syntax on a Lambda param to support annotating the param. This example
   * illustrates the common use-case of an annotation to support a compile-time null safety check on the param.
   */
  @Test
  public void testVarLambdaParamSupportingAnnotatedCompileTimeNullCheck() {
    // Simulation of externally supplied Collection that might contain null elements
    List<String> inputStrings = List.of("A", "b", "C", "d");

    List<String> strings = inputStrings.stream()
      // Example use of 'var' on Lambda method param supporting annotation e.g. compile time check on possible null
      .map((@NonNull var s) -> s.toLowerCase())
      .collect(Collectors.toList());

    assertThat(strings).hasSameSizeAs(inputStrings);
  }

  // Dummy annotation for marking a param as being subject to a non-null check, e.g. at compile time. In practice this
  // would be provided by a library such as the Checker Framework,
  @interface NonNull {}
}