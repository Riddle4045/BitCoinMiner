import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.actor._

trait Message
case object RequestWork extends Message
case class StartStandAlone(serverAddress : String , MasterPort : Int, trailingZeroes : Int) extends Message
case object Start extends Message
case object Stop extends Message
case object WorkDone extends Message
case class Work(UFID : String) extends Message
case class Result(checkString : String , bitcoin : String) extends Message


class Master(numOfWorkers : Int, ufID : String) extends Actor {
  
  //created numOfWorkers slaves and assigns them work.
  private def DistributeWorkonMachine(numOfWorkers : Int,serverAddress : String , MasterPort : Int, trailingZeroes : Int){
                    for( i <- 1 to numOfWorkers){
                            var slave = context.actorOf(Props(new Worker(serverAddress,MasterPort,trailingZeroes)), name="slave"+i) 
                            slave ! Work(ufID)
                    }
  }
  
  var activeWorkers = numOfWorkers
  def receive = {
    case StartStandAlone(addr,port,zeros)=> println("Master is up")
                  DistributeWorkonMachine(numOfWorkers,addr,port,zeros)
    case "hello" => println("hello recieved from a slave!")
    case RequestWork => println("is assigning work to workers")
                        activeWorkers +=  1
                        sender ! "sending"
                       sender ! Work(ufID) 
    case Result(p,s) => println(p+ "  " +s)
    case WorkDone => println("slave  " + sender.path + "  has finished work")
            activeWorkers -= 1
            if(activeWorkers == 0) {
               println("No Active workers left shuttting down master")
              context.system.shutdown()
              System.exit(0);
            }
            sender ! Stop   
  }  
  
}

class Worker(serverAddress : String , MasterPort : Int, trailingZeroes : Int) extends Actor {

   val r = scala.util.Random;

   def MD5(s: String): String = {
    // Besides "MD5", "SHA-256", and other hashes are available
    val m = java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"))
    m.map("%02x".format(_)).mkString
  }
   
   def checkBitCoin(s: String) : String = {
           
           val substr = s.substring(0, trailingZeroes) 
           var zero = "0"*trailingZeroes;
           if ( substr == zero) {
                 return s;
           }else{return "fail"};
   }

   def randomGen(rawID : String) = {
   for( i <- 1 to 1000000){
       var checkString = rawID+r.nextInt().toString();
           val send = 
                 checkBitCoin(MD5(checkString));
                        if(send!="fail")
                        {
                        sender ! Result(checkString,send) 
                        }}
   sender ! WorkDone
 }
 
  def receive = {
    case Start => println("Contacted  a slave")
                     val masterRef = "akka.tcp://Master@" + serverAddress + ":" + MasterPort + "/user/MasterActor"
                     println(masterRef)
                     val selection = context.actorSelection(masterRef) ! RequestWork
     case Stop => println("Shutting down worker")
                  context.stop(self)                
     case Work(s) =>   randomGen(s);
     case "sending" => println("About to get work from master")
  }
}

class Listener extends Actor {
  def receive = {
     case "Stop" => context.system.shutdown()
  }
}

object Master {
          def main(args: Array[String]) {
            
                      //the number of tariling zero's.
                      val numZero = if (args.length > 0) args(0) toInt else 3
                      var ufID = "ipatwa"
                      
                      //when there's a standAlone implementation default to this number of workers.
                      //incase of distributed, these many workers will be running on the same machine as master
                      val numOfWorkers = 3
                      
                      //port at which the server (MASTER) listens to.
                      val MasterPort = 8811
                      
                      //address of the slave actors ( valid for standAlone and distributed implementations )
                      val hostAddress = "127.0.0.1"
                      
                      //port used by the slave actors.
                      val localPort = 2771
                      
                      //address of the server(MASTER) in standAlone implementation it should be localHost in dstributed it should be the IP passed in arguments.
                      var serverAddress: String = java.net.InetAddress.getLocalHost.getHostAddress()
                      if(args.length > 1) serverAddress = args(1)  
                      if(args.length <2){
                      //configuration of the remote Actor i.e the Server.
                      val configString = """akka {
                                actor {
                                  provider = "akka.remote.RemoteActorRefProvider"
                                }
                                remote {
                                  enabled-transports = ["akka.remote.netty.tcp"]
                                  netty.tcp {
                                    hostname = """ + serverAddress + """
                                    port = """ + MasterPort + """
                                  }
                               }
                              }"""

                      val configuration = ConfigFactory.parseString(configString)
                      val system = ActorSystem("Master", ConfigFactory.load(configuration))
                      val listener = system.actorOf(Props[Listener], name="listener")
                      val master = system.actorOf(Props(new Master(numOfWorkers,ufID)), name="MasterActor")
                      master ! StartStandAlone(hostAddress,localPort,numZero)
                      }else{
                      //configuration of the slaves doing work on different machines.
                      val localconfigString = """akka {
                                actor {
                                  provider = "akka.remote.RemoteActorRefProvider"
                                }
                                remote {
                                  enabled-transports = ["akka.remote.netty.tcp"]
                                  netty.tcp {
                                    hostname = """ + hostAddress + """
                                    port = """ + localPort + """
                                  }
                               }
                              }"""
                              
                      val localconfiguration = ConfigFactory.parseString(localconfigString)
                      val localsystem = ActorSystem("Slave", ConfigFactory.load(localconfiguration))
                      val slave = localsystem.actorOf(Props(new Worker(serverAddress,MasterPort,numZero)), name="slave")
                      slave ! Start
                      }
                      
          }
}


