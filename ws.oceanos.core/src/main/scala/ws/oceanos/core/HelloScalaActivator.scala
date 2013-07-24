/*
 * Copyright 2013 Rui Gil.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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