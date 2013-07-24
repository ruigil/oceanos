package ws.oceanos.core

import org.osgi.framework._

class HelloScalaActivator extends BundleActivator {
	var serviceRegistration:ServiceRegistration[HelloScalaService] = _

	def start(bundleContext: BundleContext){
		Console.println("STARTING ws.oceanos.core.App")
		//serviceRegistration = bundleContext.registerService("com.domain.osgi.scala.HelloScalaService",new HelloScalaServiceImpl(),null)
		Console.println("REGISTERED ws.oceanos.core.App")

	}

	def stop(bundleContext: BundleContext) {
		Console.println("STOPPED ws.oceanos.core.App")
		//if(serviceRegistration != null) serviceRegistration.unregister
		Console.println("UNREGISTERED ws.oceanos.core.App")
	}
}