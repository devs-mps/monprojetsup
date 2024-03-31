package fr.gouv.monprojetsup.web.services.info;

import fr.gouv.monprojetsup.web.server.WebServer;
import fr.gouv.monprojetsup.web.db.model.Lycee;
import fr.gouv.monprojetsup.tools.server.MyService;
import fr.gouv.monprojetsup.tools.server.ResponseHeader;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetLyceesService extends MyService<MyService.EmptyRequest, GetLyceesService.Response> {

    public GetLyceesService() {
        super(EmptyRequest.class);
    }

    public record Response(
            ResponseHeader header,
            List<Lycee> lycees
    ) {
        public Response(List<Lycee> lycees) { this(new ResponseHeader(), lycees); }
    }


    @Override
    protected @NotNull Response handleRequest(@NotNull EmptyRequest req) throws Exception {
        return new Response(WebServer.db().getLycees());
    }


}
