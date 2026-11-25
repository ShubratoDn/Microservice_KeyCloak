# Custom Keycloak login page

Keycloak shows its own login page. You do not change Keycloak itself. You add a **theme**: a folder with a few files that tells Keycloak how the page should look. Then you pick that theme for your realm.

This guide shows how to do it step by step. It is the same way the `learn-microservices` theme in [keycloak-theme/](../keycloak-theme/) was made.

```
Browser -> React app -> "Log in" -> Keycloak login page (your theme) -> back to the React app with a token
```

The login page belongs to Keycloak, not to React. So one theme styles the login for **every** app in the realm.

## How a theme works (the short version)

| Piece | What it is | Example |
|---|---|---|
| **Theme folder** | A folder inside `<keycloak>/themes/` | `D:\keycloak\themes\learn-microservices` |
| **Theme type** | `login`, `account`, `admin`, `email`. We only make `login`. | `learn-microservices/login/` |
| **Parent theme** | A theme you start from, so you only write the differences | `parent=keycloak.v2` |
| **`.ftl` file** | The HTML of a page (FreeMarker template). Optional. | `template.ftl`, `login.ftl` |
| **CSS file** | Colors, spacing, fonts | `resources/css/learn.css` |
| **messages file** | All the texts on the page | `messages/messages_en.properties` |

The most important idea is the **parent**. Your theme starts as a copy of the parent. Any file you add with the same name **replaces** the parent's file. Any file you do not add is taken from the parent. So you can change just the colors and nothing else, or go as far as changing the whole layout.

## Pick your level

| Level | What you change | Time | Files |
|---|---|---|---|
| 1. Colors and logo | CSS only | 10 minutes | `theme.properties`, `learn.css` |
| 2. Texts | Words on the page | 5 minutes | `messages_en.properties` |
| 3. Layout | The HTML around the form | 1 hour | `template.ftl` |

Do level 1 first. Add the others only when you need them.

## Step 1: Create the folders

Inside the Keycloak folder:

```
D:\keycloak\themes\my-theme\login\
    theme.properties
    resources\css\my.css
    resources\img\favicon.svg      (optional)
    messages\messages_en.properties (optional)
    template.ftl                    (optional)
```

The folder name (`my-theme`) is the name you will pick in the admin console later.

## Step 2: `theme.properties`

```properties
# Start from the default Keycloak theme.
parent=keycloak.v2

# Load the default CSS first, then ours. Ours is last, so it wins.
styles=css/styles.css css/my.css
```

Why `css/styles.css` too? The line `styles=` **replaces** the parent's list. The parent's `css/styles.css` has small but needed things (tooltips, social login buttons). If you leave it out, those break. Keycloak finds `css/styles.css` in the parent folder by itself.

## Step 3: Your CSS

Create `resources/css/my.css`. Start small:

```css
/* Page background */
body#keycloak-bg {
  background: #f4f6fa !important;
}

/* The main button */
.pf-v5-c-button.pf-m-primary {
  background: #3b5bdb;
  border-radius: 8px;
}
```

Why the odd class names like `pf-v5-c-button`? The default theme uses **PatternFly 5**, a CSS library. Every part of the page has a PatternFly class. To style a part, you need its class name.

**How to find a class name:** open the login page in the browser, right-click the part, choose **Inspect**, and read the `class="..."`. Then write a rule for it in your CSS.

Useful classes on the login page:

| Part | Class |
|---|---|
| Text box (the box around the input) | `.pf-v5-c-form-control` |
| Password box + eye button | `.pf-v5-c-input-group` |
| Main button | `.pf-v5-c-button.pf-m-primary` |
| Error / success message | `.pf-v5-c-alert` |
| Page title ("Sign in") | `#kc-page-title` |
| The form card | `.pf-v5-c-login__main` |

If a rule does not seem to work, the default CSS is stronger. Make your rule more specific (for example, start it with `.pf-v5-c-login__main`) or add `!important` as a last step.

### Dark mode

The default theme turns on dark mode by adding the class `pf-v5-theme-dark` to the `<html>` tag when the computer is in dark mode. So:

```css
:root            { --my-bg: #ffffff; --my-text: #1c2333; }   /* light */
.pf-v5-theme-dark { --my-bg: #11151f; --my-text: #e8ebf4; }   /* dark  */
```

Put your colors in variables like this once, then use `var(--my-bg)` everywhere. Light and dark both work.

## Step 4: Choose the theme in Keycloak

1. Open the admin console, switch to your realm (`Learn_Microservices`).
2. **Realm settings** > **Themes** tab.
3. **Login theme** = `my-theme`.
4. **Save**.

Or with the Admin API (what we did):

```powershell
# 1. get an admin token
$t = (curl.exe -s -X POST "http://localhost:8180/realms/master/protocol/openid-connect/token" -d "grant_type=password" -d "client_id=admin-cli" -d "username=<admin-user>" -d "password=<admin-password>" | ConvertFrom-Json).access_token
# 2. set the login theme
curl.exe -X PUT "http://localhost:8180/admin/realms/Learn_Microservices" -H "Authorization: Bearer $t" -H "Content-Type: application/json" -d '{"realm":"Learn_Microservices","loginTheme":"my-theme"}'
```

Now open the React app and click **Log in**. You see your theme.

**Seeing changes:** `kc.bat start-dev` turns theme caching off. Edit a file, refresh the browser (Ctrl+F5), done. In production, Keycloak caches themes, so restart it after a change.

## Step 5 (optional): Change the texts

Create `messages/messages_en.properties`. Only write the keys you want to change. Everything else still comes from the parent.

```properties
loginAccountTitle=Sign in
doLogIn=Sign in
usernameOrEmail=Username or email
```

You can also add **your own keys** and use them in a template (see step 6):

```properties
brandHeadline=One secure sign-in for every service.
```

For another language, add `messages_bn.properties` and so on. Keycloak picks the file that matches the user's language.

## Step 6 (optional): Change the layout

CSS can move and hide things, but it cannot add new elements (a side panel, a logo block). For that, replace `template.ftl`. Every login page (sign in, forgot password, errors ...) is wrapped by this one file, so one change reaches all of them.

How we did it:

1. Copy the parent's file as a starting point. It is inside Keycloak's jar:
   `D:\keycloak\lib\lib\main\org.keycloak.keycloak-themes-<version>.jar`, at `theme/keycloak.v2/login/template.ftl`. A jar is a zip file, so you can open it with any unzip tool.
2. Keep the whole `<head>` and the `<main> ... </main>` block as they are. They contain Keycloak's logic: the page title, the error messages, the language picker, the form and the social login buttons. Do not delete them.
3. Change only what is **around** `<main>`. We removed the big title above the card and added a brand panel and a form panel:

```html
<div class="my-shell">
  <aside class="my-brand"> ...logo, headline, diagram... </aside>
  <div class="my-panel">
    <div class="pf-v5-c-login">
      <div class="pf-v5-c-login__container">
        <main class="pf-v5-c-login__main"> ...the original block... </main>
      </div>
    </div>
  </div>
</div>
```

4. Use texts from the messages file with `${msg("brandHeadline")}`. Use theme settings with `${properties.brandName}`.
5. Style the new classes (`my-shell`, `my-brand`, `my-panel`) in your CSS.

Things to know when you edit a `.ftl` file:

- It is **FreeMarker**: `${...}` prints a value, `<#if ...>` is a condition, `<#macro ...>` is a reusable block. A typo breaks the page with an error, so change a little, then refresh.
- Do not put your own text straight into the file if you can avoid it. Put it in `messages_en.properties` so it is easy to change later.
- To change only one page, copy only that file (for example `login.ftl`) instead of `template.ftl`. The page files are in the same folder inside the jar.
- Keycloak updates can change the default `template.ftl`. After an upgrade, compare your copy with the new one.

## How to open the login page to test it

The login page only opens through a real login request. If the React app is running, just click **Log in**. To open it directly (for a screenshot, for example), use the address below. The client needs **PKCE**, so the `code_challenge` part is required. Without it, Keycloak sends you straight back to the app.

```
http://localhost:8180/realms/Learn_Microservices/protocol/openid-connect/auth
  ?client_id=learn-microservices-web
  &redirect_uri=http%3A%2F%2Flocalhost%3A5173%2F
  &response_type=code&scope=openid
  &code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256
```

(Write it on one line.)

Test these states, not only the empty form:

- Wrong password (the error message).
- Dark mode and light mode.
- A narrow phone-size window.
- Other pages, such as **Forgot password**, if you enabled it.

## Things that can go wrong

| Problem | Cause | Fix |
|---|---|---|
| Theme is not in the list in **Login theme** | Wrong folder | It must be `themes\<name>\login\` inside the Keycloak folder. Restart Keycloak once. |
| No change after editing | Browser cache | Hard refresh (Ctrl+F5). Check that you edited the file inside `D:\keycloak\themes`, not only your copy in the project. |
| Tooltips or social buttons look broken | `styles=` dropped the default CSS | Keep `css/styles.css` first in `styles=`. |
| My CSS rule is ignored | The default rule is stronger | Use a more specific selector, or `!important` as a last step. |
| Page shows an error instead of the login | Typo in `template.ftl` | Read the Keycloak console log. It names the line. Undo your last change. |
| Page looks wrong in dark mode only | Colors set only for light mode | Add the same variables under `.pf-v5-theme-dark`. |
| Opening the login URL jumps back to the app | Missing PKCE parameters | Use the full address above. |

## Going back

Realm settings > Themes > **Login theme** > choose `keycloak.v2` (the default). The theme folder can stay where it is.

## Using it on another computer

The theme is just a folder. Copy `keycloak-theme/learn-microservices` from this project into `<keycloak>/themes/` on the other machine, then do step 4. With Docker, mount the folder to `/opt/keycloak/themes/learn-microservices`.

## Other options

- **Keycloakify** lets you build the theme as a React app and exports it as a `.jar`. It gives full control and fits a React team, but it has a build step and more setup. Worth it if the login page becomes a big part of the product.
- The same theme system also customizes the **account** page, the **admin console** and **emails**. Make an `account/`, `admin/` or `email/` folder next to `login/` and follow the same steps.
- Do **not** build your own login form in React that sends the password to Keycloak. It breaks two-factor login and social login, and your app would handle passwords.
