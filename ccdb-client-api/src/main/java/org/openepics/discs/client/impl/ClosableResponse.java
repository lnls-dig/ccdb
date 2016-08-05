package org.openepics.discs.client.impl;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * JAX RS Does not support the {@link AutoCloseable} for responses.
 *
 * This is a wrapper class to alleviate this unfortunate design error..
 *
 * @author <a href=mailto:miroslav.pavleski@cosylab.com>Miroslav Pavleski</a>
 * @author <a href="mailto:miha.vitorovic@cosylab.com">Miha Vitorovič</a>
 */
public class ClosableResponse extends Response implements AutoCloseable {
    private Response response;

    public ClosableResponse(Response response) {
        this.response = response;
    }

    @Override
    public int hashCode() {
        return response.hashCode();
    }

    @Override
    public int getStatus() {
        return response.getStatus();
    }

    @Override
    public boolean equals(Object obj) {
        return response.equals(obj);
    }

    @Override
    public StatusType getStatusInfo() {
        return response.getStatusInfo();
    }

    @Override
    public Object getEntity() {
        return response.getEntity();
    }

    @Override
    public <T> T readEntity(Class<T> entityType) {
        return response.readEntity(entityType);
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType) {
        return response.readEntity(entityType);
    }

    @Override
    public String toString() {
        return response.toString();
    }

    @Override
    public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
        return response.readEntity(entityType, annotations);
    }

    @Override
    public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
        return response.readEntity(entityType, annotations);
    }

    @Override
    public boolean hasEntity() {
        return response.hasEntity();
    }

    @Override
    public boolean bufferEntity() {
        return response.bufferEntity();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public MediaType getMediaType() {
        return response.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return response.getLanguage();
    }

    @Override
    public int getLength() {
        return response.getLength();
    }

    @Override
    public Set<String> getAllowedMethods() {
        return response.getAllowedMethods();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return response.getCookies();
    }

    @Override
    public EntityTag getEntityTag() {
        return response.getEntityTag();
    }

    @Override
    public Date getDate() {
        return response.getDate();
    }

    @Override
    public Date getLastModified() {
        return response.getLastModified();
    }

    @Override
    public URI getLocation() {
        return response.getLocation();
    }

    @Override
    public Set<Link> getLinks() {
        return response.getLinks();
    }

    @Override
    public boolean hasLink(String relation) {
        return response.hasLink(relation);
    }

    @Override
    public Link getLink(String relation) {
        return response.getLink(relation);
    }

    @Override
    public Builder getLinkBuilder(String relation) {
        return response.getLinkBuilder(relation);
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return response.getMetadata();
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return response.getHeaders();
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return response.getStringHeaders();
    }

    @Override
    public String getHeaderString(String name) {
        return response.getHeaderString(name);
    }
}
