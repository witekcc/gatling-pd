package prevalidation

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import java.util.UUID
import com.permissiondata.leads.pb._
import com.googlecode.protobuf.format.JsonFormat

class ValidateLoadSimulation extends Simulation {

        //val httpConf = http.baseURL("http://localhost:9008")
	val httpConf = http.baseURL("http://dev13.permissiondata.com/validationcoordinator")
	val usersCount = 1

	val domains = Array("@permissiondata.com").toList
	val userData = Iterator.continually( 
		Map(
			"EMAIL" -> (Random.alphanumeric.take(20).mkString + (Random.shuffle(domains).head)),
			"UUID" -> (UUID.randomUUID.toString())
		)
	)
	//val feeder = csv("emails.csv").random
	//val userFeeder = Iterator.continually( Map("EMAIL" -> (Random.alphanumeric.take(20).mkString + (Random.shuffle(domains).head)),
        //                                 "AGE" -> (Random.nextInt(60) + 16), 
        //                                 "GENDER" -> (Random.shuffle(gender).head),
        //                                 "STATE" -> (Random.shuffle(states).head)))
	
	def getLead = () => { 
				var lead = LeadProtos.Lead.newBuilder()
					.setUuid(UUID.randomUUID.toString())
					.setCampaignUuid("c69f79ce-9770-ac02-8157-abe31f3e2b0a")
					.setSessionUuid("00000000-0000-0000-0000-000000000000")
					.setPlacementUuid("00000000-0000-0000-0000-000000000000")
					.setUser(
						LeadProtos.User.newBuilder()
						.setEmail(Random.alphanumeric.take(20).mkString + (Random.shuffle(domains).head))
						.setIp("123.123.123.123")
					)
					.build()
				println(JsonFormat.printToString(lead))
				lead.toByteArray()
				//JsonFormat.printToString(lead)
			}

	val userFeeder = Iterator.continually(Map("LEAD" -> (getLead())))

	//TODO add ?prevalidation=true
	val scn = {
		scenario("load test 1")
		.feed(userFeeder)
		.exec(
			http("validation load test")
			.post("/validate/")
			//.header("Content-Type", "application/json")
			.header("Content-Type", "application/octet-stream")
			//.body(StringBody(JsonFormat.printToString(lead)))
			//.body(ByteArrayBody())
			//.body(StringBody("${LEAD}"))
			.body(ByteArrayBody("${LEAD}"))
			.check(status.is(session => 200))
		)
	}

	setUp(scn.inject(constantUsersPerSec(1) during(4 seconds)).protocols(httpConf))


/*
        setUp(
          TestPing.scnGet.inject(
            nothingFor(1 seconds),
            atOnceUsers(30),
            rampUsers(5000) over(300 seconds)
            ).protocols(httpProtocol)
        )
*/

/*
val hostUrl = Config.host
 
// how many requests per user
val requestsNumber = 20
 
// pause between two subsequent requests for the same user
val requestPauseSeconds = 15
 
// load users' (name, password) combination
val usersDataSource: Array[Map[String, String]] = csv("user_credentials.csv")
 
// perform requests for each user from credentials files in parallel
    private val usersCount: Int = usersDataSource.length
      val scn = {
        scenario("resource test: host=" + hostUrl + ", users_count=" + usersCount)
          .feed(usersDataSource)
 
            // at the beginning we need to find user id - note the "trick" with saving the result into the "userId" variable
            .exec(http("getUserId")
              .get("/gdc/app/account/bootstrap")
              .basicAuth("${username}", "${password}")
              .check(jsonPath("$.bootstrapResource.current.loginMD5").find.saveAs("userId"))
            )
 
            /*
             * the performance testing itself - perform multiple requests for each user
             * - requests for one user are not concurrent, there is only one request per user at time
             * - there are multiple concurrent requests for different users
             */
          .repeat(requestsNumber) {
          exec(http("processesView GET")
            .get("/gdc/app/account/profile/${userId}/dataload/processesView").basicAuth("${username}", "${password}"))
            .pause(requestPauseSeconds)
        }
  }
 
    setUp(scn.users(usersCount).ramp(20).protocolConfig(httpConf))
*/ 

}

