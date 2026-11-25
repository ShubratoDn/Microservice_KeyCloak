# Keycloak login theme: `learn-microservices`

A custom look for the Keycloak login pages (sign in, reset password, one-time code, errors).
It extends the default `keycloak.v2` theme, so everything keeps working. Only the look changes.

```
learn-microservices/login/
  theme.properties             parent theme + brand name
  template.ftl                 page layout (brand panel on the left, form on the right)
  messages/messages_en.properties   all texts, change them here
  resources/css/learn.css      all colors and spacing (same colors as the React app)
  resources/img/favicon.svg
```

## Install

1. Copy the folder `learn-microservices` into the `themes` folder of Keycloak:
   `D:\keycloak\themes\learn-microservices`
2. Keycloak admin console > realm **Learn_Microservices** > **Realm settings** > **Themes**
   > **Login theme** = `learn-microservices` > Save.

With `start-dev`, theme caching is off. Change a file, refresh the browser, done.
In production, restart Keycloak after a change.

## Change things

- Name next to the logo and in the footer: `brandName` in `theme.properties`.
- Headline and texts: `messages/messages_en.properties`.
- Colors: the variables at the top of `resources/css/learn.css`
  (light colors in `:root`, dark colors in `.pf-v5-theme-dark`).
- Dark mode follows the system setting, like the React app.
