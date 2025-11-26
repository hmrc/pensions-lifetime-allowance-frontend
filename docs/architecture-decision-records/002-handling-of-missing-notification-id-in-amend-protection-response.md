# 2. Handling of missing Notification ID in amend protection response

Date: 2025-11-26

## Status

Accepted

## Context

During migration from NPS to HIP API, it turned out that it is possible for Notification ID to not be present in API response.
It is a very rare scenario, when user amends their pensions protections and the total value of pensions exceeds the upper threshold of protected amount.
This situation may happen for more than one protection type.

## Decision

We will handle the scenario when Notification ID is not present in HIP API response, by making this field optional and by displaying confirmation page with proper explanation.

## Consequences

The service will not return 500 error response in the above scenario. It will also provide relevant information to the user.

Notification ID is a parameter used to determine what happened to given pension protection after the amendment.
All scenarios but the one described above, have been assigned a Notification ID.
They are visible in documentation, but the "no Notification ID" scenario is not documented.
Therefore, there is a risk that at some point in the future, the meaning of "no Notification ID" changes without warning.
When that happens, there is no way for us to notice that. We can only rely on users to raise concerns about the information they see, but this may not always be the case.
