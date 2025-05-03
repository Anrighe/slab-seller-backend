package utils;

import repository.model.PasswordRecoveryRequestEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PasswordRecoveryRequestUtil {


    /**
     * Checks if any of the password recovery request is valid.
     * @param passwordRecoveryRequests A collection of PasswordRecoveryRequestEntity objects to check
     * @return true if at least one password recovery request is valid, false otherwise
     */
    public static boolean isAnyPasswordRecoveryRequestStillValid(Collection<PasswordRecoveryRequestEntity> passwordRecoveryRequests) {
        for (PasswordRecoveryRequestEntity passwordRecoveryRequest : passwordRecoveryRequests) {
            if (passwordRecoveryRequest.isValid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filter any valid password recovery request
     * @param passwordRecoveryRequests A collection of PasswordRecoveryRequestEntity objects to check
     * @return all the valid password recovery request; an empty list if none is found
     */
    public static List<PasswordRecoveryRequestEntity> filterValidPasswordRecoveryRequests(Collection<PasswordRecoveryRequestEntity> passwordRecoveryRequests) {
        List<PasswordRecoveryRequestEntity> filteredPasswordRecoveryRequests = new ArrayList<>();
        for (PasswordRecoveryRequestEntity passwordRecoveryRequest : passwordRecoveryRequests) {
            if (passwordRecoveryRequest.isValid()) {
                filteredPasswordRecoveryRequests.add(passwordRecoveryRequest);
            }
        }
        return filteredPasswordRecoveryRequests;
    }

    /**
     * Checks if any of the password recovery request are in timeout by confronting the send date with the specified timeout (in seconds)
     * @param passwordRecoveryRequests password recovery requests
     * @param timeoutSeconds number of seconds of the timeout
     * @return true if the email has some requests which still need to expire, false otherwise
     */
    public static boolean isAnyPasswordRecoveryRequestInTimeout(final Collection<PasswordRecoveryRequestEntity> passwordRecoveryRequests, final int timeoutSeconds) {
        for (PasswordRecoveryRequestEntity passwordRecoveryRequest : passwordRecoveryRequests) {
            LocalDateTime sendTime = passwordRecoveryRequest.getSendTime();
            if (LocalDateTime.now().isBefore((sendTime.plusSeconds(timeoutSeconds)))) {
                return true;
            }
        }
        return false;
    }
}
