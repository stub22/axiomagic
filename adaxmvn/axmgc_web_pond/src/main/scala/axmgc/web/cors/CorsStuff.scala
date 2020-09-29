package axmgc.web.cors

private trait CorsStuff

// Code copied and modified from example found at:
// https://dzone.com/articles/handling-cors-in-akka-http

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, options, respondWithHeaders, entity, get, path, _}

import akka.http.scaladsl.model.headers._

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directive0



trait CORSHandler{
	private val corsResponseHeaders = List(
		`Access-Control-Allow-Origin`.*,
		`Access-Control-Allow-Credentials`(true),
		`Access-Control-Allow-Headers`("Authorization",
			"Content-Type", "X-Requested-With")
	)
	//this directive adds access control headers to normal responses
	private def addAccessControlHeaders: Directive0 = {
		respondWithHeaders(corsResponseHeaders)
	}
	//this handles preflight OPTIONS requests.
	private def preflightRequestHandler: dslServer.Route = options {
		complete(HttpResponse(StatusCodes.OK).
				withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
	}
	// Wrap the Route with this method to enable adding of CORS headers
	def corsHandler(r: dslServer.Route): dslServer.Route = addAccessControlHeaders {
		preflightRequestHandler ~ r
	}
	// Helper method to add CORS headers to HttpResponse
	// preventing duplication of CORS headers across code
	def addCORSHeaders(response: HttpResponse):HttpResponse =
		response.withHeaders(corsResponseHeaders)
}
