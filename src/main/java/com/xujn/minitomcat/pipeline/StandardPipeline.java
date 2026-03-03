package com.xujn.minitomcat.pipeline;

import com.xujn.minitomcat.connector.HttpRequest;
import com.xujn.minitomcat.connector.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory pipeline placeholder that stores valves but does not drive dispatch yet.
 */
public class StandardPipeline implements Pipeline {

    private final List<Valve> valves = new ArrayList<>();
    private Valve basic = new NoopValve();

    @Override
    public void addValve(Valve valve) {
        valves.add(valve);
    }

    @Override
    public Valve[] getValves() {
        return valves.toArray(Valve[]::new);
    }

    @Override
    public Valve getBasic() {
        return basic;
    }

    @Override
    public void setBasic(Valve valve) {
        this.basic = valve;
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) {
        basic.invoke(request, response, (req, resp) -> {
            // Phase 1 leaves the pipeline disconnected from runtime dispatch.
        });
    }
}
