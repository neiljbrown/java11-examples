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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A JUnit test case providing examples of the new HTTP client library/API that was finalised in JDK 11.
 *
 * <h2>A Significant Improvement over Previous JDK HTTP Client - HttpUrlConnection</h2>
 * <p>
 * The new HTTP client is a significant improvement over the use of {@link java.net.HttpURLConnection} (which due to its
 * numerous limitations wasn't really a viable option for production usage), offering the following major benefits  -
 * <ul>
 * <li>Simpler API and Improved Readability - A fluent API makes constructing requests and processing responses
 * simpler to code, easier to read and less verbose. Use of the Builder pattern instead of setter methods also
 * increases thread-safety of the client and its requests/responses.</li>
 * <li>Support for modern Java APIs - The new HTTP client supports using modern Java (8+) language features and APIs
 * including e.g. Lambda expressions; Optional type; and the Date and Time API,  etc.</li>
 * <li>Non-blocking I/O - The new HTTP client supports executing requests without blocking the current thread via
 * the use of an async API/callbacks, which utilise {@link java.util.concurrent.CompletableFuture}</li>
 * <li>Protocol support - In addition to HTTP/1.1, the new HTTP client also provides support for HTTP/2 (the default,
 * with automatic negotiation and fallback to HTTP/1.1) and WebSockets.</li>
 * </ul>
 * As a result of these improvements, the new HTTP client may well provide a viable production alternative to using 3rd
 * party libraries such as Apache HttpComponents. although that will depend on it how well it supports more advanced
 * features such as connection pooling, timeouts, etc. (TBC).
 *
 * <h2>Overview of HTTP Client API</h2>
 * The new HTTP client is contained in JDK module/package {@link java.net.http}. It consists of the following main
 * classes and interfaces:
 * <ul>
 * <li>{@link java.net.http.HttpClient} - The entry point for using the API.</li>
 * <li>{@link java.net.http.HttpRequest} - A request to be sent via the HttpClient.</li>
 * <li>{@link java.net.http.HttpResponse} - The result of an HttpRequest call.</li>
 * <li>{@link java.net.http.WebSocket} - The entry point for setting up a WebSocket client.</li>
 * </ul>
 * For more details of the API see the
 * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html">HTTP Client API documentation</a>.
 *
 * <h2>Further Reading</h2>
 * <a href="https://openjdk.java.net/groups/net/httpclient/intro.html">Introduction to the Java HTTP Client, OpenJDK Project </a> -
 * Provides a nice overview of the new HTTP client.
 */
public class HttpClientTest {

  private static final String GOOGLE_URL_STRING = "https://www.google.co.uk";

  private HttpClient httpClient;
  private boolean useDefaultHttpClient;

  @BeforeEach
  void setUp() {
    this.httpClient = createHttpClient();
    this.useDefaultHttpClient = Boolean.parseBoolean(System.getProperty("HttpClientTest.useDefaultHttpClient", "true"));
  }

  /**
   * Provides examples of how to create an {@link HttpClient} using its static factory method or builder.
   *
   * @return the created {@link HttpClient}.
   */
  private HttpClient createHttpClient() {
    if (useDefaultHttpClient) {
      // Use static factory  method to create HttpClient with default settings, covering e.g. HTTP request method,
      // protocol version, redirect policy etc. (Equivalent to using builder - HttpClient.newBuilder().build()).
      return HttpClient.newHttpClient();
    } else {
      // Use HttpClient builder to create an HTTP client with non-default, desired settings.
      return HttpClient.newBuilder()
        // Set HTTP protocol version - In this case, not strictly necessary as by default HttpClient tries to upgrade
        // to HTTP/2 if server supports it, else falls back to HTTP/1.1
        .version(HttpClient.Version.HTTP_1_1)
        // Set redirect policy
        .followRedirects(HttpClient.Redirect.NORMAL)
        // Set a connection timeout.
        // NOTE - There isn't currently an option to set a default read timeout (at least not via the builder) for
        // the HttpClient, although one can be set per request.
        .connectTimeout(Duration.ofMillis(1000))
        // Proxy - The client can also be configured to use a proxy via the proxy(...) method.
        // etc.
        .build();
    }
  }

  /**
   * Provides a basic example of using {@link HttpClient} to make a (blocking) HTTP GET request, including building
   * the HTTP request, sending the request, and processing the HTTP response, including retrieving the status code, a
   * named header, and deserialising the response body to string.
   *
   * @throws Exception if an unexpected error occurs.
   * @see #testAsyncGetRequest()
   */
  @Test
  public void testGetRequest() throws Exception {
    var request = HttpRequest.newBuilder()
      .uri(URI.create(GOOGLE_URL_STRING))
      // HTTP method defaults to GET if not specified
      .build();

    HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertHttpResponseStatusCodeInSuccessRange(response);
    assertThat(response.headers().firstValue("content-type")).isPresent().isNotEmpty();
    assertThat(response.body()).isNotEmpty();
  }

  /**
   * Provides an example of the support that {@link HttpClient} provides for executing requests asynchronously in a
   * non-blocking fashion using {@link HttpClient#sendAsync(HttpRequest, HttpResponse.BodyHandler)}. The method
   * returns a {@link CompletableFuture} which can be used to compose processing of the response in a separate thread.
   *
   * @throws Exception if an unexpected error occurs.
   * @see #testGetRequest()
   */
  @Test
  public void testAsyncGetRequest() throws Exception {
    var request = HttpRequest
      .newBuilder(URI.create(GOOGLE_URL_STRING)) // Overloaded version of HttpRequest.newBuilder() accepts a URI
      // HTTP method defaults to GET if not specified
      .build();

    // Use HttpClient.sendAsync(...) method to execute request without blocking. Returns response as CompletableFuture.
    CompletableFuture<HttpResponse<String>> futureHttpResponse =
      this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    // Chain processing of future HttpResponse without blocking (when it's returned), subject to time-out.
    futureHttpResponse
      .thenApply(HttpResponse::body)
      .thenAccept(body ->
        System.out.println("testAsyncGetRequest() - Received response body [" + body.substring(0,80) + "...]."))
      .orTimeout(1000, TimeUnit.MILLISECONDS)
      .join();

    // Alternatively, block on HttpResponse
    HttpResponse<String> response = futureHttpResponse.get(1000, TimeUnit.MILLISECONDS);

    assertHttpResponseStatusCodeInSuccessRange(response);
    assertThat(response.headers().firstValue("content-type")).isPresent().isNotEmpty();
    assertThat(response.body()).isNotEmpty();
  }

  /**
   * Provides an example of making a POST request with a non-empty body read from a string.
   * <p>
   * Builds the HTTP request from a specified HTTP method. Reads the body to be sent from one of a number of
   * implementations of the {@link HttpRequest.BodyPublisher} interface that are provided by static class
   * {@link HttpRequest.BodyPublishers} (plural). This example uses {@link HttpRequest.BodyPublishers#ofString(String)}.
   *
   * @throws Exception if an unexpected error occurs.
   * @see #testPostRequestWhenBodyPublisherOfInputStream()
   */
  @Test
  public void testPostRequestWhenBodyPublisherOfString() throws Exception {
    var request = HttpRequest.newBuilder(URI.create("https://httpbin.org/post"))
      .header("Content-Type", "application/json")
      // Specify HTTP method to use
      .POST(
        // Read /consume the request body from a supplied string
        HttpRequest.BodyPublishers.ofString("{\"message\" : \"testPostRequest() - Hello world !\"}",
          StandardCharsets.UTF_8))
      .build();

    HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertHttpResponseStatusCodeInSuccessRange(response);
  }

  /**
   * Provides an example of making a POST request with a non-empty body read from an {@link InputStream}.
   * <p>
   * Similar to {@link #testPostRequestWhenBodyPublisherOfString()}, except that shows how to read the HTTP
   * request body using an alternative implementation of {@link HttpRequest.BodyPublisher} provided by
   * {@link HttpRequest.BodyPublishers#ofInputStream(Supplier)}
   *
   * @throws Exception if an unexpected error occurs.
   * @see #testPostRequestWhenBodyPublisherOfString()
   */
  @Test
  public void testPostRequestWhenBodyPublisherOfInputStream() throws Exception {
    var request = HttpRequest.newBuilder(URI.create("https://httpbin.org/post"))
      .header("Content-Type", "application/json")
      .POST(
        // Read /consume the request body from a specified InputStream
        HttpRequest.BodyPublishers.ofInputStream(() ->
          HttpClientTest.class.getResourceAsStream("/testPostRequest-data.json")))
      .build();

    HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertHttpResponseStatusCodeInSuccessRange(response);
  }

  /**
   * Provides an example of current support for setting a per request response timeout using
   * {@link HttpRequest.Builder#timeout(Duration)}.
   * <p>
   * The documented behaviour of {@link HttpRequest.Builder#timeout(Duration)} is a little ambiguous, with respect to
   * scope of the timeout. Given that a default connect timeout can be set using
   * {@link HttpClient.Builder#connectTimeout(Duration)} I'd expected {@link HttpRequest.Builder#timeout(Duration)}
   * to specify a _read_ timeout. However, as this test shows, the timeout appears to be a total timeout including
   * the time to connect, because when the value is set to a very low value a {@link HttpConnectTimeoutException}
   * is being thrown rather than the more generic {@link HttpTimeoutException}.
   */
  @Test
  public void testResponseTimeout() {
    var request = HttpRequest.newBuilder(URI.create(GOOGLE_URL_STRING))
      // Set a very low, unachievable 'timeout' for the response
      .timeout(Duration.ofMillis(1)).build();

    assertThatThrownBy(() -> this.httpClient.send(request, HttpResponse.BodyHandlers.ofString()))
      // As per method comments, the request fails reporting a _connection_ timeout, suggesting the configured
      // timeout is NOT a _read_ timeout, but probably a timeout for the total response including connecting.
      .isInstanceOf(HttpConnectTimeoutException.class);
  }

  private void assertHttpResponseStatusCodeInSuccessRange(HttpResponse<?> response) {
    assertThat(response.statusCode()).isBetween(200, 299);
  }
}