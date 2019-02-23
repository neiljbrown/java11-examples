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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * A JUnit test case providing examples of enhancements to the {@link java.util.function.Predicate} class in Java 11.
 */
public class PredicateTest {

  /**
   * A new static method {@link java.util.function.Predicate#not(Predicate)} has been added that supports creating a
   * Predicate that is the negation of a supplied Predicate. One of things this facilitates is using a method
   * reference when the right hand-side/body of the Lambda is a negated method invocation (on the single Lambda
   * param), which wasn't previously possible. See example below.
   */
  @Test
  public void testNot() {
    final String lyrics = "How long before I get in?\n" +
      "Before it starts, before I begin?\n" +
      "How long before you decide?\n" +
      "\n" +
      "Before I know what it feels like?\n";

    // Prior to Java 11, a Lambda function which negates the result of a method invocation on a single param could
    // NOT be reduced to a method reference, e.g.
    lyrics.lines().filter(s -> !s.isBlank());

    // From J11, the new Predicate.not(Predicate) method allows a method reference to be used in the aforementioned
    // scenario -
    List<String> filteredLyrics = lyrics.lines()
      .filter(Predicate.not(String::isBlank)) // Apply Predicate.not(Predicate)
      .collect(Collectors.toList());

    assertThat(filteredLyrics).hasSize(4);
    assertThat(filteredLyrics).doesNotContain("\n");
  }
}