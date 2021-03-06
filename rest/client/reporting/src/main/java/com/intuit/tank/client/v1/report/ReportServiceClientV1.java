/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.client.v1.report;

/*
 * #%L
 * Reporting Rest Client
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.time.FastDateFormat;

import com.intuit.tank.api.service.v1.report.ReportService;
import com.intuit.tank.reporting.api.TPSInfoContainer;
import com.intuit.tank.reporting.api.TPSReportingPackage;
import com.intuit.tank.rest.BaseRestClient;
import com.intuit.tank.rest.RestServiceException;
import com.intuit.tank.rest.util.ServiceConsants;
import com.intuit.tank.results.TankResult;
import com.intuit.tank.results.TankResultPackage;

/**
 * ProjectClientV1
 * 
 * @author dangleton
 * 
 */
public class ReportServiceClientV1 extends BaseRestClient {

    private static final FastDateFormat FMT = FastDateFormat.getInstance(ReportService.DATE_FORMAT);
    private static final String SERVICE_BASE_URL = ServiceConsants.REST_SERVICE_CONTEXT
            + ReportService.SERVICE_RELATIVE_PATH;

    /**
     * 
     * @param serviceUrl
     */
    public ReportServiceClientV1(String serviceUrl) {
        super(serviceUrl, null, null);
    }

    /**
     * 
     * @param serviceUrl
     */
    public ReportServiceClientV1(String serviceUrl, final String proxyServer, final Integer proxyPort) {
        super(serviceUrl, proxyServer, proxyPort);
    }

    /**
     * 
     * @return
     */
    protected String getServiceBaseUrl() {
        return SERVICE_BASE_URL;
    }

    /**
     * 
     * @param jobId
     * @param instanceId
     * @param container
     * @throws RestServiceException
     * @throws UniformInterfaceException
     */
    public void postTpsResults(String jobId, String instanceId, TPSInfoContainer container)
            throws RestServiceException {
        TPSReportingPackage tpsPackage = new TPSReportingPackage(jobId, instanceId, container);
        WebTarget webTarget = client.target(urlBuilder.buildUrl(ReportService.METHOD_TPS_INFO));
        Response response = webTarget.request().post(Entity.entity(tpsPackage, MediaType.APPLICATION_XML_TYPE));
        exceptionHandler.checkStatusCode(response);
    }

    /**
     * 
     * @param jobId
     * @param instanceId
     * @param results
     * @throws RestServiceException
     * @throws UniformInterfaceException
     */
    public void postTimingResults(String jobId, String instanceId, List<TankResult> results)
            throws RestServiceException {
        TankResultPackage tankResultPackage = new TankResultPackage(jobId, instanceId, results);
        WebTarget webTarget = client.target(urlBuilder.buildUrl(ReportService.METHOD_TIMING_RESULTS));
        Response response = webTarget.request().post(Entity.entity(tankResultPackage, MediaType.APPLICATION_XML_TYPE));
        exceptionHandler.checkStatusCode(response);
    }

    /**
     * Gets the csv data stream.
     * 
     * @param jobId
     *            the jobId of the timing data to get
     * @param period
     *            the requested period for timing data. can be 15, 30, 45, or 60. If null period is system set default
     *            15
     * @param minDate
     *            the minimumDate inclusive. if null no minimum is set
     * @param maxDate
     *            the maximum date exclusive. if null no max is set
     * @return stream of csv data in format of "Job ID", "Page ID", "Sample Size", "Average", "Min", "Max", "Period",
     *         "Start Time" first row is header row. If empty stream no results are returned.
     */
    public InputStream getBucketTimingData(@Nonnull String jobId, @Nullable Integer period, @Nullable Date minDate,
            Date maxDate) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(ReportService.METHOD_TIMING_PERIODIC_CSV, jobId));
        if (minDate != null) {
            uriBuilder.queryParam("minTime", FMT.format(minDate));
        }
        if (maxDate != null) {
            uriBuilder.queryParam("maxTime", FMT.format(maxDate));
        }
        if (period != null && period != 15) {
            uriBuilder.queryParam("period", period.toString());
        }
        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request(MediaType.APPLICATION_OCTET_STREAM).get();
        exceptionHandler.checkStatusCode(response);
        return response.readEntity(InputStream.class);
    }

    /**
     * Gets the contents of a file as a Stream starting at the specified start point
     * 
     * @param filePath
     *            the filePath to fetch as a child of the logs dir.
     * @param start
     *            the number of bytes to skip. Pass null or 0L to get entire file.
     * @return the stream of the file
     */
    public InputStream getFile(String filePath, Long start) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(filePath));
        if (start != null) {
            uriBuilder.queryParam("from", start.toString());
        }
        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request(MediaType.APPLICATION_OCTET_STREAM).get();
        exceptionHandler.checkStatusCode(response);
        return response.readEntity(InputStream.class);
    }

    /**
     * Triggers processing the summary data for a job
     * 
     * @param jobId
     */
    public void processSummary(String jobId) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(ReportService.METHOD_PROCESS_TIMING, jobId));

        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request(MediaType.TEXT_PLAIN).get();
        exceptionHandler.checkStatusCode(response);
    }

    /**
     * Gets the timing data as csv file.
     * 
     * @param jobId
     *            the job to get the data for
     * @return InputStream or throw exception if no data found.
     */
    public InputStream getTimingCsv(String jobId) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(ReportService.METHOD_TIMING_CSV, jobId));
        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request(MediaType.APPLICATION_OCTET_STREAM).get();
        exceptionHandler.checkStatusCode(response);
        return response.readEntity(InputStream.class);
    }

    /**
     * Gets the summary data as a csv file.
     * 
     * @param jobId
     * @return InputStream or throw exception if no data found.
     */
    public InputStream getSummaryTimingCsv(String jobId) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(ReportService.METHOD_TIMING_SUMMARY_CSV, jobId));
        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request(MediaType.APPLICATION_OCTET_STREAM).get();
        exceptionHandler.checkStatusCode(response);
        return response.readEntity(InputStream.class);
    }

    /**
     * Deletes the raw timing data from storage.
     * 
     * @param jobId
     *            the job to delete data for
     */
    public void deleteTiming(String jobId) {
        UriBuilder uriBuilder = UriBuilder
                .fromUri(urlBuilder.buildUrl(ReportService.METHOD_TIMING, jobId));
        WebTarget webTarget = client.target(uriBuilder.build());
        Response response = webTarget.request().delete();
        exceptionHandler.checkStatusCode(response);
    }

}
