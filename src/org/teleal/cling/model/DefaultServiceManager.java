/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.model;

import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.state.StateVariableAccessor;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.common.util.Exceptions;
import org.teleal.common.util.Reflections;



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Default implementation, creates and manages a single instance of a plain Java bean.
 * <p>
 * Creates instance of the defined service class when it is first needed (acts as a factory),
 * manages the instance in a field (it's shared), and synchronizes (locks) all
 * multi-threaded access. A locking attempt will timeout after 500 milliseconds with
 * a runtime exception if another operation is already in progress. Override
 * {@link #getLockTimeoutMillis()} to customize this behavior, e.g. if your service
 * bean is slow and requires more time for typical action executions or state
 * variable reading.
 * </p>
 *
 * @author Christian Bauer
 */
public class DefaultServiceManager<T> implements ServiceManager<T> {

    private static Logger log = Logger.getLogger(DefaultServiceManager.class.getName());
    private String TAG = "DefaultServiceManager";
    final protected LocalService<T> service;
    final protected Class<T> serviceClass;
    final protected ReentrantLock lock = new ReentrantLock(true);

    // Locking!
    protected T serviceImpl;
    protected PropertyChangeSupport propertyChangeSupport;

    protected DefaultServiceManager(LocalService<T> service) {
        this(service, null);
    }

    public DefaultServiceManager(LocalService<T> service, Class<T> serviceClass) {
        this.service = service;
        this.serviceClass = serviceClass;
    }

    public void closeServiceManager()
    {
    	serviceImpl = null;
    }
    // The monitor entry and exit methods

    protected void lock() {
        try {
            if (lock.tryLock(getLockTimeoutMillis(), TimeUnit.MILLISECONDS)) {
                log.fine("Acquired lock");
            } else {
                throw new RuntimeException("Failed to acquire lock in milliseconds: " + getLockTimeoutMillis());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to acquire lock:" + e);
        }
    }

    protected void unlock() {
        log.fine("Releasing lock");
        lock.unlock();
    }

    protected int getLockTimeoutMillis() {
        return 500;
    }

    public LocalService<T> getService() {
        return service;
    }

    public T getImplementation() {
        lock();
        try {
            if (serviceImpl == null) {
                init();
            }
            return serviceImpl;
        } finally {
            unlock();
        }
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        lock();
        try {
            if (propertyChangeSupport == null) {
                init();
            }
            return propertyChangeSupport;
        } finally {
            unlock();
        }
    }

    public void execute(Command<T> cmd) throws Exception {
        lock();
        try {
            cmd.execute(this);
        } finally {
            unlock();
        }
    }

    @Override
    public Collection<StateVariableValue> readEventedStateVariableValues(boolean isNewSubscription) throws Exception {
        lock();
        try {
            Collection<StateVariableValue> values;
            if (isNewSubscription) {
                values = readInitialEventedStateVariableValues();
                if (values != null) {
                    log.fine("Obtained initial state variable values for event, skipping individual state variable accessors");
                    return values;
                }
            }

            values = new ArrayList();
            for (StateVariable stateVariable : getService().getStateVariables()) {
                if (stateVariable.getEventDetails().isSendEvents()) {

                    StateVariableAccessor accessor = getService().getAccessor(stateVariable);
                    if (accessor == null)
                        throw new IllegalStateException("No accessor for evented state variable");

                    values.add(accessor.read(stateVariable, getImplementation()));
                }
            }
            return values;

        } finally {
            unlock();
        }
    }

    protected void init() {
        log.fine("No service implementation instance available, initializing...");
        try {
            // The actual instance we ware going to use and hold a reference to (1:1 instance for manager)
            serviceImpl = createServiceInstance();

            // How the implementation instance will tell us about property changes
            propertyChangeSupport = createPropertyChangeSupport(serviceImpl);
            propertyChangeSupport.addPropertyChangeListener(createPropertyChangeListener(serviceImpl));

        } catch (Exception ex) {
            throw new RuntimeException("Could not initialize implementation: " + ex, ex);
        }
    }

    protected T createServiceInstance() throws Exception {
        if (serviceClass == null) {
            throw new IllegalStateException("Subclass has to provide service class or override createServiceInstance()");
        }
        try {
            // Use this constructor if possible
            return serviceClass.getConstructor(LocalService.class).newInstance(getService());
        } catch (NoSuchMethodException ex) {
            log.fine("Creating new service implementation instance with no-arg constructor: " + serviceClass.getName());
            return serviceClass.newInstance();
        }
    }

    protected PropertyChangeSupport createPropertyChangeSupport(T serviceImpl) throws Exception {
        Method m;
        if ((m = Reflections.getGetterMethod(serviceImpl.getClass(), "propertyChangeSupport")) != null &&
                PropertyChangeSupport.class.isAssignableFrom(m.getReturnType())) {
            log.fine("Service implementation instance offers PropertyChangeSupport, using that: " + serviceImpl.getClass().getName());
            return (PropertyChangeSupport) m.invoke(serviceImpl);
        }
        log.fine("Creating new PropertyChangeSupport for service implementation: " + serviceImpl.getClass().getName());
        return new PropertyChangeSupport(serviceImpl);
    }

    protected PropertyChangeListener createPropertyChangeListener(T serviceImpl) throws Exception {
        return new DefaultPropertyChangeListener();
    }

    protected Collection<StateVariableValue> readInitialEventedStateVariableValues() throws Exception {
        return null;
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Implementation: " + serviceImpl;
    }

    protected class DefaultPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
//            Utils.print(TAG, "Property change event on local service: " + e.getPropertyName());

            // Prevent recursion
            if (e.getPropertyName().equals(EVENTED_STATE_VARIABLES)) return;

            // Is it an evented state variable?
            final StateVariable sv = getService().getStateVariable(e.getPropertyName());
            if (sv == null || !sv.getEventDetails().isSendEvents()) {
                return;
            }

            try {
//            	Utils.print(TAG, "Evented state variable value changed, reading state of service: " + sv);
                Collection<StateVariableValue> currentValues = readEventedStateVariableValues(false);

                getPropertyChangeSupport().firePropertyChange(
                        EVENTED_STATE_VARIABLES,
                        null,
                        currentValues
                );

            } catch (Exception ex) {
                // TODO: Is it OK to only log this error? It means we keep running although we couldn't send events?
                log.severe("Error reading state of service after state variable update event: " + Exceptions.unwrap(ex));
            }
        }
    }
}
