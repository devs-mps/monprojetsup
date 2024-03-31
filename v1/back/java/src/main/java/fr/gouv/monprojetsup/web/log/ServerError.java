package fr.gouv.monprojetsup.web.log;

import java.time.LocalDateTime;

public record ServerError(String timestamp, String origin, String error)
{
    public ServerError(String error, String origin) {
        this(LocalDateTime.now().toString(),
                origin,
                error); }
}
