/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
package org.dojotoolkit.server.util.osgi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

public class DojoServicesInitializer {
	private static Logger logger = Logger.getLogger("org.dojotoolkit.server.util.osgi");
	
	private static final String DOJO_SERVICES_HEADER = "Export-DojoService";//$NON-NLS-1$
	private static final String DOJO_SERVICE_ID = "dojoServiceId";//$NON-NLS-1$
	private static final String IMPL = "impl";//$NON-NLS-1$
	private static final String ID = "id";//$NON-NLS-1$
	private List<ServiceRegistration> serviceRegistrations = null;

	public DojoServicesInitializer() {
		serviceRegistrations = new ArrayList<ServiceRegistration>();
	}
	
	public void start(BundleContext bundleContext) {
		Bundle bundle = bundleContext.getBundle();
		String pluginServices = (String)bundle.getHeaders().get(DOJO_SERVICES_HEADER);
		if (pluginServices != null) {
			Map<String, Object> implLookup = new HashMap<String, Object>(); 
			StringTokenizer st = new StringTokenizer(pluginServices, ",");//$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String pluginService = st.nextToken();
				StringTokenizer paramTokenizer = new StringTokenizer(pluginService, ";");//$NON-NLS-1$
				if (paramTokenizer.countTokens() == 3) {
					String serviceImplName = null;
					String id = null;
					String serviceName = null;
					Properties properties = new Properties();
					while (paramTokenizer.hasMoreTokens()) {
						String param = paramTokenizer.nextToken();
						if (param.indexOf('=') != -1) {
							String name = param.substring(0, param.indexOf('='));
							String value = param.substring(param.indexOf('=')+1);
							if (name.equals(ID)) {
								id = value;
								properties.put(DOJO_SERVICE_ID, id);
							}
							else if (name.equals(IMPL)) {
								serviceImplName = value;
							}
						}
						else {
							serviceName = param;
						}
					}
					if (serviceImplName != null && id != null) {
						Object serviceImpl = implLookup.get(serviceImplName);
						if (serviceImpl == null) {
							try {
								Class<?> serviceImplClass = bundle.loadClass(serviceImplName);
								serviceImpl = serviceImplClass.newInstance();
								logger.logp(Level.FINE, getClass().getName(), "start", "Creating instance of ["+serviceImplName+']');//$NON-NLS-1$
								Method startMethod = findMethod("start", serviceImplClass);//$NON-NLS-1$
								
								if (startMethod != null) {
									Class<?>[] parameterTypes = startMethod.getParameterTypes();
									if (parameterTypes.length == 0) {
										logger.logp(Level.FINE, getClass().getName(), "start", "Calling start() method of ["+serviceImplName+']');//$NON-NLS-1$
										startMethod.invoke(serviceImpl, (Object[])null);
									}
									else if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(BundleContext.class.getName())) {
										logger.logp(Level.FINE, getClass().getName(), "start", "Calling start(BundleContext context) method of ["+serviceImplName+']');//$NON-NLS-1$
										startMethod.invoke(serviceImpl, new Object[] {bundleContext});
									}
								}
								implLookup.put(serviceImplName, serviceImpl);
							} catch (Exception e) {
								logger.logp(Level.SEVERE, getClass().getName(), "start", "Failed to register Plugin Service ["+serviceImplName+"] ["+id+']', e);//$NON-NLS-1$//$NON-NLS-2$
							}
						}
						logger.logp(Level.FINE, getClass().getName(), "start", "Registering Plugin Service for id ["+id+"] interface ["+serviceName+"] implClass ["+serviceImplName+']');//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						ServiceRegistration sr = bundleContext.registerService(serviceName, serviceImpl, properties);
						serviceRegistrations.add(sr);
					}
					else {
						logger.logp(Level.SEVERE, getClass().getName(), "start", "An impl or id value have not been provided for "+DOJO_SERVICES_HEADER+" entry ["+pluginService+']');//$NON-NLS-1$//$NON-NLS-2$
					}
				}
				else {
					logger.logp(Level.SEVERE, getClass().getName(), "start", "Invalid number of parameters in Plugin Service entry ["+pluginService+']');//$NON-NLS-1$
				}
			}
		}
	}
	
	public void stop(BundleContext bundleContext) {
		List<String> stopped = new ArrayList<String>();
		for (ServiceRegistration sr : serviceRegistrations) {
			Object serviceImpl = bundleContext.getService(sr.getReference());
			String[] objectClass = (String[])sr.getReference().getProperty(Constants.OBJECTCLASS);
			logger.logp(Level.FINE, getClass().getName(), "stop", "Unregistering Plugin Service ["+objectClass[0]+"]");//$NON-NLS-1$//$NON-NLS-2$
			bundleContext.ungetService(sr.getReference());
			sr.unregister();
			if (!stopped.contains(serviceImpl.getClass().getName())) {
				stopped.add(serviceImpl.getClass().getName());
				Method stopMethod = findMethod("stop", serviceImpl.getClass());//$NON-NLS-1$
				
				try {
					if (stopMethod != null) {
						Class<?>[] parameterTypes = stopMethod.getParameterTypes();
						if (parameterTypes.length == 0) {
							logger.log(Level.FINE, getClass().getName(), "Calling stop() method of ["+serviceImpl.getClass().getName()+']');//$NON-NLS-1$
							stopMethod.invoke(serviceImpl, (Object[])null);
						}
						else if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(BundleContext.class.getName())) {
							logger.log(Level.FINE, getClass().getName(), "Calling stop((BundleContext context) method of ["+serviceImpl.getClass().getName()+']');//$NON-NLS-1$
							stopMethod.invoke(serviceImpl, new Object[] {bundleContext});
						}
					}
				} catch (Exception e) {
					logger.logp(Level.FINE, getClass().getName(), "stop", "Exception thrown while calling stop method for Plugin Service ["+objectClass[0]+']', e);//$NON-NLS-1$
				}
			}
		}
		serviceRegistrations.clear();
	}
	
	private Method findMethod(String methodName, Class<?> clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
