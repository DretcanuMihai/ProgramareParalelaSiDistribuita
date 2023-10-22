package com.ppd.p1.service;

import com.ppd.p1.concurrent.ProcessingConditional;
import com.ppd.p1.model.dao.*;
import com.ppd.p1.model.dto.request.AppointmentRequest;
import com.ppd.p1.model.dto.request.PaymentRequest;
import com.ppd.p1.model.dto.response.AppointmentResponse;
import com.ppd.p1.model.dto.response.MessageResponse;
import com.ppd.p1.repository.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.ppd.p1.commons.Constants.*;

public class SuperService {

    private final AppointmentRepository appointmentRepository;

    private final LocationRepository locationRepository;

    private final LocTreatOfferRepository locTreatOfferRepository;

    private final PaymentRepository paymentRepository;

    private final TreatmentRepository treatmentRepository;

    private final LocationTreatmentOffer[][] offers;

    private final Map<Integer, Appointment> activeAppointments;

    private final ProcessingConditional processingConditional;

    private static final Logger LOGGER = LogManager.getLogger();

    public SuperService(AppointmentRepository appointmentRepository, LocationRepository locationRepository,
                        LocTreatOfferRepository locTreatOfferRepository, PaymentRepository paymentRepository,
                        TreatmentRepository treatmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.locationRepository = locationRepository;
        this.locTreatOfferRepository = locTreatOfferRepository;
        this.paymentRepository = paymentRepository;
        this.treatmentRepository = treatmentRepository;
        this.offers = new LocationTreatmentOffer[NR_LOCATIONS][NR_TREATMENTS];
        this.activeAppointments = new HashMap<>();
        this.processingConditional = new ProcessingConditional();
        initialize();
    }

    /**
     * this method blocks and validates the current state and writes logs into files
     */
    public void blockAndValidate() {
        processingConditional.blockNewStarts();
        processingConditional.awaitZero();
        validate();
        processingConditional.unblockNewStarts();
    }

    /**
     * makes an appointment described by a request
     *
     * @param appointmentRequest - the request used for creating the appointment
     * @return a response containing the id of the newly created appointment (the id is null if appointment failed)
     */
    public AppointmentResponse makeAppointment(AppointmentRequest appointmentRequest) {
        AppointmentResponse response = new AppointmentResponse();
        LocationTreatmentOffer offer = offers[appointmentRequest.getLocationId()][appointmentRequest.getTreatmentId()];

        Appointment appointment = requestToAppointment(appointmentRequest);
        appointment.setOffer(offer);
        appointment.setTimestamp(new Timestamp(System.currentTimeMillis()));
        appointment.setPaid(false);
        appointment.setCanceled(false);

        offer.lock();
        if (offer.canAddAppointment(appointment)) {
            processingConditional.notifyStart();
            appointment = appointmentRepository.create(appointment);
            offer.addAppointment(appointment);
            synchronized (activeAppointments) {
                activeAppointments.put(appointment.getId(), appointment);
            }
            processingConditional.notifyFinish();
            offer.unlock();

            response.setId(appointment.getId());
            response.setMessage("programare reusita");
        } else {
            offer.unlock();
            response.setId(null);
            response.setMessage("programare nereusita");
        }
        return response;
    }

    /**
     * pays for an appointment
     *
     * @param paymentRequest - a request containing the appointment to be paid
     * @return a response with a message that reflects the success
     */
    public MessageResponse payAppointment(PaymentRequest paymentRequest) {
        Integer id = paymentRequest.getAppointmentId();
        Appointment appointment;
        synchronized (activeAppointments) {
            appointment = activeAppointments.get(id);
        }
        appointment.lock();
        if (!appointment.isPaid()) {
            processingConditional.notifyStart();
            appointment.setPaid(true);
            appointmentRepository.update(appointment);

            Payment payment = new Payment();
            payment.setTimestamp(new Timestamp(System.currentTimeMillis()));
            payment.setCnp(appointment.getCnp());
            payment.setSum(appointment.getOffer().getTreatment().getPrice());
            payment.setLocation(appointment.getOffer().getLocation());
            paymentRepository.create(payment);

            processingConditional.notifyFinish();
            appointment.unlock();
            return OK_RESPONSE;
        } else {
            appointment.unlock();
            return ERROR_RESPONSE;
        }
    }

    /**
     * cancels an appointment
     *
     * @param paymentRequest - a request containing the appointment to be canceled
     * @return a response with a message that reflects the success
     */
    public MessageResponse cancelAppointment(PaymentRequest paymentRequest) {
        Integer id = paymentRequest.getAppointmentId();
        Appointment appointment;
        synchronized (activeAppointments) {
            appointment = activeAppointments.get(id);
        }
        appointment.lock();
        if (appointment.isPaid() && !appointment.isCanceled()) {
            processingConditional.notifyStart();

            Payment payment = new Payment();
            payment.setTimestamp(new Timestamp(System.currentTimeMillis()));
            payment.setCnp(appointment.getCnp());
            payment.setSum((-1) * appointment.getOffer().getTreatment().getPrice());
            payment.setLocation(appointment.getOffer().getLocation());
            paymentRepository.create(payment);

            appointment.setCanceled(true);

            appointmentRepository.delete(id);
            appointment.getOffer().lock();
            appointment.getOffer().deleteAppointment(appointment);
            appointment.getOffer().unlock();
            synchronized (activeAppointments) {
                activeAppointments.remove(id);
            }

            processingConditional.notifyFinish();
            appointment.unlock();
            return OK_RESPONSE;
        } else {
            appointment.unlock();
            return ERROR_RESPONSE;
        }
    }

    /**
     * initializes the super service, setting up locations, treatments and offers and logs
     */
    private void initialize() {
        initializeLocations();
        initializeTreatments();
        initializeOffers();
        initializeValidation();
    }

    /**
     * this method initializes the 5 locations
     */
    private void initializeLocations() {
        for (int i = 0; i < NR_LOCATIONS; i++) {
            Location location = new Location();
            location.setId(i);
            location.setPayments(new HashSet<>());
            locationRepository.create(location);
        }
    }

    /**
     * this method initializes the 5 treatments
     */
    private void initializeTreatments() {
        int[] prices = new int[]{50, 20, 40, 100, 30};
        int[] durations = new int[]{120, 20, 30, 60, 30};
        for (int i = 0; i < NR_TREATMENTS; i++) {
            Treatment treatment = new Treatment();
            treatment.setId(i);
            treatment.setPrice(prices[i]);
            treatment.setDuration(durations[i]);
            treatmentRepository.create(treatment);
        }
    }

    /**
     * this method initializes all the offers and populates the matrix of current offers.
     */
    private void initializeOffers() {
        int[] capacities = new int[]{3, 1, 1, 2, 1};
        List<Location> locations = locationRepository.readLocations();
        List<Treatment> treatments = treatmentRepository.readTreatments();
        for (int i = 0; i < NR_LOCATIONS; i++) {
            for (int j = 0; j < NR_TREATMENTS; j++) {
                LocationTreatmentOffer offer = new LocationTreatmentOffer();
                offer.setLocation(locations.get(i));
                offer.setTreatment(treatments.get(j));
                if (i == 0) {
                    offer.setCapacity(capacities[j]);
                } else {
                    offer.setCapacity(capacities[j] * i);
                }
                offer.setAppointments(new HashSet<>());
                offer = locTreatOfferRepository.create(offer);
                offers[i][j] = offer;
            }
        }
    }

    /**
     * initializes validation output file
     */
    private void initializeValidation(){
        try {
            File file = new File(VALIDATION_OUTPUT_FILE);
            if(!file.createNewFile()){
                Files.write(Paths.get(VALIDATION_OUTPUT_FILE), ("").getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            };
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this method validates the current state and writes logs into files
     */
    private void validate() {
        boolean isValid = true;
        logValidation("Validation");
        logValidation("\tTimestamp: " + new Timestamp(System.currentTimeMillis()));
        List<Location> locations = locationRepository.readLocations();
        logValidation("\tLocations: ");
        for (Location location : locations) {
            logValidation("\t\tLocation: " + location.getId());
            logValidation("\t\t\tTimestamp: " + new Timestamp(System.currentTimeMillis()));
            logValidation("\t\t\tUnpaid Appointments: ");
            int expectedSold = 0;
            for (int i = 0; i < NR_TREATMENTS; i++) {
                LocationTreatmentOffer offer = offers[location.getId()][i];
                AtomicReference<Integer> nrPaid = new AtomicReference<>(offer.getAppointments().size());
                offer.getAppointments().stream().filter(appointment -> !appointment.isPaid()).forEach(appointment -> {
                    logValidation("\t\t\t\tAppointment: " + appointment.getId());
                    nrPaid.getAndSet(nrPaid.get() - 1);
                });
                expectedSold += nrPaid.get() * offer.getTreatment().getPrice();
            }
            Integer actualSold = location.getPayments().stream().map(Payment::getSum).reduce(0, Integer::sum);
            logValidation("\t\t\tExpected Sold: " + expectedSold);
            logValidation("\t\t\tActual Sold: " + actualSold);
            if (!actualSold.equals(expectedSold)) {
                logValidation("DETECTED INVALID");
                isValid = false;
            }
            logValidation("\t\t\tTreatments: ");
            for (int i = 0; i < NR_TREATMENTS; i++) {
                logValidation("\t\t\t\tTreatment: " + i);
                LocationTreatmentOffer offer = offers[location.getId()][i];
                int[] schedule = offer.getSchedule();
                for (int j = 0; j < schedule.length; j++) {
                    logValidation("\t\t\t\t\tTime Unit: " + j);
                    logValidation("\t\t\t\t\t\tCapacity: " + schedule[j] + "/" + offer.getCapacity());
                    if (schedule[j] > offer.getCapacity()) {
                        logValidation("DETECTED INVALID");
                        isValid = false;
                    }
                }
            }
        }
        logValidation("\tValidation Status: " + (isValid ? "OK" : "INVALID"));
    }

    /**
     * transform a request to an appointment
     *
     * @param request said request
     * @return said appointment
     */
    private Appointment requestToAppointment(AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setName(request.getName());
        appointment.setCnp(request.getCnp());
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        return appointment;
    }

    private void logValidation(String message) {
        if (VALIDATION_LOGGING_ENABLED) {
//            LOGGER.info(message);
            writeToValidationFile(message);
        }
    }

    private void writeToValidationFile(String message) {
        try {
            Files.write(Paths.get(VALIDATION_OUTPUT_FILE), (message + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
