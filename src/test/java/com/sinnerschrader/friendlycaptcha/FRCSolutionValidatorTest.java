package com.sinnerschrader.friendlycaptcha;

import com.sinnerschrader.friendlycaptcha.exception.FRCSolutionValidatorException;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.JsonResponse;
import kong.unirest.json.JSONObject;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

public class FRCSolutionValidatorTest {

  private FRCSolutionValidator frcSolutionValidator;

  @BeforeEach
  public void setUp() {
    this.frcSolutionValidator = FRCSolutionValidator.builder().secret("xxxxx").build();
  }

  @Test
  public void testInvalidSecret() {
    val frcSolutionValidator = Mockito.spy(this.frcSolutionValidator);
    val solution = "test";
    val response = createMockResponse(new JSONObject(), 401);
    doReturn(response).when(frcSolutionValidator).checkSolution(solution);

    val exception =
        assertThrows(
            FRCSolutionValidatorException.class,
            () -> {
              frcSolutionValidator.isValidSolution(solution);
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  @Test
  public void testInvalidSolution() throws FRCSolutionValidatorException {
    val frcSolutionValidator = Mockito.spy(this.frcSolutionValidator);
    val solution = "test";
    val response =
        createMockResponse(new JSONObject("{errors: [solution_invalid], success: false}"), 200);
    doReturn(response).when(frcSolutionValidator).checkSolution(solution);

    assertFalse(frcSolutionValidator.isValidSolution(solution));
  }

  @Test
  public void testValidSolution() throws FRCSolutionValidatorException {
    val frcSolutionValidator = Mockito.spy(this.frcSolutionValidator);
    val solution = "test";
    val response = createMockResponse(new JSONObject("{errors: [], success: true}"), 200);
    doReturn(response).when(frcSolutionValidator).checkSolution(solution);

    assertTrue(frcSolutionValidator.isValidSolution(solution));
  }

  @Test
  public void testMultipleErrors() throws FRCSolutionValidatorException {
    val frcSolutionValidator = Mockito.spy(this.frcSolutionValidator);
    val solution = "test";
    val response =
        createMockResponse(
            new JSONObject(
                "{errors: [solution_invalid,sitekey_invalid,secret_invalid], success: false}"),
            200);
    doReturn(response).when(frcSolutionValidator).checkSolution(solution);

    val exception =
        assertThrows(
            FRCSolutionValidatorException.class,
            () -> {
              frcSolutionValidator.isValidSolution(solution);
            });

    assertEquals(HttpStatus.OK, exception.getStatusCode());
  }

  private HttpResponse<JsonResponse> createMockResponse(JSONObject body, int statusCode) {
    @SuppressWarnings("unchecked")
    val response = (HttpResponse<JsonResponse>) Mockito.mock(HttpResponse.class);
    doReturn(statusCode).when(response).getStatus();
    doReturn(new JsonNode(body.toString())).when(response).getBody();
    return response;
  }
}
