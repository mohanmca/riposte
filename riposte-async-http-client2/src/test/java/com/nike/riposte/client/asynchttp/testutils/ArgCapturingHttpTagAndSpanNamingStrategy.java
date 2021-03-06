package com.nike.riposte.client.asynchttp.testutils;

import com.nike.wingtips.Span;
import com.nike.wingtips.tags.HttpTagAndSpanNamingAdapter;
import com.nike.wingtips.tags.HttpTagAndSpanNamingStrategy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper class that gives you a {@link HttpTagAndSpanNamingStrategy} that lets you know when methods are called
 * and what the args were. This is necessary because the entry methods to {@link HttpTagAndSpanNamingStrategy}
 * are final, so they can't be mocked.
 *
 * @author Nic Munroe
 */
public class ArgCapturingHttpTagAndSpanNamingStrategy<REQ, RES>
    extends HttpTagAndSpanNamingStrategy<REQ, RES> {

    private final AtomicReference<String> initialSpanName;
    private final AtomicBoolean initialSpanNameMethodCalled;
    private final AtomicBoolean requestTaggingMethodCalled;
    private final AtomicBoolean responseTaggingAndFinalSpanNameMethodCalled;
    private final AtomicReference<InitialSpanNameArgs<REQ>> initialSpanNameArgs;
    private final AtomicReference<RequestTaggingArgs<REQ>> requestTaggingArgs;
    private final AtomicReference<ResponseTaggingArgs<REQ, RES>> responseTaggingArgs;

    public ArgCapturingHttpTagAndSpanNamingStrategy(
        AtomicReference<String> initialSpanName,
        AtomicBoolean initialSpanNameMethodCalled,
        AtomicBoolean requestTaggingMethodCalled,
        AtomicBoolean responseTaggingAndFinalSpanNameMethodCalled,
        AtomicReference<InitialSpanNameArgs<REQ>> initialSpanNameArgs,
        AtomicReference<RequestTaggingArgs<REQ>> requestTaggingArgs,
        AtomicReference<ResponseTaggingArgs<REQ, RES>> responseTaggingArgs
    ) {
        this.initialSpanName = initialSpanName;
        this.initialSpanNameMethodCalled = initialSpanNameMethodCalled;
        this.requestTaggingMethodCalled = requestTaggingMethodCalled;
        this.responseTaggingAndFinalSpanNameMethodCalled = responseTaggingAndFinalSpanNameMethodCalled;
        this.initialSpanNameArgs = initialSpanNameArgs;
        this.requestTaggingArgs = requestTaggingArgs;
        this.responseTaggingArgs = responseTaggingArgs;
    }

    @Override
    protected @Nullable String doGetInitialSpanName(
        @NotNull REQ request, @NotNull HttpTagAndSpanNamingAdapter<REQ, ?> adapter
    ) {
        initialSpanNameMethodCalled.set(true);
        initialSpanNameArgs.set(new InitialSpanNameArgs(request, adapter));
        return initialSpanName.get();
    }

    @Override
    protected void doHandleResponseAndErrorTagging(
        @NotNull Span span,
        @Nullable REQ request,
        @Nullable RES response,
        @Nullable Throwable error,
        @NotNull HttpTagAndSpanNamingAdapter<REQ, RES> adapter
    ) {
        responseTaggingAndFinalSpanNameMethodCalled.set(true);
        responseTaggingArgs.set(
            new ResponseTaggingArgs(span, request, response, error, adapter)
        );
    }

    @Override
    protected void doHandleRequestTagging(
        @NotNull Span span,
        @NotNull REQ request,
        @NotNull HttpTagAndSpanNamingAdapter<REQ, ?> adapter
    ) {
        requestTaggingMethodCalled.set(true);
        requestTaggingArgs.set(new RequestTaggingArgs(span, request, adapter));
    }

    public static class InitialSpanNameArgs<REQ> {

        public final REQ request;
        public final HttpTagAndSpanNamingAdapter adapter;

        private InitialSpanNameArgs(
            REQ request, HttpTagAndSpanNamingAdapter adapter
        ) {
            this.request = request;
            this.adapter = adapter;
        }

        public void verifyArgs(REQ expectedRequest, HttpTagAndSpanNamingAdapter expectedAdapter) {
            assertThat(request).isSameAs(expectedRequest);
            assertThat(adapter).isSameAs(expectedAdapter);
        }
    }

    public static class RequestTaggingArgs<REQ> {

        public final Span span;
        public final REQ request;
        public final HttpTagAndSpanNamingAdapter adapter;

        private RequestTaggingArgs(
            Span span, REQ request, HttpTagAndSpanNamingAdapter adapter
        ) {
            this.span = span;
            this.request = request;
            this.adapter = adapter;
        }

        public void verifyArgs(
            Span expectedSpan, REQ expectedRequest, HttpTagAndSpanNamingAdapter expectedAdapter
        ) {
            assertThat(span).isSameAs(expectedSpan);
            assertThat(request).isSameAs(expectedRequest);
            assertThat(adapter).isSameAs(expectedAdapter);
        }
    }

    public static class ResponseTaggingArgs<REQ, RES> {

        public final Span span;
        public final REQ request;
        public final RES response;
        public final Throwable error;
        public final HttpTagAndSpanNamingAdapter adapter;

        private ResponseTaggingArgs(
            Span span, REQ request, RES response, Throwable error,
            HttpTagAndSpanNamingAdapter adapter
        ) {
            this.span = span;
            this.request = request;
            this.response = response;
            this.error = error;
            this.adapter = adapter;
        }

        public void verifyArgs(
            Span expectedSpan, REQ expectedRequest, RES expectedResponse,
            Throwable expectedError, HttpTagAndSpanNamingAdapter expectedAdapter
        ) {
            assertThat(span).isSameAs(expectedSpan);
            assertThat(request).isSameAs(expectedRequest);
            assertThat(response).isSameAs(expectedResponse);
            assertThat(error).isSameAs(expectedError);
            assertThat(adapter).isSameAs(expectedAdapter);
        }
    }
}
