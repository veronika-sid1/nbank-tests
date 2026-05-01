package api.requests.skeleton.interfaces;

import java.util.Map;

public interface GetAllEndpointInterface {
    Object getAll(Class<?> clazz);
    Object getAll(Class<?> clazz, String pathParam, Object valueParam);
}
