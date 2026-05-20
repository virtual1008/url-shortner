# Why Standardized Error Handling (RFC 7807)

## 1. The Initial Problem: "Magic Strings" and Inconsistent Contracts
In the early iterations of **LinkScale**, exceptions were handled by throwing standard Runtime exceptions with hardcoded messages. For example:
`throw new RuntimeException("URL not found");`

While functional for local development, this approach created several major issues at scale:
* **Inconsistent API Contracts:** If the Auth module threw an error, it might return a JSON structure like `{"message": "Unauthorized"}`. If the URL module threw an error, it might return `{"error": "URL not found", "status": 404}`. This inconsistency forces frontend developers to write complex, fragile parsing logic to guess where the error message lives in the response.
* **The "Magic String" Anti-Pattern:** Hardcoding text directly into the Java Service layer meant that changing a simple typo required recompiling and redeploying the entire application. 
* **Lack of Machine-Readable Codes:** External clients and microservices had no reliable way to programmatically identify *why* a request failed (e.g., distinguishing between a URL that expired vs. a URL that was deleted) without doing unstable string-matching on the error message.
* **Internationalization (i18n) Blocker:** Serving error messages to a global audience in their native language is impossible when English strings are baked into the compiled code.

## 2. What is RFC 7807?
To solve these issues, we adopted **RFC 7807 (Problem Details for HTTP APIs)**. 

RFC 7807 is an industry-standard specification created by the IETF that defines a universal JSON structure for returning errors from HTTP APIs. Instead of inventing our own custom error formats, RFC 7807 dictates that every error should contain specific fields:

* `type`: A URI identifier that categorizes the error (often a link to documentation).
* `title`: A short, human-readable summary of the problem.
* `status`: The HTTP status code.
* `detail`: A human-readable explanation specific to this occurrence.
* `instance`: A URI reference that identifies the specific occurrence of the problem.

**Before (Custom, Inconsistent Error):**
```json
{
  "error": "Not Found",
  "message": "The custom alias already exists"
}
```
**After (RFC 7807 Standardized Error):**
```json
{
  "type": "[https://api.linkscale.com/errors/URL-1003](https://api.linkscale.com/errors/URL-1003)",
  "title": "Conflict",
  "status": 409,
  "detail": "The custom alias is already in use. Please choose another.",
  "instance": "/api/v1/url/shorten",
  "errorCode": "URL-1003",
  "timestamp": "2026-05-20T17:37:23Z"
}
```
## 3. The Implementation Strategy 
To enforce this standard across LinkScale, we completely overhauled the exception architecture:
1. Interface-Driven Domain Errors: We created a base `ErrorCode` interface. Each domain in our system now implements this interface via an Enum (e.g., `UrlErrorCode`). This guarantees that every error has a specific code (like `URL-1001`), an HTTP Status, and a lookup key.
2. Externalized Properties: All human-readable text was moved out of the Java code and into a `messages.properties` file. The Java code now only references the Enum keys.
3. Unified Global Exception Handler: We consolidated all error parsing into a single `@RestControllerAdvice` class. This handler intercepts our custom `BaseAppException`, looks up the correct text from the properties file, and automatically formats the output into a Spring Boot 3 `ProblemDetail` object (which natively implements RFC 7807).

## 4. How This Resolves Our Issues (The Impact)
1. Zero Frontend Friction: Any external client or frontend team consuming the LinkScale API now knows exactly how to parse errors. They can write a single, global error interceptor on the frontend because the JSON structure is mathematically consistent.
2. Decoupled Architecture: Business logic is no longer cluttered with UI concerns. The Service layer simply states what happened (`throw new AliasAlreadyExistsException(UrlErrorCode.ALIAS_EXISTS)`), and the Exception Handler decides how to display it to the user.
3. Future-Proof for Microservices: By utilizing an `ErrorCode` interface rather than a single monolithic Enum, future microservices (like a Billing or User service) can define their own errors without causing merge conflicts in a shared repository.
4. Instant i18n Support: If we launch LinkScale in a new region, we simply drop a new file (e.g., `messages_es.properties`) into the resources folder. Spring Boot will automatically serve translated errors based on the user's browser locale, with zero changes to the underlying Java code.
