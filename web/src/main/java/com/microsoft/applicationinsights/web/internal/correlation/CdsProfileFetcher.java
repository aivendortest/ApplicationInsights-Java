/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.web.internal.correlation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

public enum CdsProfileFetcher implements AppProfileFetcher {

    INSTANCE;

    private HttpAsyncClient httpClient;

    // cache of tasks per ikey
    private final ConcurrentHashMap<String, Future<HttpResponse>> tasks;

    CdsProfileFetcher() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(3000)
            .setConnectTimeout(3000).build();

        this.httpClient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

        this.tasks = new ConcurrentHashMap<String, Future<HttpResponse>>();
    }

	@Override
	public ProfileFetcherResult fetchAppProfile(String instrumentationKey) throws InterruptedException, ExecutionException {

        ProfileFetcherResult result = new ProfileFetcherResult(null, ProfileFetcherResultTaskStatus.PENDING);
        Future<HttpResponse> currentTask = this.tasks.get(instrumentationKey);

        // if no task currently exists for this ikey, then let's create one.
        if (currentTask == null) {
            currentTask = createFetchTask(instrumentationKey);
            this.tasks.putIfAbsent(instrumentationKey, currentTask);
        }

        // check if task is still pending
        if (!currentTask.isDone()) {
            return result;
        }

        // task is ready, we can call get() now.
        HttpResponse response = currentTask.get();

        // get json payload out of response and extract appId.

		return new ProfileFetcherResult(null, ProfileFetcherResultTaskStatus.FAILED);
    }

	public void setHttpClient(HttpAsyncClient client) {
        this.httpClient = client;
    }

    private Future<HttpResponse> createFetchTask(String instrumentationKey) {
		return null;
	}
}