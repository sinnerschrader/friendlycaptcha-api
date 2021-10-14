# friendlycaptcha-api

> A lightweight java library to validate [FriendlyCaptcha](https://www.friendlycaptcha.com) solutions in Java Services.

# Usage

```java 
FRCSolutionValidator validator = FRCSolutionValidator
                  .builder()
                    .secret("<your frc api key>")
                    .sitekey("<your frc sitekey>") // optional
                  .build();

if (validator.isValidSolution(solution)) { // solution coming from frc js
  // let person in
} else {
  // consider person a robot
}
```
