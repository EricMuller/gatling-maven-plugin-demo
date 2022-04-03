package computerdatabase;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class QcmSimulation extends Simulation {

//  HttpProtocolBuilder httpProtocol = http
//    .baseUrl("http://computer-database.gatling.io") // Here is the root for all relative URLs
//    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
//    .acceptEncodingHeader("gzip, deflate")
//    .acceptLanguageHeader("en-US,en;q=0.5")
//    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0");


    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://qcm.webmarks.net")
            .inferHtmlResources()
            .disableAutoReferer()
            .acceptHeader("application/json, text/plain, */*")
            .acceptEncodingHeader("gzip, deflate, sdch")
            .acceptLanguageHeader("fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4")
            .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.2988.0 Safari/537.36");


    Map <? extends CharSequence, String> headersKeycloak = Map.of("Content-Type", "application/x-www-form-urlencoded");

    Map <? extends CharSequence, String> headers = Map.of( "Referer", "https://qcm.webmarks.net","Authorization", "Bearer #{access_token}", "Cache-Control", "no-cache", "Pragma", "no-cache");

    ScenarioBuilder snKeycloak = scenario("recuperer liste de Questions")
            .pause(5)
            .exec(http("Requestkeycloak token")
                    .post("https://keycloak.webmarks.net/realms/qcm/protocol/openid-connect/token")
                    .headers(headersKeycloak)
                    .formParam("grant_type", "password")
                    .formParam("client_id", "qcm-mobile-rest-api")
                    .formParam("username", "gatling@webmarks.net")
                    .formParam("password", "gatling")
                    .check(status().not(404), status().not(500))
                    .check(jsonPath("$.access_token").saveAs("access_token"))
            ).repeat(1).on(
                    exec(http("Search Question ")
                            .get("/api/v1/qcm/protected/questions?page=0&size=20&sort=id&filters=W10=")
                            .headers(headers))
            );

    {
        setUp(snKeycloak.injectOpen(rampUsersPerSec(10).to(50).during(100)).protocols(httpProtocol));
    }
}
