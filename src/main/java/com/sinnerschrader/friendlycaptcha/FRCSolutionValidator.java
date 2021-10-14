package com.sinnerschrader.friendlycaptcha;

import com.sinnerschrader.friendlycaptcha.exception.FRCSolutionValidatorException;
import kong.unirest.*;
import lombok.Builder;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Builder
public class FRCSolutionValidator {
  @Builder.Default
  private String verificationEndpoint = "https://api.friendlycaptcha.com/api/v1/siteverify";

  @Builder.Default private boolean succeedOnConnectionError = true;
  @Builder.Default private Integer connectTimeout = Config.DEFAULT_CONNECT_TIMEOUT;
  @Builder.Default private Integer socketTimeout = Config.DEFAULT_SOCKET_TIMEOUT;

  private String sitekey;
  private final String secret;

  public boolean isValidSolution(String solution) throws FRCSolutionValidatorException {
    try {
      val response = checkSolution(solution);
      val responseBody = response.getBody().getObject();
      val errors = responseBody.optJSONArray("errors");
      if (response.getStatus() == HttpStatus.UNAUTHORIZED) {
        throw new FRCSolutionValidatorException(
            "Unauthorized: " + (errors != null ? Arrays.toString(errors.toList().toArray()) : ""),
            response.getStatus());
      }

      if (errors != null
          && errors.toList().size() == 1
          && errors.get(0).toString().equals("solution_invalid")) {
        return false;
      }

      if (errors != null && errors.toList().size() > 0) {
        throw new FRCSolutionValidatorException(
            "Invalid request: " + Arrays.toString(errors.toList().toArray()), response.getStatus());
      }

      return responseBody.optBoolean("success");
    } catch (UnirestException e) {
      if (succeedOnConnectionError) {
        return true;
      }

      throw new FRCSolutionValidatorException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    } finally {
      Unirest.shutDown();
    }
  }

  protected HttpResponse<JsonNode> checkSolution(String solution) {
    if (!Unirest.isRunning()) {
      Unirest.config().connectTimeout(connectTimeout).socketTimeout(socketTimeout);
    }

    val request =
        Unirest.post(verificationEndpoint)
            .header("accept", "application/json")
            .field("solution", solution)
            .field("secret", secret);
    if (StringUtils.isNotEmpty(sitekey)) {
      request.field("sitekey", sitekey);
    }

    return request.asJson();
  }
}
