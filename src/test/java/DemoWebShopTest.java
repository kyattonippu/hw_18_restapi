import com.codeborne.selenide.Configuration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class DemoWebShopTest {

    private static final String MINIMAL_CONTENT_PATH = "/Themes/DefaultClean/Content/styles.css";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://demowebshop.tricentis.com/";
        Configuration.baseUrl = "http://demowebshop.tricentis.com/";
    }

    @Test
    void addToCardTest() {
        step("Add the product to the cart and check quantity", () -> {
            String data = "product_attribute_72_5_18=53&" +
                    "product_attribute_72_6_19=54&" +
                    "product_attribute_72_3_20=57&" +
                    "addtocart_72.EnteredQuantity=1";
            Response response =
                    given()
                            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                            .body(data)
                            .when()
                            .post("http://demowebshop.tricentis.com/addproducttocart/details/72/1")
                            .then()
                            .statusCode(200)
                            .body("updatetopcartsectionhtml", is("(1)"))
                            .body("message", is("The product has been added to your <a href=\"/cart\">shopping cart</a>"))
                            .extract().response();

            System.out.println("Response " + response.asString());
        });
    }

    @Test
    void authWithCookieTest() {
        String login = "maya@qa.guru";
        String password = "uEDX5e6MKCcC6B9";

        step("Authorization with cookie", () -> {
            String authorizationCookie = given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .formParam("Email", login)
                    .formParam("Password", password)
                    .when()
                    .post("/login")
                    .then()
                    .statusCode(302)
                    .extract()
                    .cookie("NOPCOMMERCE.AUTH");

            step("Open minimal content, because cookie can be set when site is opened", () ->
                    open(MINIMAL_CONTENT_PATH));

            step("Set cookie to to browser", () ->
                    getWebDriver().manage().addCookie(
                            new Cookie("NOPCOMMERCE.AUTH", authorizationCookie)));
        });
        step("Open main page", () ->
                open("http://demowebshop.tricentis.com"));

        step("Verify successful authorization", () -> {
            $(".account").shouldHave(text(login));
        });
    }
}
