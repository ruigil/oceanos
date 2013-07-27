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

import akka.actor.{ActorLogging, Actor, Props, ActorSystem}
import akka.testkit.{TestKit, ImplicitSender }
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import ws.oceanos.core.flow._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class EventProcessorTest(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with FlatSpec
  with ShouldMatchers
  with BeforeAndAfterAll
  with FlowContext {

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

  "Event Processor" should "reply to requests" in new Helper {

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

  it should "allow multi state" in new Helper {

    val ep = actor(
      in~>n("hello")~>outin~>n("beautiful")~>outin~>n("world")~>out
    )

    ep ! "Test"
    val messages1 = receiveN(1, 1.seconds)
    assert( messages1.size == 1)
    assert( messages1.head == "TestHello")

    ep ! "Test"
    val messages2 = receiveN(1, 1.seconds)
    assert( messages2.size == 1)
    assert( messages2.head == "TestBeautiful")

    ep ! "Test"
    val messages3 = receiveN(1, 1.seconds)
    assert( messages3.size == 1)
    assert( messages3.head == "TestWorld")

  }

}