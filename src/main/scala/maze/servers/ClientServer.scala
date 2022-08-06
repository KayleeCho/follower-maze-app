package maze.servers


import java.io.{BufferedReader, InputStreamReader}
import java.net.ServerSocket

import maze.registry.UserRegistry

import scala.annotation.tailrec

class ClientServer(userHandler: UserRegistry) extends Runnable {

  val clientPort = 9099

  override def run(): Unit = {
    System.out.println("Listening for client requests on " + clientPort)

    process(new ServerSocket(clientPort))


  }

  @tailrec
  private def process(serverSocket: ServerSocket): Unit =
    Option(serverSocket.accept()) match {
      case None => ()
      case Some(userSocket) =>
        val reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()))
        Option(reader.readLine()).foreach { userId =>
          if( userId != null) {
            userHandler.add(userId.toLong, userSocket)
            System.out.println( "user Id" + userId + "connected" + " (" + userHandler.numberOfUsersOnline + " total)")
          }

        }
        process(serverSocket)
    }


}