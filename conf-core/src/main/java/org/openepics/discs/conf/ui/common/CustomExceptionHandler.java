/*
 * Copyright (c) 2014 European Spallation Source
 * Copyright (c) 2014 Cosylab d.d.
 *
 * This file is part of Controls Configuration Database.
 *
 * Controls Configuration Database is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or any newer
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */

package org.openepics.discs.conf.ui.common;

import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;

import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;

/**
 * A global JSF exception handler that displays caught exceptions in the UI as popup messages.
 *
 * @author <a href="mailto:marko.kolar@cosylab.com">Marko Kolar</a>
 * @author <a href="mailto:sunil.sah@cosylab.com">Sunil Sah</a>
 *
 */
public class CustomExceptionHandler extends FullAjaxExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger(CustomExceptionHandler.class.getCanonicalName());
    private static final String UNEXPECTED_ERROR = "Unexpected error";

    /** A new JSF exception handler
     * @param wrapped the original JSF exception handler
     */
    public CustomExceptionHandler(ExceptionHandler wrapped) {
        super(wrapped);
    }

    @Override
    public void handle() throws FacesException {
        final Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = getUnhandledExceptionQueuedEvents().iterator();

        if (!unhandledExceptionQueuedEvents.hasNext()) {
            // There's no unhandled exception.
            return;
        }

        final Throwable unwrappedException = unhandledExceptionQueuedEvents.next().getContext().getException();
        // Handle UIException case which requires redirect to another page
        final InvocationTargetException ite = getInvocationTargetException(unwrappedException);
        if ((ite != null) && (ite.getTargetException() instanceof UIException)) {
            final ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            try {
                // ExternalContext.redirect is the most low-level redirect I could find, and using uri parameters
                // seems like the most straight-forward way of doing it.
                ec.redirect(ec.getRequestContextPath() + "/error.xhtml?errorMsg=" +
                        URLEncoder.encode(((UIException)ite.getTargetException()).getMessage(), "UTF-8"));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to redirect to error page.", e);
            }
        } else {
            // Handle all other cases where redirect is not needed.
            final Throwable throwable = getExceptionNonframeworkCause(unwrappedException);
            if (!(throwable instanceof javax.faces.application.ViewExpiredException)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, UNEXPECTED_ERROR, throwable.getMessage()));
                LOGGER.log(Level.SEVERE, UNEXPECTED_ERROR, throwable);
            } else {
                super.handle();
            }
        }
    }

    /**
     * Check for leaf {@link InvocationTargetException}.
     *
     * @param exception The top exception
     * @return The invocation target exception or <code>null</code>
     */
    private InvocationTargetException getInvocationTargetException(Throwable exception) {
        Throwable iterated = exception;
        while ((iterated != null) && (iterated.getCause() != null)) {
            if (iterated instanceof InvocationTargetException)
                return (InvocationTargetException) iterated;

            iterated = iterated.getCause();
        }

        return null;
    }

    /* Returns the nested exception cause that is not Faces or EJB exception, if it exists. */
    private Throwable getExceptionNonframeworkCause(Throwable exception) {
        return (exception instanceof FacesException || exception instanceof EJBException || exception instanceof ELException)
                    && (exception.getCause() != null)
               ? getExceptionNonframeworkCause(exception.getCause())
               : exception;
    }
}
