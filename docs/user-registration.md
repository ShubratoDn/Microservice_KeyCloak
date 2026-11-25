# User registration (admin, manager, staff)

An **admin** logs in, opens **Users** and registers new people. Each new person gets a Keycloak login and a profile.

```
Browser (admin) -> API Gateway :8080 -> user-service :8084 -> Keycloak Admin API  (creates the login + roles)
                                                           -> PostgreSQL ms_user  (saves the profile)
```

## Why a separate service?

The user-service owns the **profile** (phone, addresses, gender, religion ...) in its own database, like every other service. Keycloak only knows what it needs for login: name, email, password, roles. The two are linked by `keycloak_id`.

## What an admin enters

| Field | Required | Notes |
|---|---|---|
| First name | yes | |
| Last name | no | |
| Email | yes | Cannot be changed later. |
| Username | no | The login name. 3 to 100 characters: letters, numbers, dot, underscore, hyphen or @. Saved in lower case. If empty, the email is the login name. Cannot be changed later. |
| Phone | yes | 7 to 20 characters: digits, spaces, dashes, optional `+` |
| Present address | yes | |
| Permanent address | no | The form has a "same as present address" button |
| Gender | yes | `MALE`, `FEMALE`, `OTHER` |
| Religion | no | `ISLAM`, `HINDUISM`, `BUDDHISM`, `CHRISTIANITY`, `JUDAISM`, `SIKHISM`, `OTHER`, `PREFER_NOT_TO_SAY` |
| User type | yes | `ADMIN`, `MANAGER`, `STAFF` |
| Temporary password | yes | 8 to 64 characters. The user **must change it at the first login**. |

## What each user type can do

The user type decides the Keycloak realm roles. They are assigned automatically (`UserTypeRoles.java`).

| Area | ADMIN | MANAGER | STAFF |
|---|---|---|---|
| Customers | add, view, edit, delete | add, view, edit | add, view |
| Products and categories | add, view, edit, delete | add, view, edit | view |
| Orders | add, view, edit, delete | add, view, edit | add, view |
| Users | add, view, edit, delete | view | none |

Each user also gets a marker role with the type name (`ADMIN`, `MANAGER` or `STAFF`). Changing a user's type swaps the roles.

## Safety rules

- Only users with `USER_ADD`, `USER_VIEW`, `USER_EDIT`, `USER_DELETE` can use the endpoints (checked in the service, not only in the UI).
- You cannot **disable, delete or change the type of your own account**, so an admin cannot lock themselves out.
- If saving the profile fails after the Keycloak login was created, the login is removed again, so there are no half-created users.
- Existing Keycloak users that were not registered through this service (for example `admin.user`) have **no profile** and are not listed.

## Set it up (once)

1. **Keycloak** (admin console, realm `Learn_Microservices`):
   - **Realm roles:** create `USER_ADD`, `USER_VIEW`, `USER_EDIT`, `USER_DELETE` and the type roles `ADMIN`, `MANAGER`, `STAFF`.
   - **Client for the service:** Clients > Create client. Client ID `user-service`, Client authentication **On**, turn **off** Standard flow and Direct access grants, turn **on** Service accounts roles. Save.
   - **Give it admin rights:** open that client > **Service accounts roles** tab > Assign role > filter by clients > pick `realm-management` roles `manage-users`, `view-users`, `query-users` and `view-realm`.
   - **Allow the Admin API (Keycloak 26.8 and newer):** open that client > **Client scopes** tab > Add client scope > pick `kc-admin-api` > Add as **Default**. Without it the Admin API answers `401` even when the roles are right (the Keycloak log says "Token is missing the required admin API scope").
   - **Make an admin:** Users > `admin.user` > Role mapping > assign `ADMIN`, `USER_ADD`, `USER_VIEW`, `USER_EDIT`, `USER_DELETE`.
   - **Optional last name:** Realm settings > User profile > `lastName` > turn **Required** off. Without this, Keycloak refuses to log in a user who has no last name ("Account is not fully set up").
2. **Secret:** copy the client secret (Keycloak: Clients > `user-service` > Credentials) and set it on your computer before starting the service. Never commit it.
   ```powershell
   $env:KEYCLOAK_ADMIN_CLIENT_SECRET = "the-secret"
   ```
3. **Database:** `CREATE DATABASE ms_user;`
4. **Start order:** Keycloak, Config Server, Eureka, then `user` (port 8084), then restart the API Gateway so it loads the new `/api/v1/users/**` route.
5. Log out and in again as `admin.user` (the new roles are inside the token), then open **Users**.

## API (through the gateway)

| Method | URL | Role |
|---|---|---|
| POST | `/api/v1/users` | `USER_ADD` |
| GET | `/api/v1/users?userType=STAFF&q=rahim&page=0&size=10` | `USER_VIEW` |
| GET | `/api/v1/users/{id}` | `USER_VIEW` |
| PUT | `/api/v1/users/{id}` | `USER_EDIT` |
| PATCH | `/api/v1/users/{id}/status` with `{"enabled": false}` | `USER_EDIT` |
| DELETE | `/api/v1/users/{id}` | `USER_DELETE` |

The Postman collection has a `user-service` folder with all of these.
