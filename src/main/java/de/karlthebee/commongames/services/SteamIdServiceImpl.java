package de.karlthebee.commongames.services;

import de.karlthebee.commongames.services.interfaces.SteamIdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SteamIdServiceImpl implements SteamIdService {

    private static final BigInteger MAX_ULONG64 = new BigInteger("2").pow(64);

    @Override
    public String prepareId(String id) {
        //Check if URL is passed and extract
        if (id.contains("/profiles/")) {
            id = id.substring(id.indexOf("/profiles/") + 10);
        } else if (id.contains("/id/")) {
            id = id.substring(id.indexOf("/id/") + 4);
        }

        if (id.endsWith("/"))   //Remove last "/" of URL
            id = id.substring(0, id.length() - 1);
        return id;
    }

    @Override
    public boolean isId64(String id) {
        if (id.contains("/id/"))
            return false;
        var preparedId = prepareId(id);
        try {
            //Is 64bit steam id?
            var idBI = new BigInteger(preparedId);
            if (idBI.compareTo(MAX_ULONG64) <= 0)
                return true;
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isValidId(String id) {
        var preparedId = prepareId(id);

        //When numeric, check ID64 length
        if (isId64(id)) {
            //All IDs are 17 digits long, see:https://stackoverflow.com/questions/33933705/steamid64-minimum-and-maximum-length
            return preparedId.length() == 17;
        }


        //Check if username is only alphanumeric
        return Pattern.compile("^[a-zA-Z0-9]+$").matcher(preparedId).matches();
    }
}
