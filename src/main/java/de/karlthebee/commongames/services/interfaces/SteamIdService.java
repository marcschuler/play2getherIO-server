package de.karlthebee.commongames.services.interfaces;

public interface SteamIdService {

    /**
     * Prepares the input, removing all
     * @param id the raw input ID
     * @return
     */
    String prepareId(String id);

    /**
     *
     * @param id the unprepared id
     * @return true if this is the steam-internal 64bit id
     */
    boolean isId64(String id);

    /**
     * Checks if the id __MIGHT__ be valid
     * @param id the unprepared id
     * @return true if there is a chance that user exists, false otherwise
     */
    boolean isValidId(String id);
}
