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
package ws.oceanos.core.event.test

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestKit, ImplicitSender }
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import ws.oceanos.core.dsl._
import ws.oceanos.core.services._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.typesafe.config.ConfigFactory

@RunWith(classOf[JUnitRunner])
class EventProcessorTest(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with FlatSpec
  with ShouldMatchers
  with BeforeAndAfterAll
  with FlowDSL {

  def this() = this(ActorSystem("EventProcessorTest",
    ConfigFactory.parseString(
      """
        | os-event-processor-mailbox {
        |    mailbox-type = "akka.dispatch.UnboundedDequeBasedMailbox"
        |  }
      """.stripMargin)))

  //override def beforeAll() = { println(system.settings)}

  override def afterAll() = { system.shutdown() }

  class Helper {
    register("hello", Props(classOf[Echo],"Hello"))
    register("beautiful", Props(classOf[Echo],"Beautiful"))
    register("amazing", Props(classOf[Echo],"Amazing"))
    register("world", Props(classOf[Echo],"World"))
    def actor(flows: Flow*) = {
      system.actorOf(flow(flows: _*))
    }
  }

  "Event Component" should "reply to requests" in new Helper {

    val ep = actor( n("world") )

    ep ! "Hello"

    fishForMessage() {
      case m:String if m == "HelloWorld" => true
      case _ => false
    }

  }
  it should "allow to create pipelines" in new Helper {

    val ep = actor( n("hello")~>n("world") )

    //val ep = actor( "hello" ~> "os:/world")

    ep ! "Great"

    fishForMessage() {
      case m:String if m == "GreatHelloWorld" => true
      case _ => false
    }
  }

  it should "allow to process several request in a row" in new Helper {

    import scala.concurrent.duration._

    val ep = actor( n("hello") )

    (1 to 1000).foreach(_ => ep ! "Test")

    val messages = receiveN(1000, 4.seconds)

    assert( messages.forall(_ == "TestHello"))

  }

  it should "allow to create conditional branches" in new Helper {

   val ep = actor(
     n("hello")~>filter(_ == "Path1Hello")~>n("world"),
     n("hello")~>filter(_ == "Path2Hello")~>n("beautiful")~>n("world",1)
   )


   ep ! "Path2"

   fishForMessage() {
     case m:String if m == "Path2HelloBeautifulWorld" => true
     case s => false
   }
  }

  it should "allow message transforms" in new Helper {

   val ep = actor( n("hello")~>map(_ + "Map")~>n("world"))

   ep ! "Test"

   fishForMessage() {
     case m:String if m == "TestHelloMapWorld" => true
     case s => false
   }
  }


  it should "allow sync parallel branches" in new Helper {

   val ep = actor(
     n("hello")~>n("beautiful")~>merge~>n("world"),
     n("hello")~>n("amazing")~>merge~>n("world")
   )

   ep ! "Test"

   fishForMessage() {
     case m:String if m == "TestHelloBeautifulTestHelloAmazingWorld" => true
     case s => {println(s);false}
   }

  }

}
