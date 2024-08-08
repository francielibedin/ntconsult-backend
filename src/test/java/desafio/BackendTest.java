package desafio;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class BackendTest {

    JobApplicant jobApplicant =
            new JobApplicant("Francieli Desafio", 2024, "Quality Assurance", "NtConsult");

    String idCreateObject;

    @BeforeAll
    public static void setup() {
        baseURI = "https://api.restful-api.dev";
        filters(new AllureRestAssured());
        enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @DisplayName("Deve criar um objeto com sucesso utilizando o method POST")
    @Order(1)
    @Test
    void testPostSuccess() {
        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        {\s
                            "name": "%s",
                            "data": {
                               "year": %d,
                               "office": "%s",
                               "company": "%s"
                            }
                         }
                        \s""".formatted(jobApplicant.name(), jobApplicant.year(),
                        jobApplicant.oficce(), jobApplicant.Company()))
        .when()
                .post("objects")
        .then()
                .statusCode(equalTo(HttpStatus.SC_OK))
                .and()
                .body("id", notNullValue())
                .and()
                .body("createdAt", notNullValue())
                .and()
                .body("name", equalTo(jobApplicant.name()))
                .and()
                .body("data.year", equalTo(jobApplicant.year()))
                .and()
                .body("data.office", equalTo(jobApplicant.oficce()))
                .and()
                .body("data.company", equalTo(jobApplicant.Company()));

    }

    @DisplayName("Não deve criar multiplos objetos através do method POST")
    @Order(2)
    @Test
    void testPostCreateFail() {
        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        [
                           {
                              "name": "%s",
                              "data":{
                                 "year": %d,
                                 "office": "%s",
                                 "company": "%s"
                              }
                           },
                           {
                              "name":"Francieli Bedin",
                              "data":{
                                 "year":2023,
                                 "office":"Quality Assurance Senior",
                                 "company":"NtConsult"
                              }
                           }
                        ]
                        """.formatted(jobApplicant.name(), jobApplicant.year(),
                        jobApplicant.oficce(), jobApplicant.Company()))
        .when()
                .post("objects")
        .then()
                .statusCode(equalTo(HttpStatus.SC_BAD_REQUEST))
                .and()
                .body("error", equalTo("400 Bad Request. If you are trying to create or update the data, " +
                        "potential issue is that you are sending incorrect body json or it is missing at all."));
    }

    @DisplayName("Deve retorna o objeto pelo id no methor GET")
    @Order(3)
    @Test
    public void testGetSuccess() {
        givenCreateObject();
        given()
                .basePath("objects/{id}")
                .pathParam("id", idCreateObject)
        .when()
                .get()
        .then()
                .statusCode(equalTo(HttpStatus.SC_OK))
                .and()
                .body("id", equalTo(idCreateObject))
                .and()
                .body("name", equalTo(jobApplicant.name()))
                .and()
                .body("data.year", equalTo(jobApplicant.year()))
                .and()
                .body("data.office", equalTo(jobApplicant.oficce()))
                .and()
                .body("data.company", equalTo(jobApplicant.Company()));
    }

    @DisplayName("Deve retornar 404(NOT FOUND) se o objeto não for encontrado no method GET")
    @Order(4)
    @Test
    public void testGetFail() {
        var id = "fgdfgfsfdhgfhsfhg";
        given()
                .basePath("objects/{id}")
                .pathParam("id", id)
        .when()
                .get()
        .then()
                .statusCode(equalTo(HttpStatus.SC_NOT_FOUND))
                .and()
                .body("error", equalTo("Oject with id=%s was not found.".formatted(id)));
    }

    @DisplayName("Deve atualizar todos os dados existentes do objeto criado no method PUT.")
    @Order(5)
    @Test
    public void testPutSuccess() {
        var yearUpdate = 2025;
        givenCreateObject();
        given()
                .basePath("objects/{id}")
                .pathParam("id", idCreateObject)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        {\s
                            "name": "%s",
                            "data": {
                               "year": %d,
                               "office": "%s",
                               "company": "%s"
                            }
                         }
                        \s""".formatted(jobApplicant.name(), yearUpdate,
                        jobApplicant.oficce(), jobApplicant.Company()))
        .when()
                .put()
        .then()
                .statusCode(equalTo(HttpStatus.SC_OK))
                .and()
                .body("id", notNullValue())
                .and()
                .body("updatedAt", notNullValue())
                .and()
                .body("name", equalTo(jobApplicant.name()))
                .and()
                .body("data.year", equalTo(yearUpdate))
                .and()
                .body("data.office", equalTo(jobApplicant.oficce()))
                .and()
                .body("data.company", equalTo(jobApplicant.Company()));
    }

    @DisplayName("Deve retornar 404(NOT FOUND) se o objeto não for encontrado no method PUT")
    @Order(6)
    @Test
    public void testPutFail() {
        var id = "fgdfgfsfdhgfhsfhg";
        given()
                .basePath("objects/{id}")
                .pathParam("id", id)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        {\s
                            "name": "%s",
                            "data": {
                               "year": %d,
                               "office": "%s",
                               "company": "%s"
                            }
                         }
                        \s""".formatted(jobApplicant.name(), 2025,
                        jobApplicant.oficce(), jobApplicant.Company()))
        .when()
                .put()
        .then()
                .statusCode(equalTo(HttpStatus.SC_NOT_FOUND))
                .and()
                .body("error", equalTo(("The Object with id = %s doesn't exist. Please provide an object id which " +
                        "exists or generate a new Object using POST request and capture the id of it to use it as " +
                        "part of PUT request after that.").formatted(id)));
    }

    @DisplayName("Deve atualizar parcialmente um campo existente do objeto criado no method PATCH.")
    @Order(7)
    @Test
    public void testPatchSuccess() {
        var nameUpdate = "Francieli Atualizada";
        givenCreateObject();
        given()
                .basePath("objects/{id}")
                .pathParam("id", idCreateObject)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        {\s
                            "name": "%s"
                         }
                        \s""".formatted(nameUpdate))
        .when()
                .patch()
        .then()
                .statusCode(equalTo(HttpStatus.SC_OK))
                .and()
                .body("id", notNullValue())
                .and()
                .body("updatedAt", notNullValue())
                .and()
                .body("name", equalTo(nameUpdate))
                .and()
                .body("data.year", equalTo(jobApplicant.year()))
                .and()
                .body("data.office", equalTo(jobApplicant.oficce()))
                .and()
                .body("data.company", equalTo(jobApplicant.Company()));
    }

    @DisplayName("Não Deve permitir atualizar campos que não existentes do objeto criado no method PATCH.")
    @Order(8)
    @Test
    public void testPatchFail() {
        givenCreateObject();
        given()
                .basePath("objects/{id}")
                .pathParam("id", idCreateObject)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .body("""
                        {\s
                            "fullName": "%s"
                         }
                        \s""".formatted("Francieli Atualizada"))
        .when()
                .patch()
        .then()
                .statusCode(equalTo(HttpStatus.SC_NOT_FOUND))
                .and()
                .body("error", equalTo(("No valid field(s) to update have been passed as part of a request body.")));
    }

    @DisplayName("Deve deletar o objeto pelo id no methor DELETE")
    @Order(9)
    @Test
    public void testDeleteSuccess() {
        givenCreateObject();
        given()
                .basePath("objects/{id}")
                .pathParam("id", idCreateObject)
        .when()
                .delete()
        .then()
                .statusCode(equalTo(HttpStatus.SC_OK))
                .and()
                .body("message", equalTo("Object with id = %s has been deleted.".formatted(idCreateObject)));
    }

    @DisplayName("Deve retornar 404(NOT FOUND) se o objeto não for encontrado no method DELETE")
    @Order(10)
    @Test
    public void testDeleteFail() {
        var id = "fgdfgfsfdhgfhsfhg";
        given()
                .basePath("objects/{id}")
                .pathParam("id", id)
        .when()
                .delete()
        .then()
                .statusCode(equalTo(HttpStatus.SC_NOT_FOUND))
                .and()
                .body("error", equalTo("Object with id = %s doesn't exist.".formatted(id)));
    }

    public void givenCreateObject() {
        idCreateObject =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                        .body("""
                                {\s
                                    "name": "%s",
                                    "data": {
                                       "year": %d,
                                       "office": "%s",
                                       "company": "%s"
                                    }
                                 }
                                \s""".formatted(jobApplicant.name(), jobApplicant.year(),
                                jobApplicant.oficce(), jobApplicant.Company()))
                .when()
                        .post("objects")
                .then()
                        .statusCode(equalTo(HttpStatus.SC_OK))
                        .and()
                        .body("id", notNullValue())
                        .and()
                        .extract().path("id");
    }
}
