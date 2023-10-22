package com.ppd.p1.commons;

import com.ppd.p1.model.dto.response.MessageResponse;

public class Constants {

    public static final String SERVER_HOST = "localhost";

    public static final int SERVER_PORT = 55556;

    private static final long RUNNING_DURATION_MINUTES = 3;

    public static final long RUNNING_DURATION = RUNNING_DURATION_MINUTES * 60 * 1000L;

    public static final long VALIDATION_INTERVAL_NR_SECONDS = 10;

    public static final long VALIDATION_INTERVAL = VALIDATION_INTERVAL_NR_SECONDS * 1000L;

    public static final long REQUEST_INTERVAL_NR_SECONDS = 2;

    public static final long REQUEST_INTERVAL = REQUEST_INTERVAL_NR_SECONDS * 1000L;

    public static final int SERVER_THREADS = 10;

    public static final int NR_CLIENTS = 10;

    public static final MessageResponse OK_RESPONSE = new MessageResponse("ok");

    public static final MessageResponse ERROR_RESPONSE = new MessageResponse("error");

    public static final int NR_LOCATIONS = 5;

    public static final int NR_TREATMENTS = 5;

    public static final int NR_TIME_UNITS = 8 * 6; //8 hours, 6 tens in an hour

    public static boolean CLIENT_LOGGING_ENABLED = false;

    public static boolean SERVER_LOGGING_ENABLED = false;

    public static boolean SERVER_HANDLER_LOGGING_ENABLED = false;

    public static boolean VALIDATION_LOGGING_ENABLED = true;

    public static String VALIDATION_OUTPUT_FILE = "target/validation.log";
}
