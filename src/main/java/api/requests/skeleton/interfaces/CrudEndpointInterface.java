package api.requests.skeleton.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post();
    Object post(BaseModel model);
    Object get();
    Object get(long id);
    Object get(String pathParamName, Object pathParamValue);
    Object update(BaseModel model);
    Object delete(long id);
}
