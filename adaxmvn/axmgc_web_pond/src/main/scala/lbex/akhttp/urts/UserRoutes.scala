package lbex.akhttp.urts

/*
Code in this package copied from this example and hacked slightly to fit under Axmgc:
https://github.com/akka/akka-http-quickstart-scala.g8/
Package "lbex.akhttp.urts" is public domain sample code, distributed under:
http://creativecommons.org/publicdomain/zero/1.0/.
 */

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
// import UserRegistryActor._
import akka.pattern.ask
import akka.util.Timeout

//#user-routes-class
trait UserRoutes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[UserRoutes])

  // other dependencies that UserRoutes use
  def userRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete   
  lazy val userRoutes: Route =
    pathPrefix("users") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            get {
				val qryMsg = UserRegistryActor.GetUsers
				val usrFut: Future[Users] = (userRegistryActor ? qryMsg).mapTo[Users]
              	complete(usrFut)
            },
            post {
              entity(as[User]) { user =>
				val creMsg = UserRegistryActor.CreateUser(user)
				val creFut: Future[UserRegistryActor.ActionPerformed] =
                  (userRegistryActor ? creMsg).mapTo[UserRegistryActor.ActionPerformed]
                onSuccess(creFut) { performed =>
					log.info("Created user [{}]: {}", user.name, performed.description)
					complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        //#users-get-post
        //#users-get-delete
        path(Segment) { name =>
          concat(
            get {
              //#retrieve-user-info
              val maybeUser: Future[Option[User]] =
                (userRegistryActor ? UserRegistryActor.GetUser(name)).mapTo[Option[User]]
              rejectEmptyResponse {
                complete(maybeUser)
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              val userDeleted: Future[UserRegistryActor.ActionPerformed] =
                (userRegistryActor ? UserRegistryActor.DeleteUser(name)).mapTo[UserRegistryActor.ActionPerformed]
              onSuccess(userDeleted) { performed =>
                log.info("Deleted user [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#users-delete-logic
            }
          )
        }
      )
      //#users-get-delete
    }
  //#all-routes
}
