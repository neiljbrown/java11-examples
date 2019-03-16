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

import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * A JUnit test case providing examples of several new methods added to the {@link String} API in Java 11.
 */
public class StringTest {

  /**
   * The String class has finally got a method for checking whether a string is blank {@link String#isBlank()}, which
   * in addition to checking if a string is empty (as per existing method {@link String#isEmpty()}) also checks if the
   * string contains only whitespace characters. One less reason to need a 3rd party String utility library.
   */
  @Test
  public void testIsBlank() {
    final String nonEmptyBlankString = " ";
    assertThat(nonEmptyBlankString.isBlank()).isTrue();
    assertThat(nonEmptyBlankString.isEmpty()).isFalse();

    final String emptyString = "";
    assertThat(emptyString.isBlank()).isTrue();
    assertThat(emptyString.isEmpty()).isTrue();

    final String nonEmptyNonBlankString = "foo";
    assertThat(nonEmptyNonBlankString.isBlank()).isFalse();
    assertThat(nonEmptyNonBlankString.isEmpty()).isFalse();
  }

  /**
   * The String class has got a new method for removing leading and trailing whitespace from a string -
   * {@link String#strip()}. This test shows an example of its use and how it differs from {@link String#trim()}.
   * <p>
   * The new {@link String#strip()} method differs from the older method {@link String#trim()} in respect to what
   * characters are classified as whitespace. The older {@link String#trim()} method removes whitespace characters
   * defined by {@link Character#isWhitespace(char)} which are limited to those less than or equal to code point
   * (\u0020), commonly referred to as ASCII or ISO control characters. The new {@link String#strip()} method removes
   * whitespace characters defined by {@link Character#isWhitespace(int)} which includes other/all whitespace
   * characters defined in the Unicode character set. For more details see
   * <br>
   * https://stackoverflow.com/questions/51266582/difference-between-string-trim-and-strip-methods-in-java-11/51266583#51266583
   * <br>
   * https://bugs.openjdk.java.net/browse/JDK-8200378
   * <p>
   * The working assumption is therefore that typically {@link String#strip()} is viewed as a fix to, and should be used
   * in preference to {@link String#trim()}, at least in new code.
   *
   * @see #testStripLeading()
   * @see #testStripTrailing()
   */
  @Test
  public void testStripSimilarityAndDifferenceToTrim() {
    final String msg = "Hello World!";

    final String leadingAsciiWhitespace = "\t ";
    final String trailingAsciiWhitespace = " \n";
    final String msgWithLeadingAndTrailingAsciiWhitespace = leadingAsciiWhitespace + msg + trailingAsciiWhitespace;
    assertThat(msgWithLeadingAndTrailingAsciiWhitespace.strip()).isEqualTo(msg);
    assertThat(msgWithLeadingAndTrailingAsciiWhitespace.trim()).isEqualTo(msg);

    final Character nonAsciiUnicodeWhitespace = '\u2000';
    final String msgWithLeadingAndTrailingNonAsciiUnicodeWhitespace =
      nonAsciiUnicodeWhitespace + msg + nonAsciiUnicodeWhitespace;
    assertThat(msgWithLeadingAndTrailingNonAsciiUnicodeWhitespace.strip()).isEqualTo(msg);
    assertThat(msgWithLeadingAndTrailingNonAsciiUnicodeWhitespace.trim()).isNotEqualTo(msg);
    assertThat(msgWithLeadingAndTrailingNonAsciiUnicodeWhitespace.trim()).isEqualTo(msgWithLeadingAndTrailingNonAsciiUnicodeWhitespace);
  }

  /**
   * The String class has got a new method for trimming whitespace from the beginning of a string only, so-called
   * indentation whitespace - {@link String#stripLeading()}. Uses the same extended definition of whitespace
   * characters as the new {@link String#strip()} (see {@link #testStripSimilarityAndDifferenceToTrim()}.
   *
   * @see #testStripSimilarityAndDifferenceToTrim()
   * @see #testStripTrailing()
   */
  @Test
  public void testStripLeading() {
    final String msg = "Hello World!";
    final String whitespace = "\n\r ";
    final String msgWithLeadingAndTrailingWhitespace = whitespace + msg + whitespace;

    assertThat(msgWithLeadingAndTrailingWhitespace.stripLeading()).isEqualTo(msg+whitespace);
  }

  /**
   * The String class has got a new method for trimming whitespace from the end (only) of a string -
   * {@link String#stripTrailing()}. Uses the same extended definition of whitespace characters as the new
   * {@link String#strip()} (see {@link #testStripSimilarityAndDifferenceToTrim()}.
   *
   * @see #testStripSimilarityAndDifferenceToTrim()
   * @see #testStripLeading()
   */
  @Test
  public void testStripTrailing() {
    final String msg = "Hello World!";
    final String whitespace = "\n\r ";
    final String msgWithLeadingAndTrailingWhitespace = whitespace + msg + whitespace;

    assertThat(msgWithLeadingAndTrailingWhitespace.stripTrailing()).isEqualTo(whitespace+msg);
  }

  /**
   * The String class has got a new method to repeatedly concatenate its characters a specified no. of times -
   * {@link String#repeat(int)}. The assumption is that the method performs better than using
   * {@link StringBuilder#append(String)} in a loop.
   */
  @Test
  public void testRepeat() {
    assertThat("!".repeat(5)).isEqualTo("!!!!!");
  }

  /**
   * The new {@link String#lines()} method supports processing a multi-line string. It returns a
   * {@link java.util.stream.Stream} of lines split by a small no. of different line terminators (e.g. CR, LF and
   * CR/LF), plus the end of string.
   * <p>
   * This new method's API is similar to / consistent from method {@link BufferedReader#lines()} which can be used to
   * read lines (records delimited by line terminators) from a file.
   */
  @Test
  public void testLines() {
    final String rhyme = "Twinkle, twinkle, little star,\n" +
      "How I wonder what you are.\r" +
      "Up above the world so high,\r\n" +
      "Like a diamond in the sky.\n" +
      "Twinkle, twinkle, little star,\r" +
      "How I wonder what you are!";

    List<String> lines = rhyme.lines().collect(Collectors.toList());

    assertThat(lines).hasSize(6);
    assertThat(lines).startsWith("Twinkle, twinkle, little star,");
    assertThat(lines).endsWith("How I wonder what you are!");
    assertThat(lines).doesNotContain("\n", "\r", "\r\n");
  }
}