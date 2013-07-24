package ws.oceanos.core

import org.apache.felix.scr.annotations._

@Service
@Component
class HelloScalaServiceImpl extends HelloScalaService {  
   def hello() = {
	Console.println("Hello, from scala")
	"Hello, from Scala"
   }
}